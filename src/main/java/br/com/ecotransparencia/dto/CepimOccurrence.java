package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Ocorrencia em CEPIM (Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas).
 */
@Schema(description = "Impedimento em CEPIM associado ao documento")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CepimOccurrence {

    @Schema(description = "CNPJ da entidade")
    private String cnpjEntidade;

    @Schema(description = "Nome da entidade")
    private String nomeEntidade;

    @Schema(description = "Numero do convenio")
    private String numeroConvenio;

    @Schema(description = "Orgao concedente")
    private String orgaoConcedente;

    @Schema(description = "Motivo do impedimento")
    private String motivoImpedimento;

    public String getCnpjEntidade() {
        return cnpjEntidade;
    }

    public void setCnpjEntidade(String cnpjEntidade) {
        this.cnpjEntidade = cnpjEntidade;
    }

    public String getNomeEntidade() {
        return nomeEntidade;
    }

    public void setNomeEntidade(String nomeEntidade) {
        this.nomeEntidade = nomeEntidade;
    }

    public String getNumeroConvenio() {
        return numeroConvenio;
    }

    public void setNumeroConvenio(String numeroConvenio) {
        this.numeroConvenio = numeroConvenio;
    }

    public String getOrgaoConcedente() {
        return orgaoConcedente;
    }

    public void setOrgaoConcedente(String orgaoConcedente) {
        this.orgaoConcedente = orgaoConcedente;
    }

    public String getMotivoImpedimento() {
        return motivoImpedimento;
    }

    public void setMotivoImpedimento(String motivoImpedimento) {
        this.motivoImpedimento = motivoImpedimento;
    }
}
