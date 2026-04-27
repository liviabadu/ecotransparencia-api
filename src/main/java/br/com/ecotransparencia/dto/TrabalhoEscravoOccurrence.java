package br.com.ecotransparencia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Ocorrencia na Lista Suja do MTE.
 */
@Schema(description = "Inclusao na Lista Suja do MTE associada ao documento")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrabalhoEscravoOccurrence {

    @Schema(description = "Ano da acao fiscal")
    private Integer anoAcaoFiscal;

    @Schema(description = "UF onde a infracao foi constatada")
    private String uf;

    @Schema(description = "Empregador")
    private String empregador;

    @Schema(description = "CPF/CNPJ formatado, como na fonte")
    private String cpfCnpjFormatado;

    @Schema(description = "Estabelecimento (endereco)")
    private String estabelecimento;

    @Schema(description = "Numero de trabalhadores envolvidos")
    private Integer trabalhadoresEnvolvidos;

    @Schema(description = "CNAE")
    private String cnae;

    @Schema(description = "Data da decisao administrativa de procedencia")
    private LocalDate decisaoAdmProcedencia;

    @Schema(description = "Data de inclusao no Cadastro de Empregadores")
    private LocalDate inclusaoCadastroEmpregadores;

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
