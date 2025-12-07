package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.repository.EmbargoRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para IbamaDataLoader.
 *
 * US-INFRA-001: Carregar base de dados do IBAMA no startup
 *
 * Verifica o comportamento do loader durante o startup da aplicação.
 */
@QuarkusTest
@TestProfile(IbamaDataLoaderIntegrationTest.DataLoadDisabledProfile.class)
class IbamaDataLoaderIntegrationTest {

    @Inject
    EmbargoRepository repository;

    @Inject
    IbamaDataLoader loader;

    @Test
    @DisplayName("Não deve carregar dados quando app.data.load-on-startup=false")
    void shouldNotLoadDataWhenDisabled() {
        // O profile de teste desabilita o carregamento
        // Verificamos que o banco está vazio após o startup
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("Loader deve estar configurado com loadOnStartup=false no profile de teste")
    void shouldHaveLoadOnStartupDisabled() {
        // Verificamos que a configuração está correta
        assertFalse(loader.isLoadOnStartup());
    }

    /**
     * Profile de teste que desabilita o carregamento de dados.
     */
    public static class DataLoadDisabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "app.data.load-on-startup", "false"
            );
        }
    }
}
