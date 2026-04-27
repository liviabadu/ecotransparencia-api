package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Cadastro de Empregadores que tenham submetido trabalhadores a condicoes
 * analogas as de escravo (MTE - Lista Suja).
 *
 * O CSV traz CPF/CNPJ formatado (ex.: {@code 41.297.068/0001-61}). O campo
 * {@code cpfCnpj} guarda o valor digit-only normalizado (via DocumentoUtil.limpar).
 */
@Entity
@Table(
    name = "trabalho_escravo_mte",
    indexes = {
        @Index(name = "idx_trabalho_escravo_mte_cpf_cnpj", columnList = "cpf_cnpj")
    }
)
public class TrabalhoEscravoMte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** ID na fonte (coluna "ID" do CSV). */
    @Column(name = "id_origem")
    private Integer idOrigem;

    @Column(name = "ano_acao_fiscal")
    private Integer anoAcaoFiscal;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "empregador")
    private String empregador;

    /** CPF/CNPJ digit-only (normalizado). */
    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    /** CPF/CNPJ como apresentado na fonte (formatado). */
    @Column(name = "cpf_cnpj_formatado")
    private String cpfCnpjFormatado;

    @Column(name = "estabelecimento", length = 1000)
    private String estabelecimento;

    @Column(name = "trabalhadores_envolvidos")
    private Integer trabalhadoresEnvolvidos;

    @Column(name = "cnae")
    private String cnae;

    @Column(name = "decisao_adm_procedencia")
    private LocalDate decisaoAdmProcedencia;

    @Column(name = "inclusao_cadastro_empregadores")
    private LocalDate inclusaoCadastroEmpregadores;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdOrigem() {
        return idOrigem;
    }

    public void setIdOrigem(Integer idOrigem) {
        this.idOrigem = idOrigem;
    }

    public Integer getAnoAcaoFiscal() {
        return anoAcaoFiscal;
    }

    public void setAnoAcaoFiscal(Integer anoAcaoFiscal) {
        this.anoAcaoFiscal = anoAcaoFiscal;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getEmpregador() {
        return empregador;
    }

    public void setEmpregador(String empregador) {
        this.empregador = empregador;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getCpfCnpjFormatado() {
        return cpfCnpjFormatado;
    }

    public void setCpfCnpjFormatado(String cpfCnpjFormatado) {
        this.cpfCnpjFormatado = cpfCnpjFormatado;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(String estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public Integer getTrabalhadoresEnvolvidos() {
        return trabalhadoresEnvolvidos;
    }

    public void setTrabalhadoresEnvolvidos(Integer trabalhadoresEnvolvidos) {
        this.trabalhadoresEnvolvidos = trabalhadoresEnvolvidos;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
    }

    public LocalDate getDecisaoAdmProcedencia() {
        return decisaoAdmProcedencia;
    }

    public void setDecisaoAdmProcedencia(LocalDate decisaoAdmProcedencia) {
        this.decisaoAdmProcedencia = decisaoAdmProcedencia;
    }

    public LocalDate getInclusaoCadastroEmpregadores() {
        return inclusaoCadastroEmpregadores;
    }

    public void setInclusaoCadastroEmpregadores(LocalDate inclusaoCadastroEmpregadores) {
        this.inclusaoCadastroEmpregadores = inclusaoCadastroEmpregadores;
    }
}
