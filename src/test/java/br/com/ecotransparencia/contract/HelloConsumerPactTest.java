package br.com.ecotransparencia.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;

// This is an optional example consumer test kept for reference.
// The project acts as a provider only, so we disable this test by default
// to avoid generating local pacts in regular builds.
@ExtendWith(PactConsumerTestExt.class)
@Disabled("Provider-only project: consumer example kept for reference")
class HelloConsumerPactTest {

    @Pact(consumer = "hello-consumer", provider = "ecotransparencia-api")
    RequestResponsePact pactHello(PactDslWithProvider builder) {
        return builder
                .uponReceiving("request to GET /hello")
                .path("/hello")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("Hello from Quarkus REST")
                .toPact();
    }

    @Test
    @PactTestFor(providerName = "ecotransparencia-api")
    void verifyConsumerAgainstMock(MockServer mockServer) {
        RestAssured.given()
                .baseUri(mockServer.getUrl())
                .when()
                .get("/hello")
                .then()
                .statusCode(200)
                .body(equalTo("Hello from Quarkus REST"));
    }
}
