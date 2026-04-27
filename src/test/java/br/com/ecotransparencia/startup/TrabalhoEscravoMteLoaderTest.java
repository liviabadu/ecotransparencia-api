package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.TrabalhoEscravoMteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrabalhoEscravoMteLoaderTest {

    private TrabalhoEscravoMteLoader loader;
    private TrabalhoEscravoMteRepository repository;
    private DataLoadMarkerRepository markerRepository;
    private List<TrabalhoEscravoMte> persisted;

    @BeforeEach
    void setUp() {
        loader = new TrabalhoEscravoMteLoader();
        repository = mock(TrabalhoEscravoMteRepository.class);
        markerRepository = mock(DataLoadMarkerRepository.class);
        loader.repository = repository;
        loader.markerRepository = markerRepository;
        loader.csvPath = "src/test/resources/fixtures/mte/tr_escravo.csv";
        loader.loadOnStartup = true;

        persisted = new ArrayList<>();
        doAnswer(inv -> {
            persisted.add(inv.getArgument(0));
            return null;
        }).when(repository).persist(any(TrabalhoEscravoMte.class));
    }

    @Test
    @DisplayName("Carrega todos os 5 registros da fixture")
    void shouldLoadFiveRecords() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        assertEquals(5, persisted.size());
    }

    @Test
    @DisplayName("Cp1252: acentos preservados (ã, õ, Ó, Á)")
    void shouldDecodeCp1252Correctly() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        // Estabelecimento da linha 1: "ROD. PREFEITO JOAQUIM SIMÃO, KM 735, CENTRO, IGARATÁ/SP"
        TrabalhoEscravoMte first = persisted.get(0);
        assertNotNull(first.getEstabelecimento());
        assertTrue(first.getEstabelecimento().contains("SIMÃO") || first.getEstabelecimento().contains("IGARATÁ"),
                "Estabelecimento deve preservar acentos: " + first.getEstabelecimento());
    }

    @Test
    @DisplayName("CPF/CNPJ formatado preservado e digit-only normalizado")
    void shouldStoreBothRawAndNormalizedCpfCnpj() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        TrabalhoEscravoMte first = persisted.get(0);
        assertEquals("41.297.068/0001-61", first.getCpfCnpjFormatado());
        assertEquals("41297068000161", first.getCpfCnpj());
    }

    @Test
    @DisplayName("Campos numericos parseados corretamente (idOrigem, anoAcaoFiscal, trabalhadores)")
    void shouldParseNumericFields() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        TrabalhoEscravoMte first = persisted.get(0);
        assertEquals(1, first.getIdOrigem());
        assertEquals(2024, first.getAnoAcaoFiscal());
        assertEquals(2, first.getTrabalhadoresEnvolvidos());
    }

    @Test
    @DisplayName("Datas parseadas no formato dd/MM/yyyy")
    void shouldParseDates() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        TrabalhoEscravoMte first = persisted.get(0);
        assertEquals(LocalDate.of(2025, 5, 15), first.getDecisaoAdmProcedencia());
        assertEquals(LocalDate.of(2025, 10, 6), first.getInclusaoCadastroEmpregadores());
    }

    @Test
    @DisplayName("Estabelecimento com virgulas internas preservado integralmente (sem quote-char)")
    void shouldPreserveCommasInsideFields() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        // Esse campo tem multiplas virgulas e espacos.
        TrabalhoEscravoMte first = persisted.get(0);
        assertTrue(first.getEstabelecimento().contains(","),
                "Virgulas dentro do campo devem ser preservadas: " + first.getEstabelecimento());
        assertTrue(first.getEstabelecimento().contains("KM 735"),
                "Conteudo apos virgula deve estar presente: " + first.getEstabelecimento());
    }

    @Test
    @DisplayName("Idempotencia: skip quando marker existe")
    void shouldSkipWhenMarkerExists() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(new DataLoadMarker());

        loader.runLoad();

        verify(repository, never()).persist(any(TrabalhoEscravoMte.class));
        verify(markerRepository, never()).persist(any(DataLoadMarker.class));
    }

    @Test
    @DisplayName("Marker persistido apos sucesso")
    void shouldPersistMarkerOnSuccess() {
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        ArgumentCaptor<DataLoadMarker> captor = ArgumentCaptor.forClass(DataLoadMarker.class);
        verify(markerRepository).persist(captor.capture());
        assertEquals(TrabalhoEscravoMteLoader.SOURCE, captor.getValue().getSource());
    }

    @Test
    @DisplayName("Arquivo inexistente: no-op sem excecao")
    void shouldHandleMissingFileGracefully() {
        loader.csvPath = "src/test/resources/fixtures/mte/does-not-exist.csv";
        when(markerRepository.findById(TrabalhoEscravoMteLoader.SOURCE)).thenReturn(null);

        assertDoesNotThrow(() -> loader.runLoad());
        verify(repository, never()).persist(any(TrabalhoEscravoMte.class));
    }
}
