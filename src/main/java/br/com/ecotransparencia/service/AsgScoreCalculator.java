package br.com.ecotransparencia.service;

import br.com.ecotransparencia.domain.FonteDados;
import br.com.ecotransparencia.dto.AsgScoreDto;
import br.com.ecotransparencia.dto.ScoreComponentDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Embargo;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculador de Score ASG (Ambiental, Social, Governanca).
 * Agrega scores de multiplas fontes de dados com pesos configurados.
 */
@ApplicationScoped
public class AsgScoreCalculator {

    // Biomas sensiveis que aumentam o score
    private static final List<String> BIOMAS_SENSIVEIS = List.of(
        "Amazonia", "Amazônia", "Mata Atlantica", "Mata Atlântica"
    );

    /**
     * Calcula o Score ASG agregando todas as fontes de dados.
     */
    public AsgScoreDto calculate(List<Embargo> embargos, List<AutoInfracao> autosInfracao) {
        List<ScoreComponentDto> breakdown = new ArrayList<>();

        // Calcula score de embargos
        int embargoScore = calculateEmbargoScore(embargos);
        if (!embargos.isEmpty() || embargoScore > 0) {
            breakdown.add(new ScoreComponentDto(
                FonteDados.EMBARGO.getDescricao(),
                embargoScore,
                FonteDados.EMBARGO.getPeso(),
                embargos.size()
            ));
        }

        // Calcula score de autos de infracao
        int autoScore = calculateAutoInfracaoScore(autosInfracao);
        if (!autosInfracao.isEmpty() || autoScore > 0) {
            breakdown.add(new ScoreComponentDto(
                FonteDados.AUTO_INFRACAO.getDescricao(),
                autoScore,
                FonteDados.AUTO_INFRACAO.getPeso(),
                autosInfracao.size()
            ));
        }

        // Calcula score final ponderado
        int finalScore = calculateWeightedScore(breakdown);
        int totalOcorrencias = embargos.size() + autosInfracao.size();

        AsgScoreDto asgScore = new AsgScoreDto();
        asgScore.setScore(finalScore);
        asgScore.setRiskLevel(classifyRiskLevel(finalScore));
        asgScore.setTotalOcorrencias(totalOcorrencias);
        asgScore.setBreakdown(breakdown);

        return asgScore;
    }

    /**
     * Calcula o score baseado em embargos.
     *
     * Criterios:
     * - +15 pontos por embargo
     * - +10 pontos se relacionado a desmatamento
     * - +5 pontos se em bioma sensivel (Amazonia, Mata Atlantica)
     * - +1 ponto a cada 10 hectares embargados (max +10)
     */
    int calculateEmbargoScore(List<Embargo> embargos) {
        int score = 0;

        for (Embargo embargo : embargos) {
            // Base por embargo
            score += 15;

            // Desmatamento
            if ("D".equals(embargo.getSitDesmatamento())) {
                score += 10;
            }

            // Bioma sensivel
            if (embargo.getCodTipoBioma() != null && embargo.getCodTipoBioma() == 4) {
                score += 5; // Amazonia
            } else if (isBiomaSensivel(embargo.getDesTipoBioma())) {
                score += 5;
            }

            // Area embargada
            if (embargo.getQtdAreaEmbargada() != null) {
                int areaPoints = Math.min(embargo.getQtdAreaEmbargada().intValue() / 10, 10);
                score += areaPoints;
            }
        }

        return Math.min(score, 100);
    }

    /**
     * Calcula o score baseado em autos de infracao.
     *
     * Criterios:
     * - +8 pontos por auto de infracao (nao cancelado)
     * - +5 pontos se conduta intencional
     * - +3 pontos se efeito grave no meio ambiente
     * - +5 pontos se bioma sensivel
     * - Pontos por valor da multa:
     *   - Ate R$ 10.000: +2
     *   - R$ 10.001 a R$ 50.000: +5
     *   - R$ 50.001 a R$ 100.000: +8
     *   - Acima de R$ 100.000: +12
     */
    int calculateAutoInfracaoScore(List<AutoInfracao> autos) {
        int score = 0;

        for (AutoInfracao auto : autos) {
            // Ignora autos cancelados
            if ("S".equalsIgnoreCase(auto.getSituacaoCancelado())) {
                continue;
            }

            // Base por auto
            score += 8;

            // Conduta intencional
            if ("Intencional".equalsIgnoreCase(auto.getMotivacaoConduta())) {
                score += 5;
            }

            // Efeito no meio ambiente
            String efeito = auto.getEfeitoMeioAmbiente();
            if (efeito != null) {
                if (efeito.toLowerCase().contains("grave") || efeito.toLowerCase().contains("severo")) {
                    score += 3;
                }
            }

            // Bioma sensivel
            if (isBiomaSensivel(auto.getBiomasAtingidos())) {
                score += 5;
            }

            // Valor da multa
            score += calculateMultaPoints(auto.getValorAutoInfracao());
        }

        return Math.min(score, 100);
    }

    private int calculateMultaPoints(BigDecimal valor) {
        if (valor == null) {
            return 0;
        }

        double v = valor.doubleValue();
        if (v > 100_000) {
            return 12;
        } else if (v > 50_000) {
            return 8;
        } else if (v > 10_000) {
            return 5;
        } else if (v > 0) {
            return 2;
        }
        return 0;
    }

    private boolean isBiomaSensivel(String bioma) {
        if (bioma == null) {
            return false;
        }
        String biomaLower = bioma.toLowerCase();
        return BIOMAS_SENSIVEIS.stream()
            .anyMatch(b -> biomaLower.contains(b.toLowerCase()));
    }

    /**
     * Calcula o score final ponderado.
     * Formula: sum(score * peso) / sum(peso) para fontes com ocorrencias
     */
    private int calculateWeightedScore(List<ScoreComponentDto> breakdown) {
        if (breakdown.isEmpty()) {
            return 0;
        }

        double totalPonderado = 0;
        double totalPeso = 0;

        for (ScoreComponentDto component : breakdown) {
            if (component.getQuantidadeOcorrencias() > 0) {
                totalPonderado += component.getScorePonderado();
                totalPeso += component.getPeso();
            }
        }

        if (totalPeso == 0) {
            return 0;
        }

        // Normaliza pelo peso total das fontes que tem ocorrencias
        double normalized = totalPonderado / totalPeso;
        return Math.min((int) Math.round(normalized), 100);
    }

    /**
     * Classifica o nivel de risco baseado no score.
     */
    String classifyRiskLevel(int score) {
        if (score >= 80) {
            return "Critico";
        } else if (score >= 51) {
            return "Alto";
        } else if (score >= 26) {
            return "Medio";
        } else {
            return "Baixo";
        }
    }
}
