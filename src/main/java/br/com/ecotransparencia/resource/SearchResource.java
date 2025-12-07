package br.com.ecotransparencia.resource;

import br.com.ecotransparencia.dto.SearchResponse;
import br.com.ecotransparencia.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Search", description = "Endpoints para busca de entidades com areas embargadas pelo IBAMA")
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    @Path("/document")
    @Operation(
            summary = "Buscar entidade por documento",
            description = "Busca uma entidade (pessoa fisica ou juridica) pelo CPF ou CNPJ. " +
                    "Retorna informacoes da entidade, nivel de risco e lista de embargos associados."
    )
    @APIResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso. Retorna found=true se encontrado, found=false caso contrario.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchResponse.class)
            )
    )
    public SearchResponse searchByDocument(
            @Parameter(
                    name = "document",
                    description = "CPF (11 digitos) ou CNPJ (14 digitos) da entidade",
                    required = true,
                    example = "29138369000147"
            )
            @QueryParam("document") String document,
            @Parameter(
                    name = "type",
                    description = "Tipo do documento: 'cpf' ou 'cnpj'",
                    required = true,
                    example = "cnpj"
            )
            @QueryParam("type") String type) {
        return searchService.searchByDocument(document, type);
    }

    @GET
    @Path("/name")
    @Operation(
            summary = "Buscar entidades por nome",
            description = "Busca entidades pelo nome (parcial ou completo). " +
                    "A busca e case-insensitive e retorna todas as entidades cujo nome contenha o termo informado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso. Retorna found=true se encontrado, found=false caso contrario.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchResponse.class)
            )
    )
    public SearchResponse searchByName(
            @Parameter(
                    name = "name",
                    description = "Nome ou parte do nome da entidade a ser buscada",
                    required = true,
                    example = "PREFEITURA"
            )
            @QueryParam("name") String name) {
        return searchService.searchByName(name);
    }
}
