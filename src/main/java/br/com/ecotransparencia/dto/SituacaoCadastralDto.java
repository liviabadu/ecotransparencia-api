package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO com o resultado da consulta de situacao cadastral na Receita Federal.
 */
@Schema(description = "Situacao cadastral do documento na Receita Federal")
public class SituacaoCadastralDto {

    @Schema(description = "Indica se o documento e valido", example = "true")
    private boolean valido;

    @Schema(description = "Situacao cadastral na RF", example = "ATIVA")
    private String situacao;

    @Schema(description = "Data da consulta em formato ISO 8601", example = "2025-12-07T22:00:00.000Z")
    private String dataConsulta;

    @Schema(description = "Mensagem descritiva da situacao", example = "Cadastro ativo na Receita Federal")
    private String mensagem;

    @Schema(description = "Indica se houve erro na consulta a Receita Federal (rate limit, timeout, etc)", example = "false")
    private boolean erroConsulta;

    @Schema(description = "Codigo do erro HTTP retornado pela API (quando aplicavel)", example = "429")
    private Integer codigoErro;

    public SituacaoCadastralDto() {
    }

    public SituacaoCadastralDto(boolean valido, String situacao, String mensagem) {
        this.valido = valido;
        this.situacao = situacao;
        this.mensagem = mensagem;
        this.erroConsulta = false;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(String dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    /**
     * Cria uma resposta de documento valido.
     */
    public static SituacaoCadastralDto valido(String situacao, String mensagem) {
        return new SituacaoCadastralDto(true, situacao, mensagem);
    }

    /**
     * Cria uma resposta de documento invalido.
     */
    public static SituacaoCadastralDto invalido(String situacao, String mensagem) {
        return new SituacaoCadastralDto(false, situacao, mensagem);
    }

    /**
     * Cria uma resposta de erro na consulta a Receita Federal.
     * Usado quando nao foi possivel validar o CNPJ (rate limit, timeout, erro de servidor, etc).
     */
    public static SituacaoCadastralDto erroConsulta(String mensagem, Integer codigoErro) {
        SituacaoCadastralDto dto = new SituacaoCadastralDto(false, "ERRO_CONSULTA", mensagem);
        dto.setErroConsulta(true);
        dto.setCodigoErro(codigoErro);
        return dto;
    }

    public boolean isErroConsulta() {
        return erroConsulta;
    }

    public void setErroConsulta(boolean erroConsulta) {
        this.erroConsulta = erroConsulta;
    }

    public Integer getCodigoErro() {
        return codigoErro;
    }

    public void setCodigoErro(Integer codigoErro) {
        this.codigoErro = codigoErro;
    }
}
