package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para embargo do ICMBio.
 * Geometria e' deliberadamente omitida (server-side only).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IcmbioEmbargoOccurrence {
    private String numeroEmb;
    private String numeroAi;
    private String autuado;
    private String descInfra;
    private String descSanc;
    private String tipoInfra;
    private String nomeUc;
    private String municipio;
    private String uf;
    private LocalDate data;
    private Integer ano;
    private BigDecimal area;
    private String processo;
    private String julgamento;

    public String getNumeroEmb() { return numeroEmb; }
    public void setNumeroEmb(String numeroEmb) { this.numeroEmb = numeroEmb; }
    public String getNumeroAi() { return numeroAi; }
    public void setNumeroAi(String numeroAi) { this.numeroAi = numeroAi; }
    public String getAutuado() { return autuado; }
    public void setAutuado(String autuado) { this.autuado = autuado; }
    public String getDescInfra() { return descInfra; }
    public void setDescInfra(String descInfra) { this.descInfra = descInfra; }
    public String getDescSanc() { return descSanc; }
    public void setDescSanc(String descSanc) { this.descSanc = descSanc; }
    public String getTipoInfra() { return tipoInfra; }
    public void setTipoInfra(String tipoInfra) { this.tipoInfra = tipoInfra; }
    public String getNomeUc() { return nomeUc; }
    public void setNomeUc(String nomeUc) { this.nomeUc = nomeUc; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public String getProcesso() { return processo; }
    public void setProcesso(String processo) { this.processo = processo; }
    public String getJulgamento() { return julgamento; }
    public void setJulgamento(String julgamento) { this.julgamento = julgamento; }
}
