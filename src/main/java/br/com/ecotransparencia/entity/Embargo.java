package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "embargo")
public class Embargo {

    @Id
    @Column(name = "seq_tad")
    private Long seqTad;

    @Column(name = "num_tad")
    private String numTad;

    @Column(name = "ser_tad")
    private String serTad;

    @Column(name = "dat_embargo")
    private LocalDateTime datEmbargo;

    @Column(name = "dat_ult_alteracao")
    private LocalDateTime datUltAlteracao;

    @Column(name = "sig_uf_tad")
    private String sigUfTad;

    @Column(name = "nom_municipio_tad")
    private String nomMunicipioTad;

    @Column(name = "num_longitude_tad")
    private BigDecimal numLongitudeTad;

    @Column(name = "num_latitude_tad")
    private BigDecimal numLatitudeTad;

    @Column(name = "nome_imovel")
    private String nomeImovel;

    @Column(name = "des_localizacao_tad", length = 2000)
    private String desLocalizacaoTad;

    @Column(name = "nome_pessoa_embargada")
    private String nomePessoaEmbargada;

    @Column(name = "cpf_cnpj_embargado")
    private String cpfCnpjEmbargado;

    @Column(name = "sit_desmatamento")
    private String sitDesmatamento;

    @Column(name = "tp_area_embargada")
    private String tpAreaEmbargada;

    @Column(name = "qtd_area_embargada")
    private BigDecimal qtdAreaEmbargada;

    @Column(name = "operacao")
    private String operacao;

    @Column(name = "unid_ibama_controle")
    private String unidIbamaControle;

    @Column(name = "num_processo")
    private String numProcesso;

    @Column(name = "des_tad", length = 4000)
    private String desTad;

    @Column(name = "num_auto_infracao")
    private String numAutoInfracao;

    @Column(name = "ser_auto_infracao")
    private String serAutoInfracao;

    @Column(name = "qtd_area_desmatada")
    private BigDecimal qtdAreaDesmatada;

    @Column(name = "des_infracao", length = 4000)
    private String desInfracao;

    @Column(name = "cod_tipo_bioma")
    private Integer codTipoBioma;

    @Column(name = "des_tipo_bioma")
    private String desTipoBioma;

    @Column(name = "ind_baixado")
    private String indBaixado;

    // Getters and Setters

    public Long getSeqTad() {
        return seqTad;
    }

    public void setSeqTad(Long seqTad) {
        this.seqTad = seqTad;
    }

    public String getNumTad() {
        return numTad;
    }

    public void setNumTad(String numTad) {
        this.numTad = numTad;
    }

    public String getSerTad() {
        return serTad;
    }

    public void setSerTad(String serTad) {
        this.serTad = serTad;
    }

    public LocalDateTime getDatEmbargo() {
        return datEmbargo;
    }

    public void setDatEmbargo(LocalDateTime datEmbargo) {
        this.datEmbargo = datEmbargo;
    }

    public LocalDateTime getDatUltAlteracao() {
        return datUltAlteracao;
    }

    public void setDatUltAlteracao(LocalDateTime datUltAlteracao) {
        this.datUltAlteracao = datUltAlteracao;
    }

    public String getSigUfTad() {
        return sigUfTad;
    }

    public void setSigUfTad(String sigUfTad) {
        this.sigUfTad = sigUfTad;
    }

    public String getNomMunicipioTad() {
        return nomMunicipioTad;
    }

    public void setNomMunicipioTad(String nomMunicipioTad) {
        this.nomMunicipioTad = nomMunicipioTad;
    }

    public BigDecimal getNumLongitudeTad() {
        return numLongitudeTad;
    }

    public void setNumLongitudeTad(BigDecimal numLongitudeTad) {
        this.numLongitudeTad = numLongitudeTad;
    }

    public BigDecimal getNumLatitudeTad() {
        return numLatitudeTad;
    }

