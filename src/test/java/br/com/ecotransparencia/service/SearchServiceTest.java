package br.com.ecotransparencia.service;

import br.com.ecotransparencia.domain.CadastroSancao;
import br.com.ecotransparencia.dto.SearchResponse;
import br.com.ecotransparencia.dto.SituacaoCadastralDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import br.com.ecotransparencia.repository.AutoInfracaoRepository;
import br.com.ecotransparencia.repository.CepimRepository;
import br.com.ecotransparencia.repository.EmbargoRepository;
import br.com.ecotransparencia.repository.SancaoAdmPublicaRepository;
import br.com.ecotransparencia.repository.TrabalhoEscravoMteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de unidade para SearchService.
 * Testa especialmente a logica de validacao de situacao cadastral (US-007).
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    private static final String CNPJ_VALIDO = "11222333000181";
    private static final String CPF_VALIDO = "52998224725";

    @Mock
    private EmbargoRepository embargoRepository;

    @Mock
    private AutoInfracaoRepository autoInfracaoRepository;

    @Mock
    private SancaoAdmPublicaRepository sancaoAdmPublicaRepository;

    @Mock
    private CepimRepository cepimRepository;

    @Mock
    private TrabalhoEscravoMteRepository trabalhoEscravoMteRepository;

    @Mock
    private AsgScoreCalculator asgScoreCalculator;

    @Mock
    private ReceitaFederalService receitaFederalService;

    @InjectMocks
    private SearchService searchService;

    @Nested
    @DisplayName("US-007: Validacao de Situacao Cadastral do CNPJ")
    class ValidacaoSituacaoCadastralTests {

        @Test
        @DisplayName("AC-01: Deve prosseguir com analise quando CNPJ esta ATIVO")
        void shouldProceedWithAnalysisWhenCnpjIsActive() {
            // Given
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound()); // Nao encontrou ocorrencias, mas nao foi bloqueado
            assertNull(response.getBloqueadoPorSituacaoCadastral());
            verify(embargoRepository).findByDocument(CNPJ_VALIDO);
            verify(autoInfracaoRepository).findByDocument(CNPJ_VALIDO);
        }

        @Test
        @DisplayName("AC-02: Deve bloquear analise quando CNPJ esta BAIXADA")
        void shouldBlockAnalysisWhenCnpjIsBaixada() {
            // Given
            SituacaoCadastralDto situacaoBaixada = SituacaoCadastralDto.valido("BAIXADA", "CNPJ baixado");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoBaixada);

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound());
            assertTrue(response.getBloqueadoPorSituacaoCadastral());
            assertEquals("BAIXADA", response.getSituacaoCadastral().getSituacao());
            assertNull(response.getEntity());

            // Verifica que NAO consultou as bases de dados
            verify(embargoRepository, never()).findByDocument(anyString());
            verify(autoInfracaoRepository, never()).findByDocument(anyString());
        }

        @Test
        @DisplayName("AC-02: Deve bloquear analise quando CNPJ esta SUSPENSA")
        void shouldBlockAnalysisWhenCnpjIsSuspensa() {
            // Given
            SituacaoCadastralDto situacaoSuspensa = SituacaoCadastralDto.valido("SUSPENSA", "CNPJ suspenso");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoSuspensa);

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound());
            assertTrue(response.getBloqueadoPorSituacaoCadastral());
            assertEquals("SUSPENSA", response.getSituacaoCadastral().getSituacao());
        }

        @Test
        @DisplayName("AC-02: Deve bloquear analise quando CNPJ esta INAPTA")
        void shouldBlockAnalysisWhenCnpjIsInapta() {
            // Given
            SituacaoCadastralDto situacaoInapta = SituacaoCadastralDto.valido("INAPTA", "CNPJ inapto");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoInapta);

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound());
            assertTrue(response.getBloqueadoPorSituacaoCadastral());
            assertEquals("INAPTA", response.getSituacaoCadastral().getSituacao());
        }

        @Test
        @DisplayName("AC-03: Deve bloquear analise quando API da Receita Federal esta indisponivel")
        void shouldBlockAnalysisWhenReceitaFederalApiUnavailable() {
            // Given
            SituacaoCadastralDto situacaoIndisponivel = SituacaoCadastralDto.invalido(
                "INDISPONIVEL",
                "Nao foi possivel validar o CNPJ"
            );
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoIndisponivel);

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound());
            assertTrue(response.getBloqueadoPorSituacaoCadastral());
            assertEquals("INDISPONIVEL", response.getSituacaoCadastral().getSituacao());
            assertFalse(response.getSituacaoCadastral().isValido());
        }

        @Test
        @DisplayName("AC-04: Deve bloquear analise quando CNPJ tem formato invalido")
        void shouldRejectInvalidCnpjWithoutCallingApi() {
            // Given
            SituacaoCadastralDto situacaoInvalida = SituacaoCadastralDto.invalido(
                "INVALIDO",
                "CNPJ com formato invalido"
            );
            when(receitaFederalService.consultarCnpj(anyString())).thenReturn(situacaoInvalida);

            // When
            SearchResponse response = searchService.searchByDocument("12345678000199", "cnpj");

            // Then
            assertFalse(response.isFound());
            assertTrue(response.getBloqueadoPorSituacaoCadastral());
            assertEquals("INVALIDO", response.getSituacaoCadastral().getSituacao());
        }

        @Test
        @DisplayName("AC-05: Deve prosseguir com analise de CPF sem validacao de situacao cadastral")
        void shouldProceedWithCpfAnalysisWithoutCnpjValidation() {
            // Given
            when(embargoRepository.findByDocument(CPF_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CPF_VALIDO)).thenReturn(Collections.emptyList());

            // When
            SearchResponse response = searchService.searchByDocument(CPF_VALIDO, "cpf");

            // Then
            assertFalse(response.isFound()); // Nao encontrou ocorrencias
            assertNull(response.getBloqueadoPorSituacaoCadastral()); // Nao foi bloqueado

            // Verifica que NAO chamou a validacao de CNPJ
            verify(receitaFederalService, never()).consultarCnpj(anyString());

            // Verifica que consultou as bases normalmente
            verify(embargoRepository).findByDocument(CPF_VALIDO);
            verify(autoInfracaoRepository).findByDocument(CPF_VALIDO);
        }
    }

    @Nested
    @DisplayName("Busca por documento")
    class BuscaPorDocumentoTests {

        @Test
        @DisplayName("Deve retornar not found quando nao ha ocorrencias e CNPJ esta ativo")
        void shouldReturnNotFoundWhenNoOccurrencesAndCnpjActive() {
            // Given
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            // When
            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            // Then
            assertFalse(response.isFound());
            assertNull(response.getBloqueadoPorSituacaoCadastral());
            assertNull(response.getEntity());
        }
    }

    @Nested
    @DisplayName("Fase B: agregacao de novas fontes (CEIS/CNEP/CEPIM/MTE)")
    class FaseBAggregacaoTests {

        @Test
        @DisplayName("Consulta as 3 novas fontes por CPF/CNPJ normalizado")
        void shouldQueryAllNewSources() {
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(sancaoAdmPublicaRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(cepimRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(trabalhoEscravoMteRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            verify(sancaoAdmPublicaRepository).findByCpfCnpj(CNPJ_VALIDO);
            verify(cepimRepository).findByCpfCnpj(CNPJ_VALIDO);
            verify(trabalhoEscravoMteRepository).findByCpfCnpj(CNPJ_VALIDO);
        }

        @Test
        @DisplayName("Popula sancoesAdmPublica em SearchResponse quando ha CEIS/CNEP")
        void shouldPopulateSancoesInResponse() {
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            SancaoAdmPublica ceis = new SancaoAdmPublica();
            ceis.setCadastro(CadastroSancao.CEIS);
            ceis.setCpfCnpj(CNPJ_VALIDO);
            ceis.setNomeSancionado("Empresa Teste");
            ceis.setOrgaoSancionador("Prefeitura X");
            when(sancaoAdmPublicaRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(List.of(ceis));
            when(cepimRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(trabalhoEscravoMteRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            assertNotNull(response.getSancoesAdmPublica());
            assertEquals(1, response.getSancoesAdmPublica().size());
            assertEquals("Empresa Teste", response.getSancoesAdmPublica().get(0).getNomeSancionado());
            assertEquals(CadastroSancao.CEIS, response.getSancoesAdmPublica().get(0).getCadastro());
        }

        @Test
        @DisplayName("Popula impedimentosCepim em SearchResponse quando ha CEPIM")
        void shouldPopulateCepimInResponse() {
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            Cepim cep = new Cepim();
            cep.setCnpjEntidade(CNPJ_VALIDO);
            cep.setNomeEntidade("Entidade X");
            cep.setMotivoImpedimento("Irregularidade");
            when(sancaoAdmPublicaRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(cepimRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(List.of(cep));
            when(trabalhoEscravoMteRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            assertNotNull(response.getImpedimentosCepim());
            assertEquals(1, response.getImpedimentosCepim().size());
            assertEquals("Entidade X", response.getImpedimentosCepim().get(0).getNomeEntidade());
        }

        @Test
        @DisplayName("Popula trabalhoEscravo em SearchResponse quando ha MTE")
        void shouldPopulateMteInResponse() {
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            TrabalhoEscravoMte mte = new TrabalhoEscravoMte();
            mte.setCpfCnpj(CNPJ_VALIDO);
            mte.setCpfCnpjFormatado("11.222.333/0001-81");
            mte.setEmpregador("Fulano");
            mte.setUf("SP");
            mte.setTrabalhadoresEnvolvidos(3);
            when(sancaoAdmPublicaRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(cepimRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(trabalhoEscravoMteRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(List.of(mte));

            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            assertNotNull(response.getTrabalhoEscravo());
            assertEquals(1, response.getTrabalhoEscravo().size());
            assertEquals("Fulano", response.getTrabalhoEscravo().get(0).getEmpregador());
            assertEquals(3, response.getTrabalhoEscravo().get(0).getTrabalhadoresEnvolvidos());
        }

        @Test
        @DisplayName("Quando ha apenas ocorrencias em fontes Fase B, found=true e listas presentes")
        void shouldReturnFoundWhenOnlyPhaseBOccurrences() {
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoAtiva);
            when(embargoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            SancaoAdmPublica ceis = new SancaoAdmPublica();
            ceis.setCadastro(CadastroSancao.CEIS);
            ceis.setCpfCnpj(CNPJ_VALIDO);
            when(sancaoAdmPublicaRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(List.of(ceis));
            when(cepimRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());
            when(trabalhoEscravoMteRepository.findByCpfCnpj(CNPJ_VALIDO)).thenReturn(Collections.emptyList());

            SearchResponse response = searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            assertTrue(response.isFound());
            assertEquals(1, response.getSancoesAdmPublica().size());
        }

        @Test
        @DisplayName("Bloqueio por situacao cadastral nao consulta fontes Fase B")
        void shouldNotQueryPhaseBSourcesWhenBlocked() {
            SituacaoCadastralDto situacaoBaixada = SituacaoCadastralDto.valido("BAIXADA", "CNPJ baixado");
            when(receitaFederalService.consultarCnpj(CNPJ_VALIDO)).thenReturn(situacaoBaixada);

            searchService.searchByDocument(CNPJ_VALIDO, "cnpj");

            verify(sancaoAdmPublicaRepository, never()).findByCpfCnpj(anyString());
            verify(cepimRepository, never()).findByCpfCnpj(anyString());
            verify(trabalhoEscravoMteRepository, never()).findByCpfCnpj(anyString());
        }

        @Test
        @DisplayName("Normaliza documento com pontuacao antes de consultar fontes Fase B")
        void shouldNormalizeDocumentBeforeQueryingPhaseB() {
            // CPF formatado deve ser normalizado para apenas digitos antes da query
            SituacaoCadastralDto situacaoAtiva = SituacaoCadastralDto.valido("ATIVA", "Cadastro ativo");
            String cpfNormalized = "52998224725";
            when(embargoRepository.findByDocument("529.982.247-25")).thenReturn(Collections.emptyList());
            when(autoInfracaoRepository.findByDocument("529.982.247-25")).thenReturn(Collections.emptyList());
            when(sancaoAdmPublicaRepository.findByCpfCnpj(cpfNormalized)).thenReturn(Collections.emptyList());
            when(cepimRepository.findByCpfCnpj(cpfNormalized)).thenReturn(Collections.emptyList());
            when(trabalhoEscravoMteRepository.findByCpfCnpj(cpfNormalized)).thenReturn(Collections.emptyList());

            searchService.searchByDocument("529.982.247-25", "cpf");

            // Fontes Fase B sao consultadas com a forma normalizada (digit-only)
            verify(sancaoAdmPublicaRepository).findByCpfCnpj(cpfNormalized);
            verify(cepimRepository).findByCpfCnpj(cpfNormalized);
            verify(trabalhoEscravoMteRepository).findByCpfCnpj(cpfNormalized);
        }
    }
}
