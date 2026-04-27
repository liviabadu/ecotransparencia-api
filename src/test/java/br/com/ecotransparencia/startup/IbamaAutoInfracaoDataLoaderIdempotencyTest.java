package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.repository.AutoInfracaoRepository;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Testes de idempotencia para IbamaAutoInfracaoDataLoader.
 *
 * Fase A: idempotencia agora baseada em DataLoadMarker (em vez de count() > 0).
 */
class IbamaAutoInfracaoDataLoaderIdempotencyTest {

    private IbamaAutoInfracaoDataLoader loader;
    private DataLoadMarkerRepository markerRepository;
    private AutoInfracaoRepository autoInfracaoRepository;

    @BeforeEach
    void setUp() {
        loader = new IbamaAutoInfracaoDataLoader();
        markerRepository = mock(DataLoadMarkerRepository.class);
        autoInfracaoRepository = mock(AutoInfracaoRepository.class);
        loader.markerRepository = markerRepository;
        loader.repository = autoInfracaoRepository;
    }

    @Test
    @DisplayName("isAlreadyLoaded retorna true quando marker existe para ibama_auto_infracao")
    void shouldReturnTrueWhenMarkerExists() {
        when(markerRepository.findById("ibama_auto_infracao")).thenReturn(new DataLoadMarker());

        assertTrue(loader.isAlreadyLoaded());
    }

    @Test
    @DisplayName("isAlreadyLoaded retorna false quando marker nao existe")
    void shouldReturnFalseWhenMarkerMissing() {
        when(markerRepository.findById("ibama_auto_infracao")).thenReturn(null);

        assertFalse(loader.isAlreadyLoaded());
    }

    @Test
    @DisplayName("markLoaded persiste marker com source=ibama_auto_infracao")
    void shouldPersistMarkerOnMarkLoaded() {
        loader.markLoaded();

        verify(markerRepository).persist(argThat((DataLoadMarker m) ->
            "ibama_auto_infracao".equals(m.getSource()) && m.getLoadedAt() != null
        ));
    }
}
