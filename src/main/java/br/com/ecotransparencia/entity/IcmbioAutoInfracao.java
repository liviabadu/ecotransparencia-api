package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Auto de infracao do ICMBio.
 *
 * Atributos populados a partir do XLSX (autos_infracao_icmbio.xlsx).
 * Geometria (Point) populada a partir do shapefile, joined por vw_num_auto.
 * Geometria e' nullable: linhas do XLSX sem match no SHP ficam sem ponto.
 */
@Entity
@Table(
    name = "icmbio_auto_infracao",
    indexes = {
        @Index(name = "idx_icmbio_auto_cpf_cnpj", columnList = "cpf_cnpj")
    }
)
public class IcmbioAutoInfracao {

    @Id
    @Column(name = "vw_num_auto")
    private Integer vwNumAuto;

    @Column(name = "numero_ai", columnDefinition = "TEXT")
    private String numeroAi;

    @Column(name = "serie", length = 50)
    private String serie;

    @Column(name = "origem", length = 50)
    private String origem;

    @Column(name = "tipo", columnDefinition = "TEXT")
    private String tipo;

    @Column(name = "valor_multa", columnDefinition = "NUMERIC")
    private BigDecimal valorMulta;

    @Column(name = "embargo", length = 50)
    private String embargo;

    @Column(name = "apreensao", length = 50)
    private String apreensao;

    @Column(name = "autuado", columnDefinition = "TEXT")
    private String autuado;

    /** Documento apenas com digitos (indexado, base de busca). */
    @Column(name = "cpf_cnpj", length = 20)
    private String cpfCnpj;

    /** Documento como veio na fonte (preserva formatacao). */
    @Column(name = "cpf_cnpj_formatado", length = 30)
    private String cpfCnpjFormatado;

    @Column(name = "desc_ai", columnDefinition = "TEXT")
    private String descAi;

    @Column(name = "desc_sanc", columnDefinition = "TEXT")
    private String descSanc;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "artigo_1", length = 100)
    private String artigo1;

    @Column(name = "artigo_2", length = 100)
    private String artigo2;

    @Column(name = "tipo_infra", columnDefinition = "TEXT")
    private String tipoInfra;

    @Column(name = "nome_uc", columnDefinition = "TEXT")
    private String nomeUc;

    @Column(name = "cnuc", length = 50)
    private String cnuc;

    @Column(name = "municipio", columnDefinition = "TEXT")
    private String municipio;

    @Column(name = "uf", length = 8)
    private String uf;

    @Column(name = "termos_emb", columnDefinition = "TEXT")
    private String termosEmb;

    @Column(name = "termos_apr", columnDefinition = "TEXT")
    private String termosApr;

    @Column(name = "ordem_fisc", columnDefinition = "TEXT")
    private String ordemFisc;

    @Column(name = "processo", columnDefinition = "TEXT")
    private String processo;

    @Column(name = "julgamento", columnDefinition = "TEXT")
    private String julgamento;

    /** Geometria do auto (ponto). EPSG:4674 (SIRGAS 2000). Nao exposto em DTOs. */
    @Column(name = "localizacao")
    private Point localizacao;

    public Integer getVwNumAuto() { return vwNumAuto; }
    public void setVwNumAuto(Integer vwNumAuto) { this.vwNumAuto = vwNumAuto; }
    public String getNumeroAi() { return numeroAi; }
    public void setNumeroAi(String numeroAi) { this.numeroAi = numeroAi; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(BigDecimal valorMulta) { this.valorMulta = valorMulta; }
    public String getEmbargo() { return embargo; }
    public void setEmbargo(String embargo) { this.embargo = embargo; }
    public String getApreensao() { return apreensao; }
    public void setApreensao(String apreensao) { this.apreensao = apreensao; }
    public String getAutuado() { return autuado; }
    public void setAutuado(String autuado) { this.autuado = autuado; }
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
    public String getCpfCnpjFormatado() { return cpfCnpjFormatado; }
    public void setCpfCnpjFormatado(String cpfCnpjFormatado) { this.cpfCnpjFormatado = cpfCnpjFormatado; }
    public String getDescAi() { return descAi; }
    public void setDescAi(String descAi) { this.descAi = descAi; }
    public String getDescSanc() { return descSanc; }
    public void setDescSanc(String descSanc) { this.descSanc = descSanc; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getArtigo1() { return artigo1; }
    public void setArtigo1(String artigo1) { this.artigo1 = artigo1; }
    public String getArtigo2() { return artigo2; }
    public void setArtigo2(String artigo2) { this.artigo2 = artigo2; }
    public String getTipoInfra() { return tipoInfra; }
    public void setTipoInfra(String tipoInfra) { this.tipoInfra = tipoInfra; }
    public String getNomeUc() { return nomeUc; }
    public void setNomeUc(String nomeUc) { this.nomeUc = nomeUc; }
    public String getCnuc() { return cnuc; }
    public void setCnuc(String cnuc) { this.cnuc = cnuc; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getTermosEmb() { return termosEmb; }
    public void setTermosEmb(String termosEmb) { this.termosEmb = termosEmb; }
    public String getTermosApr() { return termosApr; }
    public void setTermosApr(String termosApr) { this.termosApr = termosApr; }
    public String getOrdemFisc() { return ordemFisc; }
    public void setOrdemFisc(String ordemFisc) { this.ordemFisc = ordemFisc; }
    public String getProcesso() { return processo; }
    public void setProcesso(String processo) { this.processo = processo; }
    public String getJulgamento() { return julgamento; }
    public void setJulgamento(String julgamento) { this.julgamento = julgamento; }
    public Point getLocalizacao() { return localizacao; }
    public void setLocalizacao(Point localizacao) { this.localizacao = localizacao; }
}
