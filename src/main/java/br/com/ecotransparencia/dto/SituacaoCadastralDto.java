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

    public SituacaoCadastralDto() {
    }

    public SituacaoCadastralDto(boolean valido, String situacao, String mensagem) {
        this.valido = valido;
        this.situacao = situacao;
        this.mensagem = mensagem;
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
}
