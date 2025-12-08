package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um Auto de Infracao do IBAMA.
 * Mapeada a partir dos arquivos CSV de autos de infracao por ano.
 */
@Entity
@Table(name = "auto_infracao")
public class AutoInfracao {

    @Id
    @Column(name = "seq_auto_infracao")
    private Long seqAutoInfracao;

    @Column(name = "num_auto_infracao")
    private String numAutoInfracao;

    @Column(name = "ser_auto_infracao")
    private String serAutoInfracao;

    @Column(name = "des_status_formulario")
    private String statusFormulario;

    @Column(name = "ds_sit_auto_aie")
    private String situacaoAuto;

    @Column(name = "sit_cancelado")
    private String situacaoCancelado;

    @Column(name = "tipo_auto")
    private String tipoAuto;

    @Column(name = "tipo_multa")
    private String tipoMulta;

    @Column(name = "val_auto_infracao")
    private BigDecimal valorAutoInfracao;

    @Column(name = "fundamentacao_multa", length = 4000)
    private String fundamentacaoMulta;

    @Column(name = "gravidade_infracao")
    private String gravidadeInfracao;

    @Column(name = "cd_nivel_gravidade")
    private String codigoNivelGravidade;

    @Column(name = "motivacao_conduta")
    private String motivacaoConduta;

    @Column(name = "efeito_meio_ambiente")
    private String efeitoMeioAmbiente;

    @Column(name = "efeito_saude_publica")
    private String efeitoSaudePublica;

    @Column(name = "passivel_recuperacao")
    private String passivelRecuperacao;

    @Column(name = "des_auto_infracao", length = 4000)
    private String descricaoAutoInfracao;

    @Column(name = "dat_hora_auto_infracao")
    private LocalDateTime dataHoraAutoInfracao;

    @Column(name = "dat_ciencia_autuacao")
    private LocalDateTime dataCienciaAutuacao;

    @Column(name = "dt_fato_infracional")
    private LocalDateTime dataFatoInfracional;

