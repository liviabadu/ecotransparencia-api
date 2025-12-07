package br.com.ecotransparencia.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.VerificationReports;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusTest
@Provider("EcoTransparenciaBackend")
@PactFolder("src/test/resources/pacts")
@VerificationReports({"console"})
class ProviderPactVerificationTest {

    @BeforeEach
    void before(PactVerificationContext context) {
        // Quarkus test HTTP server usually runs on 8081
        context.setTarget(new HttpTestTarget("localhost", 8081));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("an entity with CNPJ 11222333000181 exists")
    void setupEntityWithCnpj() {
        // TODO: Setup test data for entity with CNPJ 11222333000181
    }

    @State("a person with CPF 12345678909 exists")
    void setupPersonWithCpf() {
        // TODO: Setup test data for person with CPF 12345678909
    }

    @State("an entity with critical risk level exists")
    void setupEntityWithCriticalRisk() {
        // TODO: Setup test data for entity with critical risk level
    }

    @State("an entity with name containing \"Empresa Verde\" exists")
    void setupEntityWithName() {
        // TODO: Setup test data for entity with name containing "Empresa Verde"
    }

    @State("no entity with CNPJ 00000000000000 exists")
    void setupNoEntityWithCnpj() {
        // No setup needed - entity should not exist
    }

    @State("no entity with name \"Entidade Inexistente XYZ\" exists")
    void setupNoEntityWithName() {
        // No setup needed - entity should not exist
    }
}
