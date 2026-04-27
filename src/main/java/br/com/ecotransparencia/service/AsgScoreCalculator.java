package br.com.ecotransparencia.service;

import br.com.ecotransparencia.domain.FonteDados;
import br.com.ecotransparencia.dto.AsgScoreDto;
import br.com.ecotransparencia.dto.ScoreComponentDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.entity.IcmbioAutoInfracao;
import br.com.ecotransparencia.entity.IcmbioEmbargo;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return calculate(embargos, autosInfracao, sancoes, cepim, mte,
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList());
    }

    /**
     * Calcula o Score ASG agregando IBAMA + Fase B + Fase C (ICMBio).
     *
     * <p>TODO: pesos das fontes Fase C (ICMBIO_AUTO=0.10, ICMBIO_EMBARGO=0.15)
     * sao provisorios e devem ser ajustados com input do produto.
     */
    public AsgScoreDto calculate(List<Embargo> embargos,
                                 List<AutoInfracao> autosInfracao,
                                 List<SancaoAdmPublica> sancoes,
                                 List<Cepim> cepim,
                                 List<TrabalhoEscravoMte> mte,
                                 List<IcmbioAutoInfracao> icmbioAutos,
                                 List<IcmbioEmbargo> icmbioEmbargos) {
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

        // CEIS e CNEP separados no breakdown (mesmo entity, discriminador diferente).
        // Calibracao: categoria de sancao + esfera do orgao + transito em julgado + recencia.
        List<SancaoAdmPublica> ceisList = sancoes.stream()
                .filter(s -> s.getCadastro() == br.com.ecotransparencia.domain.CadastroSancao.CEIS).toList();
        List<SancaoAdmPublica> cnepList = sancoes.stream()
                .filter(s -> s.getCadastro() == br.com.ecotransparencia.domain.CadastroSancao.CNEP).toList();
        int ceisScore = calculateSancaoScore(ceisList);
        int cnepScore = calculateSancaoScore(cnepList);
        breakdown.add(new ScoreComponentDto(
                FonteDados.CEIS.getDescricao(), ceisScore, FonteDados.CEIS.getPeso(), ceisList.size()));
        breakdown.add(new ScoreComponentDto(
                FonteDados.CNEP.getDescricao(), cnepScore, FonteDados.CNEP.getPeso(), cnepList.size()));

        int cepimScore = calculateCepimScore(cepim);
        breakdown.add(new ScoreComponentDto(
                FonteDados.CEPIM.getDescricao(), cepimScore, FonteDados.CEPIM.getPeso(), cepim.size()));

        int mteScore = calculateMteScore(mte);
        breakdown.add(new ScoreComponentDto(
                FonteDados.MTE_TRABALHO_ESCRAVO.getDescricao(), mteScore,
                FonteDados.MTE_TRABALHO_ESCRAVO.getPeso(), mte.size()));

        // Fase C: ICMBio com calibracao por UC de protecao integral + recencia.
        int icmbioAutoScore = calculateIcmbioAutoScore(icmbioAutos);
        breakdown.add(new ScoreComponentDto(
                FonteDados.ICMBIO_AUTO.getDescricao(), icmbioAutoScore,
                FonteDados.ICMBIO_AUTO.getPeso(), icmbioAutos.size()));

        int icmbioEmbargoScore = calculateIcmbioEmbargoScore(icmbioEmbargos);
        breakdown.add(new ScoreComponentDto(
                FonteDados.ICMBIO_EMBARGO.getDescricao(), icmbioEmbargoScore,
                FonteDados.ICMBIO_EMBARGO.getPeso(), icmbioEmbargos.size()));

        int finalScore = calculateWeightedScoreAll(breakdown);
        int totalOcorrencias = embargos.size() + autosInfracao.size()
                + sancoes.size() + cepim.size() + mte.size()
                + icmbioAutos.size() + icmbioEmbargos.size();

        AsgScoreDto asgScore = new AsgScoreDto();
        asgScore.setScore(finalScore);
        asgScore.setRiskLevel(classifyRiskLevel(finalScore));
        asgScore.setTotalOcorrencias(totalOcorrencias);
        asgScore.setBreakdown(breakdown);

        return asgScore;
    }

    /**
     * @deprecated Score legado (10 pontos por ocorrencia). Use o overload
     * {@link #calculateSancaoScore(List)} que considera categoria, esfera,
     * transito em julgado e recencia.
     */
    @Deprecated
    int calculateSancaoScore(int count) {
        return Math.min(count * 10, 100);
    }

    /**
     * Score calibrado para sancoes administrativas (CEIS+CNEP).
     *
     * <p>Por sancao: pontos base ditados pela categoria (inidoneidade > impedimento >
     * suspensao > multa > generico), multiplicado por esfera do orgao
     * (federal=1.0, estadual=0.7, municipal=0.5), por trans em julgado
     * (sim=1.0, nao=0.6) e decay temporal por dataInicioSancao. CNEP soma
     * pontos extras por valor da multa.
     */
    int calculateSancaoScore(List<SancaoAdmPublica> sancoes) {
        double total = 0;
        for (SancaoAdmPublica s : sancoes) {
            double pts = baseScoreByCategoria(s.getCategoriaSancao());
            pts *= multiplierByEsfera(s.getEsferaOrgao());
            pts *= multiplierByTransito(s.getDataTransitoJulgado());
            // CNEP: bonus por valor da multa (CEIS nao tem)
            if (s.getCadastro() == br.com.ecotransparencia.domain.CadastroSancao.CNEP) {
                pts += calculateMultaPoints(s.getValorMulta());
            }
            pts *= recencyMultiplier(s.getDataInicioSancao());
            total += pts;
        }
        return Math.min((int) Math.round(total), 100);
    }

    /**
     * Score calibrado para CEPIM. Pondera pelo motivo do impedimento:
     * tomada de contas especial (mais grave) > irregularidade > omissao >
     * generico.
     */
    int calculateCepimScore(List<Cepim> cepim) {
        int total = 0;
        for (Cepim c : cepim) {
            total += cepimMotivoPoints(c.getMotivoImpedimento());
        }
        return Math.min(total, 100);
    }

    /**
     * Score calibrado para autos de infracao do ICMBio.
     *
     * <p>Por auto: 12 base + 8 se UC de protecao integral (PARNA, REBIO, ESEC,
     * MONA, REVIS) + faixa por valor da multa. Decay temporal por data/ano.
     */
    int calculateIcmbioAutoScore(List<IcmbioAutoInfracao> autos) {
        double total = 0;
        for (IcmbioAutoInfracao a : autos) {
            double pts = 12;
            if (isUcProtecaoIntegral(a.getNomeUc())) {
                pts += 8;
            }
            pts += calculateMultaPoints(a.getValorMulta());
            pts *= recencyMultiplierFromDateOrYear(a.getData(), a.getAno());
            total += pts;
        }
        return Math.min((int) Math.round(total), 100);
    }

    /**
     * Score calibrado para embargos do ICMBio.
     *
     * <p>Por embargo: 12 base + 8 se UC de protecao integral + 1 ponto a cada
     * 10 hectares de area embargada (max +10). Decay temporal por data/ano.
     */
    int calculateIcmbioEmbargoScore(List<IcmbioEmbargo> embargos) {
        double total = 0;
        for (IcmbioEmbargo e : embargos) {
            double pts = 12;
            if (isUcProtecaoIntegral(e.getNomeUc())) {
                pts += 8;
            }
            if (e.getArea() != null) {
                int areaPts = Math.min(e.getArea().intValue() / 10, 10);
                pts += areaPts;
            }
            pts *= recencyMultiplierFromDateOrYear(e.getData(), e.getAno());
            total += pts;
        }
        return Math.min((int) Math.round(total), 100);
    }

    /**
     * Score calibrado para MTE (Lista Suja).
     *
     * <p>Por inclusao: 15 base + 1 por trabalhador envolvido + 5 se ha decisao
     * administrativa de procedencia. Decay temporal por anoAcaoFiscal.
     */
    int calculateMteScore(List<TrabalhoEscravoMte> mte) {
        double total = 0;
        for (TrabalhoEscravoMte t : mte) {
            double pts = 15;
            if (t.getTrabalhadoresEnvolvidos() != null) {
                pts += t.getTrabalhadoresEnvolvidos();
            }
            if (t.getDecisaoAdmProcedencia() != null) {
                pts += 5;
            }
            pts *= recencyMultiplier(t.getAnoAcaoFiscal());
            total += pts;
        }
        return Math.min((int) Math.round(total), 100);
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

            // Decay temporal por data do embargo (null -> neutro 1.0).
            LocalDateTime dt = embargo.getDatEmbargo();
            embargoPoints *= recencyMultiplier(dt != null ? dt.toLocalDate() : null);

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
        double score = 0;

        for (AutoInfracao auto : autos) {
            // Ignora autos cancelados
            if ("S".equalsIgnoreCase(auto.getSituacaoCancelado())) {
                continue;
            }

            double autoPts = 0;

            // Base por auto
            autoPts += 8;

            // Conduta intencional
            if ("Intencional".equalsIgnoreCase(auto.getMotivacaoConduta())) {
                autoPts += 5;
            }

            // Efeito no meio ambiente
            String efeito = auto.getEfeitoMeioAmbiente();
            if (efeito != null) {
                if (efeito.toLowerCase().contains("grave") || efeito.toLowerCase().contains("severo")) {
                    autoPts += 3;
                }
            }

            // Bioma sensivel
            if (isBiomaSensivel(auto.getBiomasAtingidos())) {
                autoPts += 5;
            }

            // Valor da multa
            autoPts += calculateMultaPoints(auto.getValorAutoInfracao());

            // Decay temporal por data do auto (null -> neutro 1.0).
            LocalDateTime dt = auto.getDataHoraAutoInfracao();
            autoPts *= recencyMultiplier(dt != null ? dt.toLocalDate() : null);

            score += autoPts;
        }

        return Math.min((int) Math.round(score), 100);
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

    // ---------------------------------------------------------------
    // Helpers de calibracao (Fase de calibracao 2026-04-27)
    // ---------------------------------------------------------------

    /**
     * Pontos base por categoria de sancao (CEIS/CNEP):
     * inidoneidade > impedimento/proibicao > suspensao > multa > demais.
     */
    static double baseScoreByCategoria(String categoria) {
        if (categoria == null) return 8;
        String c = categoria.toLowerCase();
        if (c.contains("inidoneidade") || c.contains("inidonei") || c.contains("inidoneo")) {
            return 25;
        }
        if (c.contains("impedimento") || c.contains("proibi") || c.contains("impedida")) {
            return 12;
        }
        if (c.contains("suspens")) {
            return 10;
        }
        if (c.contains("multa") || c.contains("publica")) {
            return 8;
        }
        return 6;
    }

    /**
     * Multiplicador por esfera do orgao sancionador.
     * Federal pesa mais que estadual, que pesa mais que municipal.
     * Null -> neutro (0.7) para nao penalizar dados incompletos.
     */
    static double multiplierByEsfera(String esfera) {
        if (esfera == null) return 0.7;
        String e = esfera.toUpperCase();
        if (e.contains("FEDERAL")) return 1.0;
        if (e.contains("ESTADUAL") || e.contains("DISTRITAL")) return 0.7;
        if (e.contains("MUNICIPAL")) return 0.5;
        return 0.7;
    }

    /**
     * Multiplicador por trans em julgado: confirmada vale 1.0,
     * ainda em recurso (data nula) vale 0.6.
     */
    static double multiplierByTransito(LocalDate dataTransitoJulgado) {
        return dataTransitoJulgado != null ? 1.0 : 0.6;
    }

    /**
     * Decay temporal: ocorrencias recentes pesam mais.
     * Null -> 1.0 (neutro, nao penaliza dados sem data).
     */
    static double recencyMultiplier(LocalDate date) {
        if (date == null) return 1.0;
        int years = LocalDate.now().getYear() - date.getYear();
        if (years < 0) return 1.0;
        if (years <= 5) return 1.0;
        if (years <= 10) return 0.7;
        if (years <= 20) return 0.4;
        return 0.2;
    }

    /**
     * Decay temporal a partir de ano apenas (Integer). Null -> 1.0.
     */
    static double recencyMultiplier(Integer year) {
        if (year == null) return 1.0;
        return recencyMultiplier(LocalDate.of(year, 1, 1));
    }

    /**
     * Decay preferindo {@code data} (LocalDate) sobre {@code ano} (Integer)
     * quando ambos disponiveis. Usado pelo ICMBio.
     */
    static double recencyMultiplierFromDateOrYear(LocalDate data, Integer ano) {
        if (data != null) return recencyMultiplier(data);
        return recencyMultiplier(ano);
    }

    /**
     * Detecta UC de protecao integral (Lei 9.985/2000 - SNUC) pelo nome.
     * UCs de protecao integral (PARNA, REBIO, ESEC, MONA, REVIS) sao mais
     * restritivas que as de uso sustentavel (RESEX, FLONA, APA, RDS, ARIE).
     */
    static boolean isUcProtecaoIntegral(String nomeUc) {
        if (nomeUc == null) return false;
        String u = nomeUc.toUpperCase();
        return u.contains("PARNA") || u.contains("PARQUE NACIONAL") ||
               u.contains("REBIO") || u.contains("RESERVA BIOLOGICA") || u.contains("RESERVA BIOLÓGICA") ||
               u.contains("ESEC") || u.contains("ESTACAO ECOLOGICA") || u.contains("ESTAÇÃO ECOLÓGICA") ||
               u.contains("MONA") || u.contains("MONUMENTO NATURAL") ||
               u.contains("REVIS") || u.contains("REFUGIO DE VIDA") || u.contains("REFÚGIO DE VIDA");
    }

    /**
     * Pontos por motivo de impedimento CEPIM. Tomada de contas especial e
     * mais grave que irregularidade na execucao, que e mais grave que
     * omissao de prestacao de contas.
     */
    static int cepimMotivoPoints(String motivo) {
        if (motivo == null) return 8;
        String m = motivo.toLowerCase();
        if (m.contains("tomada de contas") || m.contains("tomada de contas especial")) return 15;
        if (m.contains("irregularidade") || m.contains("desvio")) return 12;
        if (m.contains("omiss") || m.contains("nao apresent") || m.contains("não apresent")) return 8;
        return 10;
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
