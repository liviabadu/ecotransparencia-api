package br.com.ecotransparencia.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Consumer Pact Test para gerar o contrato com o novo formato ASG Score.
 *
 * Este teste simula o que o frontend espera receber da API, incluindo:
 * - asgScore: score ASG com breakdown por fonte
 * - ocorrencias: ocorrencias agrupadas (embargos + autosInfracao)
 * - occurrences: mantido para retrocompatibilidade
 *
 * Execute este teste para gerar o JSON do contrato em target/pacts/
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "EcoTransparenciaBackend", pactVersion = PactSpecVersion.V4)
class AsgScoreConsumerPactTest {

    private static final String CONSUMER_NAME = "EcoTransparenciaFrontendV2";

    // ==================== Builders reutilizaveis ====================

    /**
     * Constroi um embargo completo com todos os campos do OccurrenceDto.
     */
    private Consumer<LambdaDslObject> buildFullEmbargo() {
        return emb -> {
            emb.stringType("id", "emb-123");
            emb.stringType("category", "Ambiental IBAMA");
            emb.stringType("date", "2024-01-15T00:00:00.000Z");
            emb.stringType("description", "Embargo por desmatamento ilegal em area de preservacao");
            emb.stringType("source", "IBAMA");
            emb.stringType("sourceUrl", "https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php");
            emb.stringType("status", "Baixado");
            emb.stringType("autoInfracao", "123456-A");
            emb.booleanType("desmatamento", true);
            emb.decimalType("areaEmbargada", 150.5);
            emb.stringType("biome", "Amazonia");
            emb.object("location", loc -> {
                loc.stringType("uf", "PA");
                loc.stringType("municipio", "Altamira");
                loc.stringType("imovel", "Fazenda Santa Clara");
            });
        };
    }

    /**
     * Constroi um auto de infracao completo com todos os campos do AutoInfracaoDto.
     */
    private Consumer<LambdaDslObject> buildFullAutoInfracao() {
        return auto -> {
            auto.stringType("id", "auto-456");
            auto.stringType("numeroAuto", "ABCD1234");
            auto.stringType("data", "2024-02-20T10:30:00.000Z");
            auto.stringType("descricao", "Auto de infracao por extracao ilegal de madeira em area de preservacao permanente");
            auto.stringType("tipoInfracao", "Flora");
            auto.decimalType("valorMulta", 75000.00);
            auto.stringType("status", "Lavrado");
            auto.stringType("gravidade", "Grave");
            auto.stringType("motivacaoConduta", "Intencional");
            auto.stringType("efeitoMeioAmbiente", "Grave");
            auto.stringType("biomasAtingidos", "Amazonia");
            auto.stringType("enquadramentoLegal", "Art. 47 - Decreto 6514/2008");
            auto.stringType("source", "IBAMA");
            auto.object("location", loc -> {
                loc.stringType("uf", "AM");
                loc.stringType("municipio", "Manaus");
            });
        };
    }

    /**
     * Constroi o breakdown do ASG Score com todos os campos do ScoreComponentDto.
     */
    private Consumer<LambdaDslObject> buildFullBreakdown(String fonte, int score, double peso, int qtd) {
        return breakdown -> {
            breakdown.stringType("fonte", fonte);
            breakdown.integerType("score", score);
            breakdown.decimalType("peso", peso);
            breakdown.decimalType("scorePonderado", score * peso);
            breakdown.integerType("quantidadeOcorrencias", qtd);
        };
    }

    // ==================== US-ASG-001: Busca por CNPJ com ASG Score ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchByCnpjWithAsgScore(PactBuilder builder) {
        return builder
                .given("an entity with CNPJ 11222333000181 has embargos and autos de infracao")
                .expectsToReceiveHttpInteraction("a request to search by CNPJ expecting ASG score with all fields", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "11222333000181")
                                        .queryParameter("type", "cnpj"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "1");
                                                entity.stringType("name", "Empresa Verde Sustentavel Ltda");
                                                entity.stringType("document", "11222333000181");
                                                entity.stringType("documentType", "cnpj");
                                                entity.integerType("score", 45);
                                                entity.stringType("riskLevel", "Medio");