    public void setNumLatitudeTad(BigDecimal numLatitudeTad) {
        this.numLatitudeTad = numLatitudeTad;
    }

    public String getNomeImovel() {
        return nomeImovel;
    }

    public void setNomeImovel(String nomeImovel) {
        this.nomeImovel = nomeImovel;
    }

    public String getDesLocalizacaoTad() {
        return desLocalizacaoTad;
    }

    public void setDesLocalizacaoTad(String desLocalizacaoTad) {
        this.desLocalizacaoTad = desLocalizacaoTad;
    }

    public String getNomePessoaEmbargada() {
        return nomePessoaEmbargada;
    }

    public void setNomePessoaEmbargada(String nomePessoaEmbargada) {
        this.nomePessoaEmbargada = nomePessoaEmbargada;
    }

    public String getCpfCnpjEmbargado() {
        return cpfCnpjEmbargado;
    }

    public void setCpfCnpjEmbargado(String cpfCnpjEmbargado) {
        this.cpfCnpjEmbargado = cpfCnpjEmbargado;
    }

    public String getSitDesmatamento() {
        return sitDesmatamento;
    }

    public void setSitDesmatamento(String sitDesmatamento) {
        this.sitDesmatamento = sitDesmatamento;
    }

    public String getTpAreaEmbargada() {
        return tpAreaEmbargada;
    }

    public void setTpAreaEmbargada(String tpAreaEmbargada) {
        this.tpAreaEmbargada = tpAreaEmbargada;
    }

    public BigDecimal getQtdAreaEmbargada() {
        return qtdAreaEmbargada;
    }

    public void setQtdAreaEmbargada(BigDecimal qtdAreaEmbargada) {
        this.qtdAreaEmbargada = qtdAreaEmbargada;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getUnidIbamaControle() {
        return unidIbamaControle;
    }

    public void setUnidIbamaControle(String unidIbamaControle) {
        this.unidIbamaControle = unidIbamaControle;
    }

    public String getNumProcesso() {
        return numProcesso;
    }

    public void setNumProcesso(String numProcesso) {
        this.numProcesso = numProcesso;
    }

    public String getDesTad() {
        return desTad;
    }

    public void setDesTad(String desTad) {
        this.desTad = desTad;
    }

    public String getNumAutoInfracao() {
        return numAutoInfracao;
    }

    public void setNumAutoInfracao(String numAutoInfracao) {
        this.numAutoInfracao = numAutoInfracao;
    }

    public String getSerAutoInfracao() {
        return serAutoInfracao;
    }

    public void setSerAutoInfracao(String serAutoInfracao) {
        this.serAutoInfracao = serAutoInfracao;
    }

    public BigDecimal getQtdAreaDesmatada() {
        return qtdAreaDesmatada;
    }

    public void setQtdAreaDesmatada(BigDecimal qtdAreaDesmatada) {
        this.qtdAreaDesmatada = qtdAreaDesmatada;
    }

    public String getDesInfracao() {
        return desInfracao;
    }

    public void setDesInfracao(String desInfracao) {
        this.desInfracao = desInfracao;
    }

    public Integer getCodTipoBioma() {
        return codTipoBioma;
    }

    public void setCodTipoBioma(Integer codTipoBioma) {
        this.codTipoBioma = codTipoBioma;
    }

    public String getDesTipoBioma() {
        return desTipoBioma;
    }

    public void setDesTipoBioma(String desTipoBioma) {
        this.desTipoBioma = desTipoBioma;
    }

    public String getIndBaixado() {
        return indBaixado;
    }

    public void setIndBaixado(String indBaixado) {
        this.indBaixado = indBaixado;
    }

    /**
     * Verifica se o embargo foi baixado.
     * @return true se indBaixado é "S", false caso contrário
     */
    public boolean isBaixado() {
        return "S".equalsIgnoreCase(indBaixado);
    }
}
