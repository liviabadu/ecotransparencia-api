# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

The `README.md` covers the business domain (IBAMA data, Score ASG, endpoints) and stack rationale — read it for product context. This file documents only what's not obvious from the source.

## Common commands

```bash
# Dev mode with hot-reload + Dev UI at http://localhost:8080/q/dev
./mvnw quarkus:dev

# Full test suite (unit + service + Pact consumer/provider)
./mvnw test

# Single test class
./mvnw -Dtest=SearchServiceTest test

# Single test method
./mvnw -Dtest=SearchServiceTest#shouldFindByCnpj test

# Pact contract tests only (consumers generate JSON in target/pacts/)
./mvnw -Dtest='*ConsumerPactTest' test
./mvnw -Dtest=ProviderPactVerificationTest test

# Production package (target/quarkus-app/, run with java -jar quarkus-run.jar)
./mvnw package

# Native image (requires GraalVM); flips skipITs and runs IT suite
./mvnw package -Pnative

# Local container build (multi-stage, copies CSV into image)
docker build -t ecotransparencia-api .
```

Swagger UI is exposed in every profile at `/q/swagger-ui`.

## Architecture notes that aren't obvious from one file

### H2 is populated from CSV at boot, not migrated

There is no Flyway/Liquibase. `quarkus.hibernate-orm.database.generation=drop-and-create` recreates the schema on every startup, and two `@Startup` beans in `startup/` then bulk-load it from CSV:

- `IbamaDataLoader` reads `app.data.csv-path` (default `docs/ibama/areas_embargadas.csv`, ~125 MB).
- `IbamaAutoInfracaoDataLoader` reads every `auto_infracao_ano_YYYY.csv` under `app.data.autos-csv-dir`.

Both are **gated by profile-specific properties** in `application.properties`:

| Profile | `load-on-startup` (embargos) | `load-autos-on-startup` |
|---------|------------------------------|--------------------------|
| default | `false` | `true` |
| `%dev`  | `true`  | `true` |
| `%prod` | `true`  | `true` (paths point to `/deployments/data/...`) |
| `%test` | `false` | `false` (so tests start instantly) |

Implication: `./mvnw quarkus:dev` will spend 30–90s loading CSVs before serving requests; `quarkus.startup-timeout=300s` is set for that reason. If you add a test that needs real data, seed it explicitly — don't flip `%test` loading on.

### `ReceitaFederalService` swap is build-time, not runtime

There are two beans implementing `ReceitaFederalService`:

- `ReceitaFederalServiceStub` — `@DefaultBean`, validates check digits and always returns `ATIVA`/`REGULAR`. Used in dev/test.
- `ReceitaFederalServiceImpl` — `@IfBuildProperty(name="ecotransparencia.receita-federal.use-real-api", stringValue="true")`. Calls the CNPJA API via `CnpjaApiClient` (`@RegisterRestClient`).

The selection happens at **build time** via Quarkus ARC, not via Spring-style `@Profile`. To force the real client locally: `./mvnw quarkus:dev -Decotransparencia.receita-federal.use-real-api=true`. Prod sets it via `%prod.` override.

When mocking in unit tests, mock the `ReceitaFederalService` interface — both impls share it.

### Pact contract testing has two halves

Consumer side (`src/test/java/.../contract/*ConsumerPactTest.java`) generates pact JSON into `target/pacts/`. The provider side (`ProviderPactVerificationTest`) reads pact files from `src/test/resources/pacts/` (committed) and replays them against the running Quarkus test app. If you change a contract, regenerate from the consumer test, then copy the JSON from `target/pacts/` into `src/test/resources/pacts/` so the provider verification picks it up.

### Cloud Run deploy assumes the CSV is in the image

`cloudbuild.yaml` step 1 downloads `gs://ecotransparencia2/areas_embargadas.csv` to `docs/ibama/`, so the Dockerfile's `COPY docs/ibama/areas_embargadas.csv /deployments/data/...` works. A local `docker build` only succeeds if you've placed that CSV (or an empty stub) at the same path. The deploy sets `QUARKUS_PROFILE=prod`, which is what flips `use-real-api` to `true` and points data paths at `/deployments/data/`.

`quarkus.http.port=${PORT:8080}` honors Cloud Run's `PORT` injection — don't hardcode 8080 in new resources or healthchecks.

## Where to look for context

- `docs/user_stories/US-*.md` — feature specs (US-001 through US-007 + US-INFRA-*). These are the source of truth for behavior; tests reference them by ID (e.g. `// US-007`).
- `docs/ibama/dicionario.md` — column dictionary for the IBAMA CSV; consult before changing CSV-parsing logic in the loaders.
- `.run/Cloud Run_ Run Locally.run.xml` — IntelliJ run config showing how the team runs the prod-equivalent container locally.
