package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resposta da busca de entidades")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    @Schema(description = "Indica se a entidade foi encontrada", example = "true")
    private boolean found;

    @Schema(description = "Indica se a analise foi bloqueada devido a situacao cadastral do CNPJ (US-007)", example = "false")
    private Boolean bloqueadoPorSituacaoCadastral;

    @Schema(description = "Situacao cadastral do CNPJ na Receita Federal (presente quando bloqueado)")
    private SituacaoCadastralDto situacaoCadastral;

    @Schema(description = "Dados da entidade encontrada (null se found=false ou bloqueado)")
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

    /**
     * Cria resposta de bloqueio por situacao cadastral do CNPJ (US-007).
     * Usado quando o CNPJ nao esta ATIVO na Receita Federal.
     */
    public static SearchResponse bloqueadoPorSituacaoCadastral(SituacaoCadastralDto situacao) {
        SearchResponse response = new SearchResponse(false);
        response.setBloqueadoPorSituacaoCadastral(true);
        response.setSituacaoCadastral(situacao);
        return response;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public Boolean getBloqueadoPorSituacaoCadastral() {
        return bloqueadoPorSituacaoCadastral;
    }

    public void setBloqueadoPorSituacaoCadastral(Boolean bloqueadoPorSituacaoCadastral) {
        this.bloqueadoPorSituacaoCadastral = bloqueadoPorSituacaoCadastral;
    }

    public SituacaoCadastralDto getSituacaoCadastral() {
        return situacaoCadastral;
    }

    public void setSituacaoCadastral(SituacaoCadastralDto situacaoCadastral) {
        this.situacaoCadastral = situacaoCadastral;
    }

    public EntityDto getEntity() {
        return entity;
    }

    public void setEntity(EntityDto entity) {
        this.entity = entity;
    }
}
