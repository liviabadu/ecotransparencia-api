package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * DTO que representa o Score ASG (Ambiental, Social, Governanca) calculado
 * a partir de multiplas fontes de dados.
 */
@Schema(description = "Score ASG calculado a partir de multiplas fontes de dados ambientais")
public class AsgScoreDto {

    @Schema(description = "Score final ASG (0-100)", example = "45")
    private int score;

    @Schema(description = "Nivel de risco: Baixo, Medio, Alto ou Critico", example = "Medio")
    private String riskLevel;

    @Schema(description = "Total de ocorrencias em todas as fontes", example = "8")
    private int totalOcorrencias;

    @Schema(description = "Detalhamento do score por fonte de dados")
    private List<ScoreComponentDto> breakdown;

    public AsgScoreDto() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getTotalOcorrencias() {
        return totalOcorrencias;
    }

    public void setTotalOcorrencias(int totalOcorrencias) {
        this.totalOcorrencias = totalOcorrencias;
    }

    public List<ScoreComponentDto> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<ScoreComponentDto> breakdown) {
        this.breakdown = breakdown;
    }
}
