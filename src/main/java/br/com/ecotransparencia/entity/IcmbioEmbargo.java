package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Embargo do ICMBio.
 *
 * Atributos populados a partir do XLSX (embargos_icmbio.xlsx).
 * Geometria (Polygon/MultiPolygon) populada a partir do shapefile, joined
 * por vw_num_emb. Geometria e' nullable.
 */
@Entity
@Table(
    name = "icmbio_embargo",
    indexes = {
        @Index(name = "idx_icmbio_embargo_cpf_cnpj", columnList = "cpf_cnpj")
    }
)
public class IcmbioEmbargo {

    @Id
    @Column(name = "vw_num_emb")
    private Integer vwNumEmb;

    @Column(name = "numero_emb")
    private String numeroEmb;

    @Column(name = "serie", length = 50)
    private String serie;

    @Column(name = "origem", length = 50)
    private String origem;

    @Column(name = "numero_ai")
    private String numeroAi;

    @Column(name = "cpf_cnpj", length = 20)
    private String cpfCnpj;

    @Column(name = "cpf_cnpj_formatado", length = 30)
    private String cpfCnpjFormatado;

    @Column(name = "autuado", length = 500)
    private String autuado;

    @Column(name = "desc_infra", columnDefinition = "TEXT")
    private String descInfra;

    @Column(name = "desc_sanc", columnDefinition = "TEXT")
    private String descSanc;

    @Column(name = "artigo_1", length = 100)
    private String artigo1;

    @Column(name = "artigo_2", length = 100)
    private String artigo2;

    @Column(name = "tipo_infra")
    private String tipoInfra;

    @Column(name = "nome_uc")
    private String nomeUc;

    @Column(name = "cnuc", length = 50)
    private String cnuc;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "obs", columnDefinition = "TEXT")
    private String obs;

    @Column(name = "julgamento")
    private String julgamento;

    @Column(name = "area", precision = 18, scale = 4)
    private BigDecimal area;

    @Column(name = "processo")
    private String processo;

    /** Geometria do embargo (poligono). EPSG:4674. Nao exposto em DTOs. */
    @Column(name = "geometria")
    private Geometry geometria;

    public Integer getVwNumEmb() { return vwNumEmb; }
    public void setVwNumEmb(Integer vwNumEmb) { this.vwNumEmb = vwNumEmb; }
    public String getNumeroEmb() { return numeroEmb; }
    public void setNumeroEmb(String numeroEmb) { this.numeroEmb = numeroEmb; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getNumeroAi() { return numeroAi; }
    public void setNumeroAi(String numeroAi) { this.numeroAi = numeroAi; }
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
    public String getCpfCnpjFormatado() { return cpfCnpjFormatado; }
    public void setCpfCnpjFormatado(String cpfCnpjFormatado) { this.cpfCnpjFormatado = cpfCnpjFormatado; }
    public String getAutuado() { return autuado; }
    public void setAutuado(String autuado) { this.autuado = autuado; }
    public String getDescInfra() { return descInfra; }
    public void setDescInfra(String descInfra) { this.descInfra = descInfra; }
    public String getDescSanc() { return descSanc; }
    public void setDescSanc(String descSanc) { this.descSanc = descSanc; }
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
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
    public String getJulgamento() { return julgamento; }
    public void setJulgamento(String julgamento) { this.julgamento = julgamento; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public String getProcesso() { return processo; }
    public void setProcesso(String processo) { this.processo = processo; }
    public Geometry getGeometria() { return geometria; }
    public void setGeometria(Geometry geometria) { this.geometria = geometria; }
}
