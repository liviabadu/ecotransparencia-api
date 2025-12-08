package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO que representa o componente de score de uma fonte de dados especifica.
 */
@Schema(description = "Componente de score de uma fonte de dados")
public class ScoreComponentDto {

    @Schema(description = "Nome da fonte de dados", example = "Embargos IBAMA")
    private String fonte;

    @Schema(description = "Score bruto desta fonte (0-100)", example = "45")
    private int score;

    @Schema(description = "Peso desta fonte no calculo final (0.0-1.0)", example = "0.5")
    private double peso;

    @Schema(description = "Score ponderado (score * peso)", example = "22.5")
    private double scorePonderado;

    @Schema(description = "Quantidade de ocorrencias desta fonte", example = "3")
    private int quantidadeOcorrencias;

    public ScoreComponentDto() {
    }

    public ScoreComponentDto(String fonte, int score, double peso, int quantidadeOcorrencias) {
        this.fonte = fonte;
        this.score = score;
        this.peso = peso;
        this.scorePonderado = score * peso;
        this.quantidadeOcorrencias = quantidadeOcorrencias;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        this.scorePonderado = score * this.peso;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
        this.scorePonderado = this.score * peso;
    }

    public double getScorePonderado() {
        return scorePonderado;
    }

    public int getQuantidadeOcorrencias() {
        return quantidadeOcorrencias;
    }

    public void setQuantidadeOcorrencias(int quantidadeOcorrencias) {
        this.quantidadeOcorrencias = quantidadeOcorrencias;
    }
}
