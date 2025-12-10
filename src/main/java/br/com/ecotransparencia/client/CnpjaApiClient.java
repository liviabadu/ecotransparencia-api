package br.com.ecotransparencia.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST Client para CNPJA API Open - Consulta de CNPJ.
 * Documentacao: https://cnpja.com/api/open
 *
 * Exemplo de uso:
 * curl --request GET --url 'https://open.cnpja.com/office/07526557011659'
 */
@RegisterRestClient(configKey = "cnpja-api")
@Path("/office")
public interface CnpjaApiClient {

    /**
     * Consulta dados de um CNPJ na base da Receita Federal via CNPJA API Open.
     *
     * @param cnpj CNPJ a ser consultado (14 digitos, apenas numeros)
     * @return dados do CNPJ incluindo situacao cadastral (status.id = 2 significa "Ativa")
     */
    @GET
    @Path("/{cnpj}")
    @Produces(MediaType.APPLICATION_JSON)
    CnpjaApiResponse consultarCnpj(@PathParam("cnpj") String cnpj);
}
