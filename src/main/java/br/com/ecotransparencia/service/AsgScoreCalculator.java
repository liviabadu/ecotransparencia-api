package br.com.ecotransparencia.service;

import br.com.ecotransparencia.domain.FonteDados;
import br.com.ecotransparencia.dto.AsgScoreDto;
import br.com.ecotransparencia.dto.ScoreComponentDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
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
     * Calcula o Score ASG agregando apenas IBAMA (Embargos + Autos).
     *
     * <p>Mantido por retrocompatibilidade: gera breakdown apenas com EMBARGO
     * e AUTO_INFRACAO; o denominador da media ponderada usa apenas esses dois
     * pesos. Para o calculo completo incluindo fontes Fase B
     * (CEIS/CNEP/CEPIM/MTE), use o overload de 5 parametros.
     */
    public AsgScoreDto calculate(List<Embargo> embargos, List<AutoInfracao> autosInfracao) {
        List<ScoreComponentDto> breakdown = new ArrayList<>();

        int embargoScore = calculateEmbargoScore(embargos);
        breakdown.add(new ScoreComponentDto(
            FonteDados.EMBARGO.getDescricao(),
            embargoScore,
            FonteDados.EMBARGO.getPeso(),
            embargos.size()
        ));

        int autoScore = calculateAutoInfracaoScore(autosInfracao);
        breakdown.add(new ScoreComponentDto(
            FonteDados.AUTO_INFRACAO.getDescricao(),
            autoScore,
            FonteDados.AUTO_INFRACAO.getPeso(),
            autosInfracao.size()
        ));

        int finalScore = calculateWeightedScoreAll(breakdown);
        int totalOcorrencias = embargos.size() + autosInfracao.size();

        AsgScoreDto asgScore = new AsgScoreDto();
        asgScore.setScore(finalScore);
        asgScore.setRiskLevel(classifyRiskLevel(finalScore));
        asgScore.setTotalOcorrencias(totalOcorrencias);
        asgScore.setBreakdown(breakdown);

        return asgScore;
    }

    /**
     * Calcula o Score ASG agregando IBAMA + fontes Fase B.
     *
     * <p>IMPORTANTE: o score considera todas as fontes simultaneamente. Cada
     * fonte aparece no breakdown com seu peso configurado em {@link FonteDados}.
     *
     * <p>TODO: pesos das fontes Fase B (CEIS/CNEP/CEPIM/MTE) sao provisorios
     * (peso 0.05-0.10) e devem ser ajustados com input do produto. Ver brief
     * Fase B (~step 12) para o contexto.
     */
    public AsgScoreDto calculate(List<Embargo> embargos,
                                 List<AutoInfracao> autosInfracao,
                                 List<SancaoAdmPublica> sancoes,
                                 List<Cepim> cepim,
                                 List<TrabalhoEscravoMte> mte) {
        List<ScoreComponentDto> breakdown = new ArrayList<>();

        int embargoScore = calculateEmbargoScore(embargos);
        breakdown.add(new ScoreComponentDto(
            FonteDados.EMBARGO.getDescricao(),
            embargoScore,
            FonteDados.EMBARGO.getPeso(),
            embargos.size()
        ));

        int autoScore = calculateAutoInfracaoScore(autosInfracao);
        breakdown.add(new ScoreComponentDto(
            FonteDados.AUTO_INFRACAO.getDescricao(),
            autoScore,
            FonteDados.AUTO_INFRACAO.getPeso(),
            autosInfracao.size()
        ));

        // CEIS e CNEP separados no breakdown (mesmo entity, discriminador diferente)
        long ceisCount = sancoes.stream().filter(s -> s.getCadastro() == br.com.ecotransparencia.domain.CadastroSancao.CEIS).count();
        long cnepCount = sancoes.stream().filter(s -> s.getCadastro() == br.com.ecotransparencia.domain.CadastroSancao.CNEP).count();
        int ceisScore = calculateSancaoScore((int) ceisCount);
        int cnepScore = calculateSancaoScore((int) cnepCount);
        breakdown.add(new ScoreComponentDto(
                FonteDados.CEIS.getDescricao(), ceisScore, FonteDados.CEIS.getPeso(), (int) ceisCount));
        breakdown.add(new ScoreComponentDto(
                FonteDados.CNEP.getDescricao(), cnepScore, FonteDados.CNEP.getPeso(), (int) cnepCount));

        int cepimScore = calculateSancaoScore(cepim.size());
        breakdown.add(new ScoreComponentDto(
                FonteDados.CEPIM.getDescricao(), cepimScore, FonteDados.CEPIM.getPeso(), cepim.size()));

        int mteScore = calculateMteScore(mte);
        breakdown.add(new ScoreComponentDto(
                FonteDados.MTE_TRABALHO_ESCRAVO.getDescricao(), mteScore,
                FonteDados.MTE_TRABALHO_ESCRAVO.getPeso(), mte.size()));

        int finalScore = calculateWeightedScoreAll(breakdown);
        int totalOcorrencias = embargos.size() + autosInfracao.size()
                + sancoes.size() + cepim.size() + mte.size();

        AsgScoreDto asgScore = new AsgScoreDto();
        asgScore.setScore(finalScore);
        asgScore.setRiskLevel(classifyRiskLevel(finalScore));
        asgScore.setTotalOcorrencias(totalOcorrencias);
        asgScore.setBreakdown(breakdown);

        return asgScore;
    }

    /**
     * Score generico para sancoes administrativas / impedimentos.
     *
     * <p>TODO: criterio provisorio - 10 pontos por ocorrencia, capped em 100.
     * Substituir por criterio de produto (ex.: peso por categoria de sancao,
     * valor da multa, esfera do orgao).
     */
    int calculateSancaoScore(int count) {
        return Math.min(count * 10, 100);
    }

    /**
     * Score para Lista Suja MTE.
     *
     * <p>TODO: criterio provisorio - 15 pontos por ocorrencia (peso maior que
     * sancoes administrativas dado o nivel de gravidade), +1 ponto por
     * trabalhador envolvido. Capped em 100.
     */
    int calculateMteScore(List<TrabalhoEscravoMte> mte) {
        int score = 0;
        for (TrabalhoEscravoMte t : mte) {
            score += 15;
            if (t.getTrabalhadoresEnvolvidos() != null) {
                score += t.getTrabalhadoresEnvolvidos();
            }
        }
        return Math.min(score, 100);
    }

    // Fator de redução para embargos baixados (10% do valor normal)
    private static final double FATOR_EMBARGO_BAIXADO = 0.10;

    /**
     * Calcula o score baseado em embargos.
     *
     * Criterios para embargos ATIVOS:
     * - +15 pontos por embargo
     * - +10 pontos se relacionado a desmatamento
     * - +5 pontos se em bioma sensivel (Amazonia, Mata Atlantica)
     * - +1 ponto a cada 10 hectares embargados (max +10)
     *
     * Criterios para embargos BAIXADOS:
     * - Aplica-se apenas 10% dos pontos (peso muito baixo, mas ainda considerados)
     */
    int calculateEmbargoScore(List<Embargo> embargos) {
        double score = 0;

        for (Embargo embargo : embargos) {
            double embargoPoints = 0;

            // Base por embargo
            embargoPoints += 15;

            // Desmatamento
            if ("D".equals(embargo.getSitDesmatamento())) {
                embargoPoints += 10;
            }

            // Bioma sensivel
            if (embargo.getCodTipoBioma() != null && embargo.getCodTipoBioma() == 4) {
                embargoPoints += 5; // Amazonia
            } else if (isBiomaSensivel(embargo.getDesTipoBioma())) {
                embargoPoints += 5;
            }

            // Area embargada
            if (embargo.getQtdAreaEmbargada() != null) {
                int areaPoints = Math.min(embargo.getQtdAreaEmbargada().intValue() / 10, 10);
                embargoPoints += areaPoints;
            }

            // Se embargo baixado, aplica fator de redução (peso muito baixo)
            if (embargo.isBaixado()) {
                embargoPoints *= FATOR_EMBARGO_BAIXADO;
            }

            score += embargoPoints;
        }

        return Math.min((int) Math.round(score), 100);
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
     * Calcula o score final ponderado considerando TODAS as fontes (Autos E Embargos).
     * Formula: sum(score * peso) / sum(peso) para TODAS as fontes
     *
     * Diferente do método anterior, este considera AMBAS as fontes sempre,
     * mesmo que uma delas não tenha ocorrências.
     */
    private int calculateWeightedScoreAll(List<ScoreComponentDto> breakdown) {
        if (breakdown.isEmpty()) {
            return 0;
        }

        double totalPonderado = 0;
        double totalPeso = 0;

        // Considera TODAS as fontes, não apenas as com ocorrências
        for (ScoreComponentDto component : breakdown) {
            totalPonderado += component.getScorePonderado();
            totalPeso += component.getPeso();
        }

        if (totalPeso == 0) {
            return 0;
        }

        // Normaliza pelo peso total de TODAS as fontes
        double normalized = totalPonderado / totalPeso;
        return Math.min((int) Math.round(normalized), 100);
    }

    /**
     * @deprecated Use calculateWeightedScoreAll para considerar Autos E Embargos
     */
    @Deprecated
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
