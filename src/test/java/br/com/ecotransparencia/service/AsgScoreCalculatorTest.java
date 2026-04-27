package br.com.ecotransparencia.service;

import br.com.ecotransparencia.domain.CadastroSancao;
import br.com.ecotransparencia.dto.AsgScoreDto;
import br.com.ecotransparencia.dto.ScoreComponentDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade para AsgScoreCalculator.
 */
class AsgScoreCalculatorTest {

    private AsgScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AsgScoreCalculator();
    }

    @Nested
    @DisplayName("Calculo de Score de Embargos")
    class EmbargoScoreTests {

        @Test
        @DisplayName("Deve retornar 0 quando nao ha embargos")
        void shouldReturnZeroWhenNoEmbargos() {
            int score = calculator.calculateEmbargoScore(Collections.emptyList());
            assertEquals(0, score);
        }

        @Test
        @DisplayName("Deve adicionar 15 pontos por embargo")
        void shouldAdd15PointsPerEmbargo() {
            Embargo embargo = createBasicEmbargo();
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(15, score);
        }

        @Test
        @DisplayName("Deve adicionar 10 pontos para desmatamento")
        void shouldAdd10PointsForDeforestation() {
            Embargo embargo = createBasicEmbargo();
            embargo.setSitDesmatamento("D");
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(25, score); // 15 base + 10 desmatamento
        }

        @Test
        @DisplayName("Deve adicionar 5 pontos para bioma Amazonia")
        void shouldAdd5PointsForAmazonia() {
            Embargo embargo = createBasicEmbargo();
            embargo.setCodTipoBioma(4); // Amazonia
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(20, score); // 15 base + 5 bioma
        }

        @Test
        @DisplayName("Deve adicionar pontos por area embargada (1 ponto por 10 hectares)")
        void shouldAddPointsForArea() {
            Embargo embargo = createBasicEmbargo();
            embargo.setQtdAreaEmbargada(new BigDecimal("50")); // 50 hectares = 5 pontos
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(20, score); // 15 base + 5 area
        }

        @Test
        @DisplayName("Deve limitar pontos de area a 10")
        void shouldCapAreaPointsAt10() {
            Embargo embargo = createBasicEmbargo();
            embargo.setQtdAreaEmbargada(new BigDecimal("500")); // 500 hectares = 50 pontos, limitado a 10
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(25, score); // 15 base + 10 area (capped)
        }

        @Test
        @DisplayName("Deve limitar score total a 100")
        void shouldCapTotalScoreAt100() {
            // Cria 10 embargos com desmatamento e bioma Amazonia
            List<Embargo> embargos = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> {
                    Embargo e = createBasicEmbargo();
                    e.setSeqTad((long) i);
                    e.setSitDesmatamento("D");
                    e.setCodTipoBioma(4);
                    e.setQtdAreaEmbargada(new BigDecimal("100"));
                    return e;
                })
                .toList();

            int score = calculator.calculateEmbargoScore(embargos);
            assertEquals(100, score);
        }

        @Test
        @DisplayName("Deve aplicar peso muito baixo (10%) para embargos baixados")
        void shouldApplyLowWeightForBaixadoEmbargos() {
            Embargo embargoAtivo = createBasicEmbargo();
            embargoAtivo.setSeqTad(1L);
            // Embargo ativo: 15 pontos

            Embargo embargoBaixado = createBasicEmbargo();
            embargoBaixado.setSeqTad(2L);
            embargoBaixado.setIndBaixado("S"); // Embargo baixado
            // Embargo baixado: 15 * 0.10 = 1.5 ~ 2 pontos

            int scoreAtivo = calculator.calculateEmbargoScore(List.of(embargoAtivo));
            int scoreBaixado = calculator.calculateEmbargoScore(List.of(embargoBaixado));

            assertEquals(15, scoreAtivo);
            assertEquals(2, scoreBaixado); // 15 * 0.10 = 1.5 arredondado
        }

        @Test
        @DisplayName("Embargos baixados com desmatamento devem ter peso reduzido")
        void shouldReduceScoreForBaixadoEmbargoWithDeforestation() {
            Embargo embargoBaixado = createBasicEmbargo();
            embargoBaixado.setSitDesmatamento("D");
            embargoBaixado.setIndBaixado("S");
            // Base: 15 + 10 (desmatamento) = 25
            // Com reducao: 25 * 0.10 = 2.5 ~ 3

            int score = calculator.calculateEmbargoScore(List.of(embargoBaixado));
            assertEquals(3, score); // 25 * 0.10 = 2.5 arredondado
        }

        @Test
        @DisplayName("Embargos com indBaixado N devem ter pontuacao normal")
        void shouldUseNormalScoreForNonBaixadoEmbargo() {
            Embargo embargo = createBasicEmbargo();
            embargo.setIndBaixado("N"); // Nao baixado
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(15, score);
        }

        @Test
        @DisplayName("Embargos com indBaixado null devem ter pontuacao normal")
        void shouldUseNormalScoreForNullBaixadoEmbargo() {
            Embargo embargo = createBasicEmbargo();
            embargo.setIndBaixado(null); // null = nao baixado
            int score = calculator.calculateEmbargoScore(List.of(embargo));
            assertEquals(15, score);
        }
    }

    @Nested
    @DisplayName("Calculo de Score de Autos de Infracao")
    class AutoInfracaoScoreTests {

        @Test
        @DisplayName("Deve retornar 0 quando nao ha autos")
        void shouldReturnZeroWhenNoAutos() {
            int score = calculator.calculateAutoInfracaoScore(Collections.emptyList());
            assertEquals(0, score);
        }

        @Test
        @DisplayName("Deve adicionar 8 pontos por auto")
        void shouldAdd8PointsPerAuto() {
            AutoInfracao auto = createBasicAutoInfracao();
            int score = calculator.calculateAutoInfracaoScore(List.of(auto));
            assertEquals(8, score);
        }

        @Test
        @DisplayName("Deve ignorar autos cancelados")
        void shouldIgnoreCancelledAutos() {
            AutoInfracao auto = createBasicAutoInfracao();
            auto.setSituacaoCancelado("S");
            int score = calculator.calculateAutoInfracaoScore(List.of(auto));
            assertEquals(0, score);
        }

        @Test
        @DisplayName("Deve adicionar 5 pontos para conduta intencional")
        void shouldAdd5PointsForIntentional() {
            AutoInfracao auto = createBasicAutoInfracao();
            auto.setMotivacaoConduta("Intencional");
            int score = calculator.calculateAutoInfracaoScore(List.of(auto));
            assertEquals(13, score); // 8 base + 5 intencional
        }

        @Test
        @DisplayName("Deve adicionar 5 pontos para bioma Amazonia")
        void shouldAdd5PointsForAmazoniaBiome() {
            AutoInfracao auto = createBasicAutoInfracao();
            auto.setBiomasAtingidos("Amazônia");
            int score = calculator.calculateAutoInfracaoScore(List.of(auto));
            assertEquals(13, score); // 8 base + 5 bioma
        }

        @Test
        @DisplayName("Deve adicionar pontos por valor da multa")
        void shouldAddPointsForFineValue() {
            AutoInfracao auto1 = createBasicAutoInfracao();
            auto1.setValorAutoInfracao(new BigDecimal("5000")); // +2
            assertEquals(10, calculator.calculateAutoInfracaoScore(List.of(auto1)));

            AutoInfracao auto2 = createBasicAutoInfracao();
            auto2.setValorAutoInfracao(new BigDecimal("25000")); // +5
            assertEquals(13, calculator.calculateAutoInfracaoScore(List.of(auto2)));

            AutoInfracao auto3 = createBasicAutoInfracao();
            auto3.setValorAutoInfracao(new BigDecimal("75000")); // +8
            assertEquals(16, calculator.calculateAutoInfracaoScore(List.of(auto3)));

            AutoInfracao auto4 = createBasicAutoInfracao();
            auto4.setValorAutoInfracao(new BigDecimal("150000")); // +12
            assertEquals(20, calculator.calculateAutoInfracaoScore(List.of(auto4)));
        }
    }

    @Nested
    @DisplayName("Calculo de Score ASG Agregado")
    class AsgScoreTests {

        @Test
        @DisplayName("Deve retornar score 0 quando nao ha ocorrencias")
        void shouldReturnZeroWhenNoOccurrences() {
            AsgScoreDto asg = calculator.calculate(Collections.emptyList(), Collections.emptyList());
            assertEquals(0, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(0, asg.getTotalOcorrencias());
        }

        @Test
        @DisplayName("Deve calcular score ponderado com apenas embargos (considera ambas fontes)")
        void shouldCalculateWeightedScoreWithOnlyEmbargos() {
            Embargo embargo = createBasicEmbargo();
            embargo.setSitDesmatamento("D"); // 25 pontos

            AsgScoreDto asg = calculator.calculate(List.of(embargo), Collections.emptyList());

            // Score de embargos = 25, peso = 0.5
            // Score de autos = 0, peso = 0.35
            // NOVA LOGICA: Considera AMBAS as fontes (Autos E Embargos)
            // Ponderado = (25*0.5 + 0*0.35) / (0.5 + 0.35) = 12.5 / 0.85 = 14.7 ~ 15
            assertEquals(15, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(1, asg.getTotalOcorrencias());
            // Verifica que AMBAS as fontes estao no breakdown
            assertEquals(2, asg.getBreakdown().size());
        }

        @Test
        @DisplayName("Deve calcular score ponderado com apenas autos (considera ambas fontes)")
        void shouldCalculateWeightedScoreWithOnlyAutos() {
            AutoInfracao auto = createBasicAutoInfracao();
            auto.setMotivacaoConduta("Intencional");
            auto.setValorAutoInfracao(new BigDecimal("50000")); // 8 + 5 + 5 = 18 pontos

            AsgScoreDto asg = calculator.calculate(Collections.emptyList(), List.of(auto));

            // Score de embargos = 0, peso = 0.25 (calibracao 2026-04-27)
            // Score de autos = 18, peso = 0.18
            // Ponderado = (0*0.25 + 18*0.18) / (0.25 + 0.18) = 3.24 / 0.43 = 7.53 ~ 8
            assertEquals(8, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(1, asg.getTotalOcorrencias());
            // Verifica que AMBAS as fontes estao no breakdown
            assertEquals(2, asg.getBreakdown().size());
        }

        @Test
        @DisplayName("Deve calcular score ponderado combinando fontes")
        void shouldCalculateWeightedScoreWithBothSources() {
            Embargo embargo = createBasicEmbargo();
            embargo.setSitDesmatamento("D");
            embargo.setCodTipoBioma(4); // 15 + 10 + 5 = 30 pontos

            AutoInfracao auto = createBasicAutoInfracao();
            auto.setMotivacaoConduta("Intencional");
            auto.setValorAutoInfracao(new BigDecimal("150000")); // 8 + 5 + 12 = 25 pontos

            AsgScoreDto asg = calculator.calculate(List.of(embargo), List.of(auto));

            // Score embargos = 30, peso = 0.5
            // Score autos = 25, peso = 0.35
            // Ponderado = (30*0.5 + 25*0.35) / (0.5 + 0.35) = (15 + 8.75) / 0.85 = 27.94 ~ 28
            assertEquals(28, asg.getScore());
            assertEquals("Medio", asg.getRiskLevel());
            assertEquals(2, asg.getTotalOcorrencias());
        }

        @Test
        @DisplayName("Deve incluir breakdown por fonte")
        void shouldIncludeBreakdownBySource() {
            Embargo embargo = createBasicEmbargo();
            AutoInfracao auto = createBasicAutoInfracao();

            AsgScoreDto asg = calculator.calculate(List.of(embargo), List.of(auto));

            assertNotNull(asg.getBreakdown());
            assertEquals(2, asg.getBreakdown().size());

            ScoreComponentDto embargoComponent = asg.getBreakdown().stream()
                .filter(c -> c.getFonte().contains("Embargo"))
                .findFirst()
                .orElseThrow();
            assertEquals(15, embargoComponent.getScore());
            assertEquals(0.25, embargoComponent.getPeso()); // calibracao ESG 60/20/20
            assertEquals(1, embargoComponent.getQuantidadeOcorrencias());

            ScoreComponentDto autoComponent = asg.getBreakdown().stream()
                .filter(c -> c.getFonte().contains("Auto"))
                .findFirst()
                .orElseThrow();
            assertEquals(8, autoComponent.getScore());
            assertEquals(0.18, autoComponent.getPeso()); // calibracao ESG 60/20/20
            assertEquals(1, autoComponent.getQuantidadeOcorrencias());
        }
    }

    @Nested
    @DisplayName("Classificacao de Nivel de Risco")
    class RiskLevelTests {

        @Test
        @DisplayName("Deve classificar como Baixo para score 0-25")
        void shouldClassifyAsLowForScoreUpTo25() {
            assertEquals("Baixo", calculator.classifyRiskLevel(0));
            assertEquals("Baixo", calculator.classifyRiskLevel(25));
        }

        @Test
        @DisplayName("Deve classificar como Medio para score 26-50")
        void shouldClassifyAsMediumForScore26To50() {
            assertEquals("Medio", calculator.classifyRiskLevel(26));
            assertEquals("Medio", calculator.classifyRiskLevel(50));
        }

        @Test
        @DisplayName("Deve classificar como Alto para score 51-79")
        void shouldClassifyAsHighForScore51To79() {
            assertEquals("Alto", calculator.classifyRiskLevel(51));
            assertEquals("Alto", calculator.classifyRiskLevel(79));
        }

        @Test
        @DisplayName("Deve classificar como Critico para score 80+")
        void shouldClassifyAsCriticalForScore80Plus() {
            assertEquals("Critico", calculator.classifyRiskLevel(80));
            assertEquals("Critico", calculator.classifyRiskLevel(100));
        }
    }

    // Helper methods

    private Embargo createBasicEmbargo() {
        Embargo embargo = new Embargo();
        embargo.setSeqTad(1L);
        embargo.setNomePessoaEmbargada("Teste Ltda");
        embargo.setCpfCnpjEmbargado("12345678000100");
        return embargo;
    }

    private AutoInfracao createBasicAutoInfracao() {
        AutoInfracao auto = new AutoInfracao();
        auto.setSeqAutoInfracao(1L);
        auto.setNomeInfrator("Teste Ltda");
        auto.setCpfCnpjInfrator("12345678000100");
        auto.setSituacaoCancelado("N");
        return auto;
    }

    @Nested
    @DisplayName("Fase B: novas fontes (CEIS/CNEP/CEPIM/MTE)")
    class FaseBScoreTests {

        @Test
        @DisplayName("Score generico de sancoes: 10 pontos por ocorrencia, capped em 100")
        void shouldComputeSancaoScore() {
            assertEquals(0, calculator.calculateSancaoScore(0));
            assertEquals(10, calculator.calculateSancaoScore(1));
            assertEquals(50, calculator.calculateSancaoScore(5));
            assertEquals(100, calculator.calculateSancaoScore(15)); // cap
        }

        @Test
        @DisplayName("Score MTE: 15 pontos base + 1 por trabalhador envolvido")
        void shouldComputeMteScore() {
            TrabalhoEscravoMte t = new TrabalhoEscravoMte();
            t.setTrabalhadoresEnvolvidos(3);
            assertEquals(18, calculator.calculateMteScore(List.of(t)));

            TrabalhoEscravoMte t2 = new TrabalhoEscravoMte();
            t2.setTrabalhadoresEnvolvidos(null);
            assertEquals(15, calculator.calculateMteScore(List.of(t2)));

            assertEquals(0, calculator.calculateMteScore(Collections.emptyList()));
        }

        @Test
        @DisplayName("calculate de 5 args inclui todas as fontes no breakdown")
        void shouldIncludeAllPhaseBSourcesInBreakdown() {
            SancaoAdmPublica ceis = new SancaoAdmPublica();
            ceis.setCadastro(CadastroSancao.CEIS);
            SancaoAdmPublica cnep = new SancaoAdmPublica();
            cnep.setCadastro(CadastroSancao.CNEP);
            Cepim cep = new Cepim();
            TrabalhoEscravoMte mte = new TrabalhoEscravoMte();
            mte.setTrabalhadoresEnvolvidos(2);

            AsgScoreDto asg = calculator.calculate(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    List.of(ceis, cnep),
                    List.of(cep),
                    List.of(mte));

            // Breakdown deve ter 8 fontes: EMBARGO, AUTO_INFRACAO, CEIS, CNEP, CEPIM, MTE,
            // ICMBIO_AUTO, ICMBIO_EMBARGO (overload de 5 args delega ao de 7 com listas vazias).
            assertEquals(8, asg.getBreakdown().size());
            assertEquals(4, asg.getTotalOcorrencias()); // 2 sancoes + 1 cepim + 1 mte
        }

        @Test
        @DisplayName("calculate de 5 args sem ocorrencias retorna score 0")
        void shouldReturnZeroWhenNoOccurrencesAtAll() {
            AsgScoreDto asg = calculator.calculate(
                    Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

            assertEquals(0, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(0, asg.getTotalOcorrencias());
            // 8 fontes: EMBARGO, AUTO_INFRACAO, CEIS, CNEP, CEPIM, MTE, ICMBIO_AUTO, ICMBIO_EMBARGO
            assertEquals(8, asg.getBreakdown().size());
        }

        @Test
        @DisplayName("calculate de 2 args preserva comportamento original (apenas IBAMA no breakdown)")
        void shouldPreserveOriginalBehaviorWith2ArgOverload() {
            Embargo embargo = createBasicEmbargo();
            embargo.setSitDesmatamento("D"); // 25 pontos

            AsgScoreDto asgOld = calculator.calculate(List.of(embargo), Collections.emptyList());

            // Score identico ao teste original (apenas IBAMA no breakdown)
            assertEquals(15, asgOld.getScore());
            assertEquals("Baixo", asgOld.getRiskLevel());
            assertEquals(1, asgOld.getTotalOcorrencias());
            assertEquals(2, asgOld.getBreakdown().size());
        }
    }

    @Nested
    @DisplayName("Calibracao 2026-04-27: categoria, esfera, transito, recencia, UC integral")
    class CalibracaoTests {

        @Test
        @DisplayName("Sancao: inidoneidade vale mais que multa simples")
        void shouldScoreInidoneidadeHigherThanMulta() {
            SancaoAdmPublica inidoneidade = new SancaoAdmPublica();
            inidoneidade.setCadastro(CadastroSancao.CEIS);
            inidoneidade.setCategoriaSancao("Declaracao de Inidoneidade");
            inidoneidade.setEsferaOrgao("FEDERAL");
            inidoneidade.setDataTransitoJulgado(java.time.LocalDate.now());

            SancaoAdmPublica multa = new SancaoAdmPublica();
            multa.setCadastro(CadastroSancao.CEIS);
            multa.setCategoriaSancao("Multa simples");
            multa.setEsferaOrgao("FEDERAL");
            multa.setDataTransitoJulgado(java.time.LocalDate.now());

            int scoreInid = calculator.calculateSancaoScore(List.of(inidoneidade));
            int scoreMulta = calculator.calculateSancaoScore(List.of(multa));
            assertTrue(scoreInid > scoreMulta,
                    "Inidoneidade (" + scoreInid + ") deve pesar mais que multa (" + scoreMulta + ")");
            assertEquals(25, scoreInid); // base 25 * 1.0 federal * 1.0 transito
            assertEquals(8, scoreMulta);  // base 8 * 1.0 * 1.0
        }

        @Test
        @DisplayName("Sancao: federal pesa mais que estadual e municipal")
        void shouldWeightFederalMoreThanStateAndMunicipal() {
            SancaoAdmPublica federal = sancaoComEsfera("FEDERAL");
            SancaoAdmPublica estadual = sancaoComEsfera("ESTADUAL");
            SancaoAdmPublica municipal = sancaoComEsfera("MUNICIPAL");

            int sFed = calculator.calculateSancaoScore(List.of(federal));
            int sEst = calculator.calculateSancaoScore(List.of(estadual));
            int sMun = calculator.calculateSancaoScore(List.of(municipal));
            assertTrue(sFed > sEst && sEst > sMun,
                    "esperado fed > est > mun, ficou: " + sFed + " > " + sEst + " > " + sMun);
        }

        @Test
        @DisplayName("Sancao: sem transito em julgado vale 60% (em recurso)")
        void shouldDiscountWithoutTransito() {
            SancaoAdmPublica comTransito = sancaoComEsfera("FEDERAL");
            comTransito.setDataTransitoJulgado(java.time.LocalDate.now());

            SancaoAdmPublica semTransito = sancaoComEsfera("FEDERAL");
            semTransito.setDataTransitoJulgado(null);

            int scoreCom = calculator.calculateSancaoScore(List.of(comTransito));
            int scoreSem = calculator.calculateSancaoScore(List.of(semTransito));
            // Sem transito = 60% do com transito
            assertEquals((int) Math.round(scoreCom * 0.6), scoreSem);
        }

        @Test
        @DisplayName("Recencia: sancao de 20 anos atras pesa menos que recente")
        void shouldDecayOldSancao() {
            SancaoAdmPublica recente = sancaoComEsfera("FEDERAL");
            recente.setDataInicioSancao(java.time.LocalDate.now());

            SancaoAdmPublica antiga = sancaoComEsfera("FEDERAL");
            antiga.setDataInicioSancao(java.time.LocalDate.now().minusYears(25));

            int scoreRec = calculator.calculateSancaoScore(List.of(recente));
            int scoreAnt = calculator.calculateSancaoScore(List.of(antiga));
            assertTrue(scoreRec > scoreAnt,
                    "Recente (" + scoreRec + ") deve pesar mais que antiga (" + scoreAnt + ")");
        }

        @Test
        @DisplayName("ICMBio: UC de protecao integral (PARNA, REBIO) bonifica score")
        void shouldBonusForUcProtecaoIntegral() {
            br.com.ecotransparencia.entity.IcmbioAutoInfracao parna =
                    new br.com.ecotransparencia.entity.IcmbioAutoInfracao();
            parna.setVwNumAuto(1);
            parna.setNomeUc("PARNA de Anavilhanas");

            br.com.ecotransparencia.entity.IcmbioAutoInfracao apa =
                    new br.com.ecotransparencia.entity.IcmbioAutoInfracao();
            apa.setVwNumAuto(2);
            apa.setNomeUc("APA Costa de Itacare");

            int scoreParna = calculator.calculateIcmbioAutoScore(List.of(parna));
            int scoreApa = calculator.calculateIcmbioAutoScore(List.of(apa));
            assertTrue(scoreParna > scoreApa,
                    "PARNA (" + scoreParna + ") deve pesar mais que APA (" + scoreApa + ")");
            assertEquals(20, scoreParna); // 12 + 8 UC integral
            assertEquals(12, scoreApa);   // 12 base
        }

        @Test
        @DisplayName("CEPIM: tomada de contas especial pesa mais que omissao")
        void shouldWeightCepimByMotivo() {
            Cepim grave = new Cepim();
            grave.setMotivoImpedimento("INSTAURACAO DE TOMADA DE CONTAS ESPECIAL");
            Cepim leve = new Cepim();
            leve.setMotivoImpedimento("OMISSAO NO DEVER DE PRESTAR CONTAS");

            int sGrave = calculator.calculateCepimScore(List.of(grave));
            int sLeve = calculator.calculateCepimScore(List.of(leve));
            assertTrue(sGrave > sLeve, "TCE (" + sGrave + ") > omissao (" + sLeve + ")");
            assertEquals(15, sGrave);
            assertEquals(8, sLeve);
        }

        @Test
        @DisplayName("CNEP: bonus por valor de multa alto (acima de 100k)")
        void shouldBonusCnepByValorMulta() {
            SancaoAdmPublica cnepMultaAlta = new SancaoAdmPublica();
            cnepMultaAlta.setCadastro(CadastroSancao.CNEP);
            cnepMultaAlta.setCategoriaSancao("Multa");
            cnepMultaAlta.setEsferaOrgao("FEDERAL");
            cnepMultaAlta.setDataTransitoJulgado(java.time.LocalDate.now());
            cnepMultaAlta.setValorMulta(new BigDecimal("500000"));

            SancaoAdmPublica cnepMultaBaixa = new SancaoAdmPublica();
            cnepMultaBaixa.setCadastro(CadastroSancao.CNEP);
            cnepMultaBaixa.setCategoriaSancao("Multa");
            cnepMultaBaixa.setEsferaOrgao("FEDERAL");
            cnepMultaBaixa.setDataTransitoJulgado(java.time.LocalDate.now());
            cnepMultaBaixa.setValorMulta(new BigDecimal("5000"));

            int sAlta = calculator.calculateSancaoScore(List.of(cnepMultaAlta));
            int sBaixa = calculator.calculateSancaoScore(List.of(cnepMultaBaixa));
            assertTrue(sAlta > sBaixa, "Multa alta (" + sAlta + ") > multa baixa (" + sBaixa + ")");
        }

        private SancaoAdmPublica sancaoComEsfera(String esfera) {
            SancaoAdmPublica s = new SancaoAdmPublica();
            s.setCadastro(CadastroSancao.CEIS);
            s.setCategoriaSancao("Impedimento de licitar");
            s.setEsferaOrgao(esfera);
            s.setDataTransitoJulgado(java.time.LocalDate.now());
            return s;
        }
    }
}
