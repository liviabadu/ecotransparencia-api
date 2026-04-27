package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para auto de infracao do ICMBio.
 * Geometria e' deliberadamente omitida (server-side only).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IcmbioAutoOccurrence {
    private String numeroAi;
    private String tipo;
    private BigDecimal valorMulta;
    private String autuado;
    private String descAi;
    private LocalDate data;
    private Integer ano;
    private String tipoInfra;
    private String nomeUc;
    private String municipio;
    private String uf;
    private String processo;
    private String julgamento;

    public String getNumeroAi() { return numeroAi; }
    public void setNumeroAi(String numeroAi) { this.numeroAi = numeroAi; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(BigDecimal valorMulta) { this.valorMulta = valorMulta; }
    public String getAutuado() { return autuado; }
    public void setAutuado(String autuado) { this.autuado = autuado; }
    public String getDescAi() { return descAi; }
    public void setDescAi(String descAi) { this.descAi = descAi; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getTipoInfra() { return tipoInfra; }
    public void setTipoInfra(String tipoInfra) { this.tipoInfra = tipoInfra; }
    public String getNomeUc() { return nomeUc; }
    public void setNomeUc(String nomeUc) { this.nomeUc = nomeUc; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getProcesso() { return processo; }
    public void setProcesso(String processo) { this.processo = processo; }
    public String getJulgamento() { return julgamento; }
    public void setJulgamento(String julgamento) { this.julgamento = julgamento; }
}
