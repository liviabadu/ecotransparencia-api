package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.repository.CepimRepository;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CepimLoaderTest {

    private CepimLoader loader;
    private CepimRepository repository;
    private DataLoadMarkerRepository markerRepository;
    private List<Cepim> persisted;

    @BeforeEach
    void setUp() {
        loader = new CepimLoader();
        repository = mock(CepimRepository.class);
        markerRepository = mock(DataLoadMarkerRepository.class);
        loader.repository = repository;
        loader.markerRepository = markerRepository;
        loader.csvDir = "src/test/resources/fixtures/adm_publica";
        loader.loadOnStartup = true;

        persisted = new ArrayList<>();
        doAnswer(inv -> {
            persisted.add(inv.getArgument(0));
            return null;
        }).when(repository).persist(any(Cepim.class));
    }

    @Test
    @DisplayName("Carrega 5 registros da fixture")
    void shouldLoadFiveRecords() {
        when(markerRepository.findById(CepimLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        assertEquals(5, persisted.size());
    }

    @Test
    @DisplayName("Mapeia campos: cnpjEntidade, nomeEntidade, numeroConvenio, orgaoConcedente, motivoImpedimento")
    void shouldMapAllFields() {
        when(markerRepository.findById(CepimLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        Cepim first = persisted.get(0);
        assertEquals("06321332000154", first.getCnpjEntidade());
        assertEquals("CENTRO BRASIL TRABALHO - CBT", first.getNomeEntidade());
        assertEquals("622947", first.getNumeroConvenio());
        assertNotNull(first.getOrgaoConcedente());
        assertNotNull(first.getMotivoImpedimento());
    }

    @Test
    @DisplayName("ISO-8859-1: acentos preservados")
    void shouldDecodeIso88591() {
        when(markerRepository.findById(CepimLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        boolean hasAccent = persisted.stream()
                .map(Cepim::getOrgaoConcedente)
                .filter(java.util.Objects::nonNull)
                .anyMatch(o -> o.contains("ó") || o.contains("í") || o.contains("ã") || o.contains("ú") || o.contains("Ã"));
        assertTrue(hasAccent, "Algum orgaoConcedente deve conter acentos preservados");
    }

    @Test
    @DisplayName("Idempotencia: skip quando marker existe")
    void shouldSkipWhenMarkerExists() {
        when(markerRepository.findById(CepimLoader.SOURCE)).thenReturn(new DataLoadMarker());

        loader.runLoad();

        verify(repository, never()).persist(any(Cepim.class));
        verify(markerRepository, never()).persist(any(DataLoadMarker.class));
    }

    @Test
    @DisplayName("Marker persistido apos sucesso")
    void shouldPersistMarkerOnSuccess() {
        when(markerRepository.findById(CepimLoader.SOURCE)).thenReturn(null);

        loader.runLoad();

        ArgumentCaptor<DataLoadMarker> captor = ArgumentCaptor.forClass(DataLoadMarker.class);
        verify(markerRepository).persist(captor.capture());
        assertEquals(CepimLoader.SOURCE, captor.getValue().getSource());
    }
}
