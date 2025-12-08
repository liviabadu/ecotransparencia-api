package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.AsgScoreDto;
import br.com.ecotransparencia.dto.ScoreComponentDto;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Embargo;
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
        @DisplayName("Deve calcular score ponderado com apenas embargos")
        void shouldCalculateWeightedScoreWithOnlyEmbargos() {
            Embargo embargo = createBasicEmbargo();
            embargo.setSitDesmatamento("D"); // 25 pontos

            AsgScoreDto asg = calculator.calculate(List.of(embargo), Collections.emptyList());

            // Score de embargoS = 25, peso = 0.5
            // Como so tem embargos, normaliza: 25 * 0.5 / 0.5 = 25
            assertEquals(25, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(1, asg.getTotalOcorrencias());
        }

        @Test
        @DisplayName("Deve calcular score ponderado com apenas autos")
        void shouldCalculateWeightedScoreWithOnlyAutos() {
            AutoInfracao auto = createBasicAutoInfracao();
            auto.setMotivacaoConduta("Intencional");
            auto.setValorAutoInfracao(new BigDecimal("50000")); // 8 + 5 + 5 = 18 pontos

            AsgScoreDto asg = calculator.calculate(Collections.emptyList(), List.of(auto));

            // Score de autos = 18, peso = 0.35
            // Como so tem autos, normaliza: 18 * 0.35 / 0.35 = 18
            assertEquals(18, asg.getScore());
            assertEquals("Baixo", asg.getRiskLevel());
            assertEquals(1, asg.getTotalOcorrencias());
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
            assertEquals(0.5, embargoComponent.getPeso());
            assertEquals(1, embargoComponent.getQuantidadeOcorrencias());

            ScoreComponentDto autoComponent = asg.getBreakdown().stream()
                .filter(c -> c.getFonte().contains("Auto"))
                .findFirst()
                .orElseThrow();
            assertEquals(8, autoComponent.getScore());
            assertEquals(0.35, autoComponent.getPeso());
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
}