    @Column(name = "cod_municipio")
    private Integer codigoMunicipio;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "uf")
    private String uf;

    @Column(name = "num_processo")
    private String numeroProcesso;

    @Column(name = "cod_infracao")
    private Integer codigoInfracao;

    @Column(name = "des_infracao", length = 500)
    private String descricaoInfracao;

    @Column(name = "tipo_infracao")
    private String tipoInfracao;

    @Column(name = "tp_pessoa_infrator")
    private String tipoPessoaInfrator;

    @Column(name = "nome_infrator")
    private String nomeInfrator;

    @Column(name = "cpf_cnpj_infrator")
    private String cpfCnpjInfrator;

    @Column(name = "qt_area")
    private BigDecimal quantidadeArea;

    @Column(name = "infracao_area")
    private String infracaoArea;

    @Column(name = "classificacao_area")
    private String classificacaoArea;

    @Column(name = "num_longitude")
    private BigDecimal longitude;

    @Column(name = "num_latitude")
    private BigDecimal latitude;

    @Column(name = "des_local_infracao", length = 2000)
    private String descricaoLocalInfracao;

    @Column(name = "ds_biomas_atingidos")
    private String biomasAtingidos;

    @Column(name = "operacao")
    private String operacao;

    @Column(name = "dt_lancamento")
    private LocalDateTime dataLancamento;

    @Column(name = "dt_ult_alteracao")
    private LocalDateTime dataUltimaAlteracao;

    @Column(name = "ds_enquadramento_administrativo", length = 500)
    private String enquadramentoAdministrativo;

    @Column(name = "ultima_atualizacao_relatorio")
    private LocalDateTime ultimaAtualizacaoRelatorio;

    // Getters e Setters

    public Long getSeqAutoInfracao() {
        return seqAutoInfracao;
    }

    public void setSeqAutoInfracao(Long seqAutoInfracao) {
        this.seqAutoInfracao = seqAutoInfracao;
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

    public String getStatusFormulario() {
        return statusFormulario;
    }

    public void setStatusFormulario(String statusFormulario) {
        this.statusFormulario = statusFormulario;
    }

    public String getSituacaoAuto() {
        return situacaoAuto;
    }

    public void setSituacaoAuto(String situacaoAuto) {
        this.situacaoAuto = situacaoAuto;
    }

    public String getSituacaoCancelado() {
        return situacaoCancelado;
    }

    public void setSituacaoCancelado(String situacaoCancelado) {
        this.situacaoCancelado = situacaoCancelado;
    }

    public String getTipoAuto() {
        return tipoAuto;
    }

    public void setTipoAuto(String tipoAuto) {
        this.tipoAuto = tipoAuto;
    }

    public String getTipoMulta() {
        return tipoMulta;
    }

    public void setTipoMulta(String tipoMulta) {
        this.tipoMulta = tipoMulta;
    }

    public BigDecimal getValorAutoInfracao() {
        return valorAutoInfracao;
    }

    public void setValorAutoInfracao(BigDecimal valorAutoInfracao) {
        this.valorAutoInfracao = valorAutoInfracao;
    }

    public String getFundamentacaoMulta() {
        return fundamentacaoMulta;
    }

    public void setFundamentacaoMulta(String fundamentacaoMulta) {
        this.fundamentacaoMulta = fundamentacaoMulta;
    }

    public String getGravidadeInfracao() {
        return gravidadeInfracao;
    }

    public void setGravidadeInfracao(String gravidadeInfracao) {
        this.gravidadeInfracao = gravidadeInfracao;
    }

    public String getCodigoNivelGravidade() {
        return codigoNivelGravidade;
    }

    public void setCodigoNivelGravidade(String codigoNivelGravidade) {
        this.codigoNivelGravidade = codigoNivelGravidade;
    }

    public String getMotivacaoConduta() {
        return motivacaoConduta;
    }

    public void setMotivacaoConduta(String motivacaoConduta) {
        this.motivacaoConduta = motivacaoConduta;
    }

    public String getEfeitoMeioAmbiente() {
        return efeitoMeioAmbiente;
    }

    public void setEfeitoMeioAmbiente(String efeitoMeioAmbiente) {
        this.efeitoMeioAmbiente = efeitoMeioAmbiente;
    }

    public String getEfeitoSaudePublica() {
        return efeitoSaudePublica;
    }

    public void setEfeitoSaudePublica(String efeitoSaudePublica) {
        this.efeitoSaudePublica = efeitoSaudePublica;
    }

    public String getPassivelRecuperacao() {
        return passivelRecuperacao;
    }

    public void setPassivelRecuperacao(String passivelRecuperacao) {
        this.passivelRecuperacao = passivelRecuperacao;
    }

    public String getDescricaoAutoInfracao() {
        return descricaoAutoInfracao;
    }

    public void setDescricaoAutoInfracao(String descricaoAutoInfracao) {
        this.descricaoAutoInfracao = descricaoAutoInfracao;
    }

    public LocalDateTime getDataHoraAutoInfracao() {
        return dataHoraAutoInfracao;
    }

    public void setDataHoraAutoInfracao(LocalDateTime dataHoraAutoInfracao) {
        this.dataHoraAutoInfracao = dataHoraAutoInfracao;
    }

    public LocalDateTime getDataCienciaAutuacao() {
        return dataCienciaAutuacao;
    }

    public void setDataCienciaAutuacao(LocalDateTime dataCienciaAutuacao) {
        this.dataCienciaAutuacao = dataCienciaAutuacao;
    }

    public LocalDateTime getDataFatoInfracional() {
        return dataFatoInfracional;
    }

    public void setDataFatoInfracional(LocalDateTime dataFatoInfracional) {
        this.dataFatoInfracional = dataFatoInfracional;
    }

    public Integer getCodigoMunicipio() {
        return codigoMunicipio;
    }

    public void setCodigoMunicipio(Integer codigoMunicipio) {
        this.codigoMunicipio = codigoMunicipio;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public Integer getCodigoInfracao() {
        return codigoInfracao;
    }

    public void setCodigoInfracao(Integer codigoInfracao) {
        this.codigoInfracao = codigoInfracao;
    }

    public String getDescricaoInfracao() {
        return descricaoInfracao;
    }

    public void setDescricaoInfracao(String descricaoInfracao) {
        this.descricaoInfracao = descricaoInfracao;
    }

    public String getTipoInfracao() {
        return tipoInfracao;
    }

    public void setTipoInfracao(String tipoInfracao) {
        this.tipoInfracao = tipoInfracao;
    }

    public String getTipoPessoaInfrator() {
        return tipoPessoaInfrator;
    }

    public void setTipoPessoaInfrator(String tipoPessoaInfrator) {
        this.tipoPessoaInfrator = tipoPessoaInfrator;
    }

    public String getNomeInfrator() {
        return nomeInfrator;
    }

    public void setNomeInfrator(String nomeInfrator) {
        this.nomeInfrator = nomeInfrator;
    }

    public String getCpfCnpjInfrator() {
        return cpfCnpjInfrator;
    }

    public void setCpfCnpjInfrator(String cpfCnpjInfrator) {
        this.cpfCnpjInfrator = cpfCnpjInfrator;
    }

    public BigDecimal getQuantidadeArea() {
        return quantidadeArea;
    }

    public void setQuantidadeArea(BigDecimal quantidadeArea) {
        this.quantidadeArea = quantidadeArea;
    }

    public String getInfracaoArea() {
        return infracaoArea;
    }

    public void setInfracaoArea(String infracaoArea) {
        this.infracaoArea = infracaoArea;
    }

    public String getClassificacaoArea() {
        return classificacaoArea;
    }

    public void setClassificacaoArea(String classificacaoArea) {
        this.classificacaoArea = classificacaoArea;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getDescricaoLocalInfracao() {
        return descricaoLocalInfracao;
    }

    public void setDescricaoLocalInfracao(String descricaoLocalInfracao) {
        this.descricaoLocalInfracao = descricaoLocalInfracao;
    }

    public String getBiomasAtingidos() {
        return biomasAtingidos;
    }

    public void setBiomasAtingidos(String biomasAtingidos) {
        this.biomasAtingidos = biomasAtingidos;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public LocalDateTime getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDateTime dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public LocalDateTime getDataUltimaAlteracao() {
        return dataUltimaAlteracao;
    }

    public void setDataUltimaAlteracao(LocalDateTime dataUltimaAlteracao) {
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public String getEnquadramentoAdministrativo() {
        return enquadramentoAdministrativo;
    }

    public void setEnquadramentoAdministrativo(String enquadramentoAdministrativo) {
        this.enquadramentoAdministrativo = enquadramentoAdministrativo;
    }

    public LocalDateTime getUltimaAtualizacaoRelatorio() {
        return ultimaAtualizacaoRelatorio;
    }

    public void setUltimaAtualizacaoRelatorio(LocalDateTime ultimaAtualizacaoRelatorio) {
        this.ultimaAtualizacaoRelatorio = ultimaAtualizacaoRelatorio;
    }
}
