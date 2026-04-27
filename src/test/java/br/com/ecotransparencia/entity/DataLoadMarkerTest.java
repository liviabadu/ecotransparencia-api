package br.com.ecotransparencia.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a entidade DataLoadMarker.
 *
 * Marker usada pelos loaders de startup para garantir idempotencia
 * (ver IbamaDataLoader / IbamaAutoInfracaoDataLoader na Fase A).
 */
@QuarkusTest
class DataLoadMarkerTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    @DisplayName("Deve persistir e recuperar marker preservando todos os campos")
    void shouldRoundTripAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 10, 30, 0);

        DataLoadMarker marker = new DataLoadMarker();
        marker.setSource("ibama_embargo");
        marker.setVersion("v1.0");
        marker.setLoadedAt(now);

        em.persist(marker);
        em.flush();
        em.clear();

        DataLoadMarker found = em.find(DataLoadMarker.class, "ibama_embargo");

        assertNotNull(found, "Marker deveria ser encontrado pela source");
        assertEquals("ibama_embargo", found.getSource());
        assertEquals("v1.0", found.getVersion());
        assertEquals(now, found.getLoadedAt());
    }

    @Test
    @Transactional
    @DisplayName("Deve permitir version null (apenas loaded_at e source obrigatorios)")
    void shouldAllowNullVersion() {
        DataLoadMarker marker = new DataLoadMarker();
        marker.setSource("ibama_auto_infracao");
        marker.setVersion(null);
        marker.setLoadedAt(LocalDateTime.now());

        em.persist(marker);
        em.flush();
        em.clear();

        DataLoadMarker found = em.find(DataLoadMarker.class, "ibama_auto_infracao");
        assertNotNull(found);
        assertNull(found.getVersion());
    }
}