                                                // ASG Score completo
                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 45);
                                                    asg.stringType("riskLevel", "Medio");
                                                    asg.integerType("totalOcorrencias", 3);
                                                    asg.minArrayLike("breakdown", 1,
                                                            buildFullBreakdown("Embargos IBAMA", 30, 0.5, 2));
                                                });

                                                // Ocorrencias agrupadas com todos os campos
                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.minArrayLike("embargos", 1, buildFullEmbargo());
                                                    ocorrencias.minArrayLike("autosInfracao", 1, buildFullAutoInfracao());
                                                });

                                                // Retrocompatibilidade com todos os campos
                                                entity.minArrayLike("occurrences", 1, buildFullEmbargo());
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchByCnpjWithAsgScore")
    void testSearchByCnpjWithAsgScore(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "11222333000181")
                .queryParam("type", "cnpj")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getString("entity.document"), is("11222333000181"));

        // Valida ASG Score
        assertThat(response.jsonPath().getInt("entity.asgScore.score"), greaterThanOrEqualTo(0));
        assertThat(response.jsonPath().getString("entity.asgScore.riskLevel"), notNullValue());
        assertThat(response.jsonPath().getList("entity.asgScore.breakdown"), not(empty()));
        assertThat(response.jsonPath().getDouble("entity.asgScore.breakdown[0].scorePonderado"), notNullValue());

        // Valida embargos completos
        assertThat(response.jsonPath().getList("entity.ocorrencias.embargos"), not(empty()));
        assertThat(response.jsonPath().getString("entity.ocorrencias.embargos[0].sourceUrl"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.embargos[0].autoInfracao"), notNullValue());
        assertThat(response.jsonPath().getBoolean("entity.ocorrencias.embargos[0].desmatamento"), notNullValue());
        assertThat(response.jsonPath().getDouble("entity.ocorrencias.embargos[0].areaEmbargada"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.embargos[0].biome"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.embargos[0].location.imovel"), notNullValue());

        // Valida autos completos
        assertThat(response.jsonPath().getList("entity.ocorrencias.autosInfracao"), not(empty()));
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].status"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].gravidade"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].motivacaoConduta"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].efeitoMeioAmbiente"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].biomasAtingidos"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].enquadramentoLegal"), notNullValue());
        assertThat(response.jsonPath().getString("entity.ocorrencias.autosInfracao[0].location.uf"), notNullValue());

        // Valida retrocompatibilidade
        assertThat(response.jsonPath().getList("entity.occurrences"), not(empty()));
    }

    // ==================== US-ASG-002: Busca por CPF com ASG Score ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchByCpfWithAsgScore(PactBuilder builder) {
        return builder
                .given("a person with CPF 12345678909 has embargos and autos de infracao")
                .expectsToReceiveHttpInteraction("a request to search by CPF expecting ASG score with all fields", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "12345678909")
                                        .queryParameter("type", "cpf"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "5");
                                                entity.stringType("name", "Joao da Silva Teste");
                                                entity.stringType("document", "12345678909");
                                                entity.stringType("documentType", "cpf");
                                                entity.integerType("score", 28);
                                                entity.stringType("riskLevel", "Medio");

                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 28);
                                                    asg.stringType("riskLevel", "Medio");
                                                    asg.integerType("totalOcorrencias", 2);
                                                    asg.minArrayLike("breakdown", 1,
                                                            buildFullBreakdown("Embargos IBAMA", 20, 0.5, 1));
                                                });

                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.minArrayLike("embargos", 1, buildFullEmbargo());
                                                    ocorrencias.minArrayLike("autosInfracao", 1, buildFullAutoInfracao());
                                                });

                                                entity.minArrayLike("occurrences", 1, buildFullEmbargo());
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchByCpfWithAsgScore")
    void testSearchByCpfWithAsgScore(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "12345678909")
                .queryParam("type", "cpf")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getString("entity.documentType"), is("cpf"));
        assertThat(response.jsonPath().getInt("entity.asgScore.score"), greaterThanOrEqualTo(0));
    }

    // ==================== US-ASG-003: Busca por Nome com ASG Score ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchByNameWithAsgScore(PactBuilder builder) {
        return builder
                .given("an entity with name containing \"Empresa Verde\" has multiple occurrences")
                .expectsToReceiveHttpInteraction("a request to search by name expecting ASG score with all fields", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/name")
                                        .queryParameter("name", "Empresa Verde"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "1");
                                                entity.stringType("name", "Empresa Verde Sustentavel Ltda");
                                                entity.stringType("document", "11222333000181");
                                                entity.stringType("documentType", "cnpj");
                                                entity.integerType("score", 45);
                                                entity.stringType("riskLevel", "Medio");

                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 45);
                                                    asg.stringType("riskLevel", "Medio");
                                                    asg.integerType("totalOcorrencias", 3);
                                                    asg.minArrayLike("breakdown", 1,
                                                            buildFullBreakdown("Embargos IBAMA", 30, 0.5, 2));
                                                });

                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.minArrayLike("embargos", 1, buildFullEmbargo());
                                                    ocorrencias.minArrayLike("autosInfracao", 1, buildFullAutoInfracao());
                                                });

                                                entity.minArrayLike("occurrences", 1, buildFullEmbargo());
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchByNameWithAsgScore")
    void testSearchByNameWithAsgScore(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("name", "Empresa Verde")
                .when()
                .get("/api/search/name");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getString("entity.name"), containsString("Empresa Verde"));
        assertThat(response.jsonPath().getObject("entity.asgScore", Object.class), notNullValue());
        assertThat(response.jsonPath().getObject("entity.ocorrencias", Object.class), notNullValue());
    }

    // ==================== US-ASG-004: Entidade nao encontrada ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchNotFoundWithAsg(PactBuilder builder) {
        return builder
                .given("no entity with CNPJ 00000000000000 exists")
                .expectsToReceiveHttpInteraction("a request to search non-existent CNPJ with ASG", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "00000000000000")
                                        .queryParameter("type", "cnpj"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", false);
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchNotFoundWithAsg")
    void testSearchNotFoundWithAsg(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "00000000000000")
                .queryParam("type", "cnpj")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(false));
    }

    // ==================== US-ASG-005: Risco Critico com multiplas fontes ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchCriticalRiskWithAsgScore(PactBuilder builder) {
        return builder
                .given("an entity with critical risk has many embargos and autos de infracao")
                .expectsToReceiveHttpInteraction("a request to search entity with critical ASG risk", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "44555666000181")
                                        .queryParameter("type", "cnpj"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "8");
                                                entity.stringType("name", "Mineradora Vermelha S.A.");
                                                entity.stringType("document", "44555666000181");
                                                entity.stringType("documentType", "cnpj");
                                                entity.integerType("score", 92);
                                                entity.stringValue("riskLevel", "Critico");

                                                // ASG Score critico com breakdown de multiplas fontes
                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 92);
                                                    asg.stringValue("riskLevel", "Critico");
                                                    asg.integerType("totalOcorrencias", 8);
                                                    // Breakdown com 2 fontes
                                                    asg.array("breakdown", arr -> {
                                                        arr.object(buildFullBreakdown("Embargos IBAMA", 100, 0.5, 4));
                                                        arr.object(buildFullBreakdown("Autos de Infracao IBAMA", 80, 0.35, 4));
                                                    });
                                                });

                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.minArrayLike("embargos", 1, buildFullEmbargo());
                                                    ocorrencias.minArrayLike("autosInfracao", 1, buildFullAutoInfracao());
                                                });

                                                entity.minArrayLike("occurrences", 1, buildFullEmbargo());
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchCriticalRiskWithAsgScore")
    void testSearchCriticalRiskWithAsgScore(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "44555666000181")
                .queryParam("type", "cnpj")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getString("entity.riskLevel"), is("Critico"));
        assertThat(response.jsonPath().getInt("entity.asgScore.score"), greaterThanOrEqualTo(80));
        assertThat(response.jsonPath().getString("entity.asgScore.riskLevel"), is("Critico"));
        assertThat(response.jsonPath().getList("entity.asgScore.breakdown").size(), is(2));
    }

    // ==================== US-ASG-006: Apenas embargos (sem autos) ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchWithOnlyEmbargos(PactBuilder builder) {
        return builder
                .given("an entity with only embargos exists")
                .expectsToReceiveHttpInteraction("a request to search entity with only embargos", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "55666777000199")
                                        .queryParameter("type", "cnpj"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "10");
                                                entity.stringType("name", "Fazenda Antiga Ltda");
                                                entity.stringType("document", "55666777000199");
                                                entity.stringType("documentType", "cnpj");
                                                entity.integerType("score", 15);
                                                entity.stringValue("riskLevel", "Baixo");

                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 15);
                                                    asg.stringValue("riskLevel", "Baixo");
                                                    asg.integerType("totalOcorrencias", 1);
                                                    asg.minArrayLike("breakdown", 1, 1,
                                                            buildFullBreakdown("Embargos IBAMA", 15, 0.5, 1));
                                                });

                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.minArrayLike("embargos", 1, buildFullEmbargo());
                                                    ocorrencias.array("autosInfracao", arr -> {});
                                                });

                                                entity.minArrayLike("occurrences", 1, buildFullEmbargo());
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchWithOnlyEmbargos")
    void testSearchWithOnlyEmbargos(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "55666777000199")
                .queryParam("type", "cnpj")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getList("entity.ocorrencias.embargos"), not(empty()));
        assertThat(response.jsonPath().getList("entity.ocorrencias.autosInfracao"), empty());
        assertThat(response.jsonPath().getList("entity.asgScore.breakdown").size(), is(1));
    }

    // ==================== US-ASG-007: Apenas autos de infracao (sem embargos) ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchWithOnlyAutosInfracao(PactBuilder builder) {
        return builder
                .given("an entity with only autos de infracao exists")
                .expectsToReceiveHttpInteraction("a request to search entity with only autos de infracao", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "98765432100")
                                        .queryParameter("type", "cpf"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", true);
                                            body.object("entity", entity -> {
                                                entity.stringType("id", "20");
                                                entity.stringType("name", "Pescador Irregular");
                                                entity.stringType("document", "98765432100");
                                                entity.stringType("documentType", "cpf");
                                                entity.integerType("score", 20);
                                                entity.stringValue("riskLevel", "Baixo");

                                                entity.object("asgScore", asg -> {
                                                    asg.integerType("score", 20);
                                                    asg.stringValue("riskLevel", "Baixo");
                                                    asg.integerType("totalOcorrencias", 2);
                                                    asg.minArrayLike("breakdown", 1, 1,
                                                            buildFullBreakdown("Autos de Infracao IBAMA", 20, 0.35, 2));
                                                });

                                                entity.object("ocorrencias", ocorrencias -> {
                                                    ocorrencias.array("embargos", arr -> {});
                                                    ocorrencias.minArrayLike("autosInfracao", 1, buildFullAutoInfracao());
                                                });

                                                // Sem embargos, occurrences fica vazio
                                                entity.array("occurrences", arr -> {});
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchWithOnlyAutosInfracao")
    void testSearchWithOnlyAutosInfracao(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "98765432100")
                .queryParam("type", "cpf")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(true));
        assertThat(response.jsonPath().getList("entity.ocorrencias.embargos"), empty());
        assertThat(response.jsonPath().getList("entity.ocorrencias.autosInfracao"), not(empty()));
        assertThat(response.jsonPath().getList("entity.asgScore.breakdown").size(), is(1));
    }

    // ==================== US-007: CNPJ bloqueado por situacao cadastral ====================

    @Pact(consumer = CONSUMER_NAME)
    V4Pact searchCnpjBlockedBySituacaoCadastral(PactBuilder builder) {
        return builder
                .given("CNPJ 75776849000150 has situacao cadastral BAIXADA")
                .expectsToReceiveHttpInteraction("a request to search CNPJ blocked by situacao cadastral", httpBuilder ->
                        httpBuilder
                                .withRequest(request -> request
                                        .method("GET")
                                        .path("/api/search/document")
                                        .queryParameter("document", "75776849000150")
                                        .queryParameter("type", "cnpj"))
                                .willRespondWith(response -> response
                                        .status(200)
                                        .header("Content-Type", "application/json")
                                        .body(LambdaDsl.newJsonBody(body -> {
                                            body.booleanValue("found", false);
                                            body.booleanValue("bloqueadoPorSituacaoCadastral", true);
                                            body.object("situacaoCadastral", situacao -> {
                                                situacao.booleanType("valido", true);
                                                situacao.stringType("situacao", "Baixada");
                                                situacao.stringType("dataConsulta", "2025-12-10T15:00:00.000Z");
                                                situacao.stringType("mensagem", "CNPJ com situacao 'Baixada' na Receita Federal. Analise ASG nao disponivel para empresas inativas.");
                                            });
                                        }).build())))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchCnpjBlockedBySituacaoCadastral")
    void testSearchCnpjBlockedBySituacaoCadastral(MockServer mockServer) {
        Response response = RestAssured.given()
                .baseUri(mockServer.getUrl())
                .queryParam("document", "75776849000150")
                .queryParam("type", "cnpj")
                .when()
                .get("/api/search/document");

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getBoolean("found"), is(false));
        assertThat(response.jsonPath().getBoolean("bloqueadoPorSituacaoCadastral"), is(true));
        assertThat(response.jsonPath().getString("situacaoCadastral.situacao"), is("Baixada"));
        assertThat(response.jsonPath().getString("situacaoCadastral.mensagem"), notNullValue());
        // entity deve ser null quando bloqueado
        assertThat(response.jsonPath().getObject("entity", Object.class), nullValue());
    }
}
