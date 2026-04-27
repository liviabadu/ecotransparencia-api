package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

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

    @Schema(description = "Sancoes administrativas associadas (CEIS + CNEP) - Fase B")
    private List<SancaoAdmPublicaOccurrence> sancoesAdmPublica;

    @Schema(description = "Impedimentos no CEPIM associados - Fase B")
    private List<CepimOccurrence> impedimentosCepim;

    @Schema(description = "Inclusoes na Lista Suja do MTE associadas - Fase B")
    private List<TrabalhoEscravoOccurrence> trabalhoEscravo;

    @Schema(description = "Autos de infracao do ICMBio - Fase C")
    private List<IcmbioAutoOccurrence> icmbioAutos;

    @Schema(description = "Embargos do ICMBio - Fase C")
    private List<IcmbioEmbargoOccurrence> icmbioEmbargos;

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

    public List<SancaoAdmPublicaOccurrence> getSancoesAdmPublica() {
        return sancoesAdmPublica;
    }

    public void setSancoesAdmPublica(List<SancaoAdmPublicaOccurrence> sancoesAdmPublica) {
        this.sancoesAdmPublica = sancoesAdmPublica;
    }

    public List<CepimOccurrence> getImpedimentosCepim() {
        return impedimentosCepim;
    }

    public void setImpedimentosCepim(List<CepimOccurrence> impedimentosCepim) {
        this.impedimentosCepim = impedimentosCepim;
    }

    public List<TrabalhoEscravoOccurrence> getTrabalhoEscravo() {
        return trabalhoEscravo;
    }

    public void setTrabalhoEscravo(List<TrabalhoEscravoOccurrence> trabalhoEscravo) {
        this.trabalhoEscravo = trabalhoEscravo;
    }

    public List<IcmbioAutoOccurrence> getIcmbioAutos() {
        return icmbioAutos;
    }

    public void setIcmbioAutos(List<IcmbioAutoOccurrence> icmbioAutos) {
        this.icmbioAutos = icmbioAutos;
    }

    public List<IcmbioEmbargoOccurrence> getIcmbioEmbargos() {
        return icmbioEmbargos;
    }

    public void setIcmbioEmbargos(List<IcmbioEmbargoOccurrence> icmbioEmbargos) {
        this.icmbioEmbargos = icmbioEmbargos;
    }
}
