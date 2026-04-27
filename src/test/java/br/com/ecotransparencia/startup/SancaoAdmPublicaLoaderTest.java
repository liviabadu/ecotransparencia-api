package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.domain.CadastroSancao;
import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.SancaoAdmPublicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de unidade para SancaoAdmPublicaLoader.
 *
 * Foco:
 * - Decodificacao ISO-8859-1 (acentos preservados)
 * - Discriminador {@code cadastro} populado conforme arquivo (CEIS|CNEP)
 * - {@code valorMulta} apenas em CNEP (NULL em CEIS)
 * - Offset de coluna correto (CNEP tem +1 col em VALOR DA MULTA)
 * - Idempotencia via DataLoadMarker
 * - Tolerancia a diretorio inexistente (no-op)
 */
class SancaoAdmPublicaLoaderTest {

    private SancaoAdmPublicaLoader loader;
    private SancaoAdmPublicaRepository repository;
    private DataLoadMarkerRepository markerRepository;
    private List<SancaoAdmPublica> persisted;

    @BeforeEach
    void setUp() {
        loader = new SancaoAdmPublicaLoader();
        repository = mock(SancaoAdmPublicaRepository.class);
        markerRepository = mock(DataLoadMarkerRepository.class);
        loader.repository = repository;
        loader.markerRepository = markerRepository;
        loader.csvDir = "src/test/resources/fixtures/adm_publica";
        loader.loadOnStartup = true;

        // Captura tudo o que e' persistido para inspecao
        persisted = new ArrayList<>();
        doAnswer(inv -> {
            persisted.add(inv.getArgument(0));
            return null;
        }).when(repository).persist(any(SancaoAdmPublica.class));
    }

    @Test
    @DisplayName("Idempotencia: nao executa quando marker ja existe")
    void shouldSkipWhenMarkerExists() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(new DataLoadMarker());

        loader.runLoad();

        verify(repository, never()).persist(any(SancaoAdmPublica.class));
        verify(markerRepository, never()).persist(any(DataLoadMarker.class));
    }

    @Test
    @DisplayName("Idempotencia: marca como carregado apos sucesso")
    void shouldMarkLoadedAfterRun() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        ArgumentCaptor<DataLoadMarker> captor = ArgumentCaptor.forClass(DataLoadMarker.class);
        verify(markerRepository).persist(captor.capture());
        assertEquals(SancaoAdmPublicaLoader.SOURCE, captor.getValue().getSource());
        assertNotNull(captor.getValue().getLoadedAt());
    }

    @Test
    @DisplayName("Carrega CEIS e CNEP na mesma tabela com discriminador correto")
    void shouldLoadBothCadastrosIntoSameTable() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        long ceisCount = persisted.stream().filter(s -> s.getCadastro() == CadastroSancao.CEIS).count();
        long cnepCount = persisted.stream().filter(s -> s.getCadastro() == CadastroSancao.CNEP).count();

        assertEquals(5, ceisCount, "Devem existir 5 registros CEIS");
        assertEquals(5, cnepCount, "Devem existir 5 registros CNEP");
    }

    @Test
    @DisplayName("ISO-8859-1: acentos preservados em nomeSancionado e fundamentacaoLegal")
    void shouldDecodeIso88591Correctly() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        // KM INDÚSTRIA E COMÉRCIO DE MÓVEIS / NF FARMACÊUTICA - acentos da fonte
        boolean hasAccent = persisted.stream()
                .map(SancaoAdmPublica::getNomeSancionado)
                .filter(java.util.Objects::nonNull)
                .anyMatch(n -> n.contains("Ó") || n.contains("É") || n.contains("Ê") || n.contains("Á"));
        assertTrue(hasAccent, "Algum nomeSancionado deve conter acentos preservados");
    }

    @Test
    @DisplayName("CEIS: valorMulta sempre null (coluna nao existe na fonte)")
    void shouldHaveNullValorMultaForCeis() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        boolean allCeisNull = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CEIS)
                .allMatch(s -> s.getValorMulta() == null);
        assertTrue(allCeisNull, "Todos os registros CEIS devem ter valorMulta=null");
    }

    @Test
    @DisplayName("CNEP: valorMulta presente, decimal pt-BR convertido (517662,90 -> 517662.90)")
    void shouldParseCnepValorMultaInPtBr() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        // O primeiro registro CNEP do fixture tem VALOR DA MULTA = 517662,90
        SancaoAdmPublica firstCnep = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CNEP)
                .findFirst()
                .orElseThrow();
        assertNotNull(firstCnep.getValorMulta());
        // Aceita 517662.90 (esperado) ou 517662.9 (BigDecimal sem trailing zero)
        assertEquals(0, new BigDecimal("517662.90").compareTo(firstCnep.getValorMulta()));
    }

    @Test
    @DisplayName("Offset de coluna respeitado: dataInicioSancao em CEIS (idx 10) e CNEP (idx 11)")
    void shouldRespectColumnOffsetBetweenCadastros() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        // CEIS row 1: dataInicioSancao = 26/04/2024
        SancaoAdmPublica ceis = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CEIS)
                .findFirst()
                .orElseThrow();
        assertEquals(LocalDate.of(2024, 4, 26), ceis.getDataInicioSancao(),
                "CEIS row deve ter dataInicioSancao = 2024-04-26");

        // CNEP row 1: dataInicioSancao = 03/08/2023 (campo +1 deslocado por VALOR DA MULTA)
        SancaoAdmPublica cnep = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CNEP)
                .findFirst()
                .orElseThrow();
        assertEquals(LocalDate.of(2023, 8, 3), cnep.getDataInicioSancao(),
                "CNEP row deve ter dataInicioSancao = 2023-08-03 (offset de +1 vs CEIS)");
    }

    @Test
    @DisplayName("CNPJ/CPF preservado da fonte (digitos apenas)")
    void shouldPreserveCpfCnpjFromSource() {
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        SancaoAdmPublica firstCeis = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CEIS)
                .findFirst()
                .orElseThrow();
        assertEquals("17344993000111", firstCeis.getCpfCnpj());

        SancaoAdmPublica firstCnep = persisted.stream()
                .filter(s -> s.getCadastro() == CadastroSancao.CNEP)
                .findFirst()
                .orElseThrow();
        assertEquals("55015050968", firstCnep.getCpfCnpj());
    }

    @Test
    @DisplayName("Diretorio inexistente: no-op sem excecao")
    void shouldHandleMissingDirectoryGracefully() {
        loader.csvDir = "src/test/resources/fixtures/does-not-exist";
        when(markerRepository.findById(SancaoAdmPublicaLoader.SOURCE)).thenReturn(null);

        assertDoesNotThrow(() -> loader.runLoad());
        verify(repository, never()).persist(any(SancaoAdmPublica.class));
        // Marker nao persistido pois nao houve carga
        verify(markerRepository, never()).persist(any(DataLoadMarker.class));
    }
}
