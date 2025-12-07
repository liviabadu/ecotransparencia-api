package br.com.ecotransparencia.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.VerificationReports;
import au.com.dius.pact.provider.junitsupport.loader.PactFilter;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.repository.EmbargoRepository;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Provider contract verification test.
 *
 * Currently testing:
 * - US-001 (Search by CNPJ)
 * - US-002 (Search by CPF)
 * - US-003 (Search by Name)
 * - US-005 (Critical Risk Level)
 *
 * Filter pattern matches interaction descriptions.
 * To enable more interactions, update the @PactFilter regex.
 */
@QuarkusTest
@Provider("EcoTransparenciaBackend")
@PactFolder("src/test/resources/pacts")
@PactFilter(".*(CNPJ|CPF|name|critical).*")  // US-001 to US-005: CNPJ, CPF, name search and critical risk
@VerificationReports({"console"})
class ProviderPactVerificationTest {

    private EmbargoRepository getRepository() {
        return Arc.container().instance(EmbargoRepository.class).get();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8081));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // ==================== US-001: Search by CNPJ ====================

    @State("an entity with CNPJ 11222333000181 exists")
    void setupEntityWithCnpj() {
        QuarkusTransaction.requiringNew().run(() -> {
            EmbargoRepository repository = getRepository();
            repository.deleteAll();

            Embargo embargo = new Embargo();
            embargo.setSeqTad(1L);
            embargo.setNumTad("12345");
            embargo.setSerTad("A");
            embargo.setNomePessoaEmbargada("Empresa Verde Sustentável Ltda");
            embargo.setCpfCnpjEmbargado("11222333000181");
            embargo.setTpAreaEmbargada("Ambiental IBAMA");
            embargo.setDatEmbargo(java.time.LocalDateTime.of(2023, 6, 15, 0, 0));
            embargo.setDesTad("Advertência por descarte irregular de resíduos");
            embargo.setSigUfTad("SP");
            embargo.setNomMunicipioTad("São Paulo");
            embargo.setSitDesmatamento("N");
            embargo.setQtdAreaEmbargada(java.math.BigDecimal.valueOf(5.5));
            embargo.setNumAutoInfracao("123456");
            embargo.setSerAutoInfracao("A");

            repository.persist(embargo);
        });
    }

    // ==================== US-002: Search by CPF ====================

    @State("a person with CPF 12345678909 exists")
    void setupPersonWithCpf() {
        QuarkusTransaction.requiringNew().run(() -> {
            EmbargoRepository repository = getRepository();
            repository.deleteAll();

            Embargo embargo = new Embargo();
            embargo.setSeqTad(5L);
            embargo.setNumTad("890123");
            embargo.setSerTad("A");
            embargo.setNomePessoaEmbargada("João da Silva Teste");
            embargo.setCpfCnpjEmbargado("12345678909");
            embargo.setTpAreaEmbargada("Ambiental IBAMA");
            embargo.setDatEmbargo(java.time.LocalDateTime.of(2024, 2, 15, 0, 0));
            embargo.setDesTad("Auto de infração por pesca ilegal em área protegida");
            embargo.setSigUfTad("AM");
            embargo.setNomMunicipioTad("Manaus");
            embargo.setSitDesmatamento("N");
            embargo.setQtdAreaEmbargada(java.math.BigDecimal.valueOf(0));
            embargo.setNumAutoInfracao("890123");
            embargo.setSerAutoInfracao("A");
            embargo.setDesTipoBioma("Amazônia");
            embargo.setCodTipoBioma(4);

            repository.persist(embargo);
        });
    }

    // ==================== US-003: Search by Name ====================

    @State("an entity with name containing \"Empresa Verde\" exists")
    void setupEntityWithName() {
        QuarkusTransaction.requiringNew().run(() -> {
            EmbargoRepository repository = getRepository();
            repository.deleteAll();

            Embargo embargo = new Embargo();
            embargo.setSeqTad(1L);
            embargo.setNumTad("12345");
            embargo.setSerTad("A");
            embargo.setNomePessoaEmbargada("Empresa Verde Sustentável Ltda");
            embargo.setCpfCnpjEmbargado("11222333000181");
            embargo.setTpAreaEmbargada("Ambiental IBAMA");
            embargo.setDatEmbargo(java.time.LocalDateTime.of(2023, 6, 15, 0, 0));
            embargo.setDesTad("Advertência por descarte irregular de resíduos");
            embargo.setSigUfTad("SP");
            embargo.setNomMunicipioTad("São Paulo");
            embargo.setSitDesmatamento("N");
            embargo.setQtdAreaEmbargada(java.math.BigDecimal.valueOf(5.5));
            embargo.setNumAutoInfracao("123456");
            embargo.setSerAutoInfracao("A");

            repository.persist(embargo);
        });
    }

    // ==================== US-004: Not Found scenarios (disabled) ====================

    @State("no entity with CNPJ 00000000000000 exists")
    void setupNoEntityWithCnpj() {
        // US-004: Will be enabled with US-001
    }

    @State("no entity with name \"Entidade Inexistente XYZ\" exists")
    void setupNoEntityWithName() {
        QuarkusTransaction.requiringNew().run(() -> {
            EmbargoRepository repository = getRepository();
            repository.deleteAll();
            // No data inserted - entity should not be found
        });
    }

    // ==================== US-005: Critical Risk ====================

    @State("an entity with critical risk level exists")
    void setupEntityWithCriticalRisk() {
        QuarkusTransaction.requiringNew().run(() -> {
            EmbargoRepository repository = getRepository();
            repository.deleteAll();

            // Create multiple embargos to achieve critical risk score (>=80)
            // Each embargo: 10 (base) + 5 (deforestation) + 3 (Amazônia) + 10 (area) = 28 points
            // 4 embargos = 112 points, capped at 100
            for (int i = 0; i < 4; i++) {
                Embargo embargo = new Embargo();
                embargo.setSeqTad(8L + i);
                embargo.setNumTad("45678" + i);
                embargo.setSerTad("A");
                embargo.setNomePessoaEmbargada("Mineradora Vermelha S.A.");
                embargo.setCpfCnpjEmbargado("44555666000181");
                embargo.setTpAreaEmbargada("Ambiental IBAMA");
                embargo.setDatEmbargo(java.time.LocalDateTime.of(2024, 6, 1, 0, 0));
                embargo.setDesTad("Embargo total de atividades por contaminação");
                embargo.setSigUfTad("PA");
                embargo.setNomMunicipioTad("Altamira");
                embargo.setSitDesmatamento("D");                                      // +5 points
                embargo.setCodTipoBioma(4);                                           // Amazônia +3 points
                embargo.setDesTipoBioma("Amazônia");
                embargo.setQtdAreaEmbargada(java.math.BigDecimal.valueOf(150));       // +10 points (max)
                embargo.setNumAutoInfracao("456780");
                embargo.setSerAutoInfracao("A");

                repository.persist(embargo);
            }
        });
    }

}
