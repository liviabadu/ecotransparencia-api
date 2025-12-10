package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.SearchResponse;
import br.com.ecotransparencia.dto.SituacaoCadastralDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.repository.AutoInfracaoRepository;
import br.com.ecotransparencia.repository.EmbargoRepository;
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
}
