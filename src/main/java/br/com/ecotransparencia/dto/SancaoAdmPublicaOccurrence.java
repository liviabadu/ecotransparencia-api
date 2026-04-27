package br.com.ecotransparencia.dto;

import br.com.ecotransparencia.domain.CadastroSancao;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ocorrencia em CEIS ou CNEP (sancao administrativa).
 *
 * Tipo discriminado por {@link #cadastro}; {@link #valorMulta} e' nullable
 * (apenas CNEP).
 */
@Schema(description = "Sancao administrativa (CEIS ou CNEP) associada ao documento")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SancaoAdmPublicaOccurrence {

    @Schema(description = "Cadastro de origem", example = "CEIS")
    private CadastroSancao cadastro;

    @Schema(description = "Codigo da sancao", example = "305206")
    private String codigoSancao;

    @Schema(description = "Nome do sancionado")
    private String nomeSancionado;

    @Schema(description = "Categoria da sancao")
    private String categoriaSancao;

    @Schema(description = "Valor da multa em reais (apenas CNEP)", nullable = true)
    private BigDecimal valorMulta;

    @Schema(description = "Data de inicio da sancao", example = "2024-04-26")
    private LocalDate dataInicioSancao;

    @Schema(description = "Data final da sancao", nullable = true)
    private LocalDate dataFimSancao;

    @Schema(description = "Orgao sancionador")
    private String orgaoSancionador;

    @Schema(description = "UF do orgao sancionador")
    private String ufOrgao;

    @Schema(description = "Esfera do orgao (FEDERAL/ESTADUAL/MUNICIPAL)")
    private String esferaOrgao;

    @Schema(description = "Fundamentacao legal da sancao")
    private String fundamentacaoLegal;

    public CadastroSancao getCadastro() {
        return cadastro;
    }

    public void setCadastro(CadastroSancao cadastro) {
        this.cadastro = cadastro;
    }

    public String getCodigoSancao() {
        return codigoSancao;
    }

    public void setCodigoSancao(String codigoSancao) {
        this.codigoSancao = codigoSancao;
    }

    public String getNomeSancionado() {
        return nomeSancionado;
    }

    public void setNomeSancionado(String nomeSancionado) {
        this.nomeSancionado = nomeSancionado;
    }

    public String getCategoriaSancao() {
        return categoriaSancao;
    }

    public void setCategoriaSancao(String categoriaSancao) {
        this.categoriaSancao = categoriaSancao;
    }

    public BigDecimal getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(BigDecimal valorMulta) {
        this.valorMulta = valorMulta;
    }

    public LocalDate getDataInicioSancao() {
        return dataInicioSancao;
    }

    public void setDataInicioSancao(LocalDate dataInicioSancao) {
        this.dataInicioSancao = dataInicioSancao;
    }

    public LocalDate getDataFimSancao() {
        return dataFimSancao;
    }

    public void setDataFimSancao(LocalDate dataFimSancao) {
        this.dataFimSancao = dataFimSancao;
    }

    public String getOrgaoSancionador() {
        return orgaoSancionador;
    }

    public void setOrgaoSancionador(String orgaoSancionador) {
        this.orgaoSancionador = orgaoSancionador;
    }

    public String getUfOrgao() {
        return ufOrgao;
    }

    public void setUfOrgao(String ufOrgao) {
        this.ufOrgao = ufOrgao;
    }

    public String getEsferaOrgao() {
        return esferaOrgao;
    }

    public void setEsferaOrgao(String esferaOrgao) {
        this.esferaOrgao = esferaOrgao;
    }

    public String getFundamentacaoLegal() {
        return fundamentacaoLegal;
    }

    public void setFundamentacaoLegal(String fundamentacaoLegal) {
        this.fundamentacaoLegal = fundamentacaoLegal;
    }
}
