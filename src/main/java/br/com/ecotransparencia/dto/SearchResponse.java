package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    private boolean found;
    private EntityDto entity;

    public SearchResponse() {
    }

    public SearchResponse(boolean found) {
        this.found = found;
    }

    public SearchResponse(boolean found, EntityDto entity) {
        this.found = found;
        this.entity = entity;
    }

    public static SearchResponse notFound() {
        return new SearchResponse(false);
    }

    public static SearchResponse found(EntityDto entity) {
        return new SearchResponse(true, entity);
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public EntityDto getEntity() {
        return entity;
    }

    public void setEntity(EntityDto entity) {
        this.entity = entity;
    }
}
