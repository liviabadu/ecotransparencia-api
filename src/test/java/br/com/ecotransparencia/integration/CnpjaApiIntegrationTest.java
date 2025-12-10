package br.com.ecotransparencia.integration;

import br.com.ecotransparencia.client.CnpjaApiClient;
import br.com.ecotransparencia.client.CnpjaApiResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integracao com a API CNPJA Open.
 *
 * Para executar apenas este teste:
 * ./mvnw test -Dtest=CnpjaApiIntegrationTest
 */
@QuarkusTest
@Tag("integration")
class CnpjaApiIntegrationTest {

    // CNPJ com situacao BAIXADA (presente no arquivo areas_embargadas.csv)
    private static final String CNPJ_BAIXADO = "75776849000150";

    // CNPJ com situacao ATIVA
    private static final String CNPJ_ATIVO = "00000000000191";

    @Inject
    @RestClient
    CnpjaApiClient cnpjaApiClient;

    @Test
    @DisplayName("Deve retornar situacao BAIXADA para CNPJ 75776849000150")
    void shouldReturnBaixadaForInactiveCnpj() {
        // When
        CnpjaApiResponse response = cnpjaApiClient.consultarCnpj(CNPJ_BAIXADO);

        // Then
        assertNotNull(response);
        assertEquals(CNPJ_BAIXADO, response.getTaxId());
        assertFalse(response.isAtiva(), "CNPJ deve estar inativo (BAIXADA)");
        assertEquals("Baixada", response.getDescricaoSituacaoCadastral());

        System.out.println("=== CNPJ BAIXADO ===");
        System.out.println("CNPJ: " + response.getTaxId());
        System.out.println("Razao Social: " + response.getRazaoSocial());
        System.out.println("Situacao: " + response.getDescricaoSituacaoCadastral());
        System.out.println("isAtiva: " + response.isAtiva());
    }

    @Test
    @DisplayName("Deve retornar situacao ATIVA para CNPJ 00000000000191")
    void shouldReturnAtivaForActiveCnpj() {
        // When
        CnpjaApiResponse response = cnpjaApiClient.consultarCnpj(CNPJ_ATIVO);

        // Then
        assertNotNull(response);
        assertEquals(CNPJ_ATIVO, response.getTaxId());
        assertTrue(response.isAtiva(), "CNPJ deve estar ativo");
        assertEquals("Ativa", response.getDescricaoSituacaoCadastral());

        System.out.println("=== CNPJ ATIVO ===");
        System.out.println("CNPJ: " + response.getTaxId());
        System.out.println("Razao Social: " + response.getRazaoSocial());
        System.out.println("Situacao: " + response.getDescricaoSituacaoCadastral());
        System.out.println("isAtiva: " + response.isAtiva());
    }
}
