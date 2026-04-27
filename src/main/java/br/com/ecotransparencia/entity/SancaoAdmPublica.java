package br.com.ecotransparencia.entity;

import br.com.ecotransparencia.domain.CadastroSancao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sancoes administrativas (CEIS + CNEP), unificadas em uma unica tabela
 * com discriminador {@code cadastro}. CEIS tem 24 colunas, CNEP tem 25
 * (apenas {@code valorMulta} adicional).
 *
 * O campo {@code cpfCnpj} guarda o documento como veio na fonte (digitos
 * apenas, no caso do CEIS/CNEP). Indice em {@code cpf_cnpj} esta na
 * migracao V7.
 */
@Entity
@Table(
    name = "sancao_adm_publica",
    indexes = {
        @Index(name = "idx_sancao_adm_publica_cpf_cnpj", columnList = "cpf_cnpj")
    }
)
public class SancaoAdmPublica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cadastro", nullable = false, length = 8)
    private CadastroSancao cadastro;

    @Column(name = "codigo_sancao")
    private String codigoSancao;

    @Column(name = "tipo_pessoa", length = 1)
    private String tipoPessoa;

    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    @Column(name = "nome_sancionado")
    private String nomeSancionado;

    @Column(name = "nome_orgao_sancionador")
    private String nomeOrgaoSancionador;

    @Column(name = "razao_social_receita")
    private String razaoSocialReceita;

    @Column(name = "nome_fantasia_receita")
    private String nomeFantasiaReceita;

    @Column(name = "numero_processo")
    private String numeroProcesso;

    @Column(name = "categoria_sancao")
    private String categoriaSancao;

    @Column(name = "valor_multa", precision = 18, scale = 2)
    private BigDecimal valorMulta;

    @Column(name = "data_inicio_sancao")
    private LocalDate dataInicioSancao;

    @Column(name = "data_fim_sancao")
    private LocalDate dataFimSancao;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "publicacao", length = 1000)
    private String publicacao;

    @Column(name = "detalhamento_meio_publicacao", length = 1000)
    private String detalhamentoMeioPublicacao;

    @Column(name = "data_transito_julgado")
    private LocalDate dataTransitoJulgado;

    @Column(name = "abrangencia_sancao")
    private String abrangenciaSancao;

    @Column(name = "orgao_sancionador")
    private String orgaoSancionador;

    @Column(name = "uf_orgao", length = 2)
    private String ufOrgao;

    @Column(name = "esfera_orgao")
    private String esferaOrgao;

    @Column(name = "fundamentacao_legal", columnDefinition = "TEXT")
    private String fundamentacaoLegal;

    @Column(name = "data_origem_informacao")
    private LocalDate dataOrigemInformacao;

    @Column(name = "origem_informacoes")
    private String origemInformacoes;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getNomeSancionado() {
        return nomeSancionado;
    }

    public void setNomeSancionado(String nomeSancionado) {
        this.nomeSancionado = nomeSancionado;
    }

    public String getNomeOrgaoSancionador() {
        return nomeOrgaoSancionador;
    }

    public void setNomeOrgaoSancionador(String nomeOrgaoSancionador) {
        this.nomeOrgaoSancionador = nomeOrgaoSancionador;
    }

    public String getRazaoSocialReceita() {
        return razaoSocialReceita;
    }

    public void setRazaoSocialReceita(String razaoSocialReceita) {
        this.razaoSocialReceita = razaoSocialReceita;
    }

    public String getNomeFantasiaReceita() {
        return nomeFantasiaReceita;
    }

    public void setNomeFantasiaReceita(String nomeFantasiaReceita) {
        this.nomeFantasiaReceita = nomeFantasiaReceita;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
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

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getPublicacao() {
        return publicacao;
    }

    public void setPublicacao(String publicacao) {
        this.publicacao = publicacao;
    }

    public String getDetalhamentoMeioPublicacao() {
        return detalhamentoMeioPublicacao;
    }

    public void setDetalhamentoMeioPublicacao(String detalhamentoMeioPublicacao) {
        this.detalhamentoMeioPublicacao = detalhamentoMeioPublicacao;
    }

    public LocalDate getDataTransitoJulgado() {
        return dataTransitoJulgado;
    }

    public void setDataTransitoJulgado(LocalDate dataTransitoJulgado) {
        this.dataTransitoJulgado = dataTransitoJulgado;
    }

    public String getAbrangenciaSancao() {
        return abrangenciaSancao;
    }

    public void setAbrangenciaSancao(String abrangenciaSancao) {
        this.abrangenciaSancao = abrangenciaSancao;
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

    public LocalDate getDataOrigemInformacao() {
        return dataOrigemInformacao;
    }

    public void setDataOrigemInformacao(LocalDate dataOrigemInformacao) {
        this.dataOrigemInformacao = dataOrigemInformacao;
    }

    public String getOrigemInformacoes() {
        return origemInformacoes;
    }

    public void setOrigemInformacoes(String origemInformacoes) {
        this.origemInformacoes = origemInformacoes;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
