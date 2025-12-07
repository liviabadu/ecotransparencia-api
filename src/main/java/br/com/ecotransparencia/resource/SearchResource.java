package br.com.ecotransparencia.resource;

import br.com.ecotransparencia.dto.SearchResponse;
import br.com.ecotransparencia.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    @Path("/document")
    public SearchResponse searchByDocument(
            @QueryParam("document") String document,
            @QueryParam("type") String type) {
        return searchService.searchByDocument(document, type);
    }

    @GET
    @Path("/name")
    public SearchResponse searchByName(@QueryParam("name") String name) {
        // Will be implemented in US-003
        return SearchResponse.notFound();
    }
}
