package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.EmbargoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Testes de idempotencia para IbamaDataLoader.
 *
 * Fase A: idempotencia agora baseada em DataLoadMarker (em vez de count() > 0).
 * Garante que cargas parciais nao causam duplicidade e que o gate e' explicito.
 */
class IbamaDataLoaderIdempotencyTest {

    private IbamaDataLoader loader;
    private DataLoadMarkerRepository markerRepository;
    private EmbargoRepository embargoRepository;

    @BeforeEach
    void setUp() {
        loader = new IbamaDataLoader();
        markerRepository = mock(DataLoadMarkerRepository.class);
        embargoRepository = mock(EmbargoRepository.class);
        loader.markerRepository = markerRepository;
        loader.repository = embargoRepository;
    }

    @Test
    @DisplayName("isAlreadyLoaded retorna true quando marker existe para a fonte")
    void shouldReturnTrueWhenMarkerExists() {
        when(markerRepository.findById("ibama_embargo")).thenReturn(new DataLoadMarker());

        assertTrue(loader.isAlreadyLoaded());
    }

    @Test
    @DisplayName("isAlreadyLoaded retorna false quando marker nao existe para a fonte")
    void shouldReturnFalseWhenMarkerMissing() {
        when(markerRepository.findById("ibama_embargo")).thenReturn(null);

        assertFalse(loader.isAlreadyLoaded());
    }

    @Test
    @DisplayName("markLoaded persiste marker com source=ibama_embargo")
    void shouldPersistMarkerOnMarkLoaded() {
        loader.markLoaded();

        verify(markerRepository).persist(argThat((DataLoadMarker m) ->
            "ibama_embargo".equals(m.getSource()) && m.getLoadedAt() != null
        ));
    }
}
