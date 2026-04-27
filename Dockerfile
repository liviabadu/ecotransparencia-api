# Multi-stage Dockerfile para a EcoTransparencia API.
#
# Os arquivos em docs/ sao baixados do GCS pelo cloudbuild antes do build
# (ver cloudbuild.yaml step `download-data`). Para build local completo,
# certifique-se de que docs/ contem todas as fontes esperadas.
#
# Build: docker build -t ecotransparencia-api .
# Run:   docker run --rm -p 8080:8080 \
#          -e DB_URL=jdbc:postgresql://host.docker.internal:5432/ecotransparencia \
#          -e DB_USER=postgres -e DB_PASSWORD=... \
#          -e QUARKUS_PROFILE=prod \
#          ecotransparencia-api

# ============================================================================
# Stage 1: Build com Maven 3.9 + Temurin 21
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# pom.xml separado para aproveitar layer cache (deps mudam menos que codigo)
COPY pom.xml .

# Pre-baixa dependencias. GeoTools vem do repo OSGeo, configurado no pom.xml.
# -B = batch mode (sem cores/spinner), output mais limpo no log do cloudbuild.
RUN mvn dependency:go-offline -B || true

# Copia codigo e empacota
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================================================
# Stage 2: Runtime UBI9 + OpenJDK 21
# ============================================================================
FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

# Copia o build do Quarkus (fast-jar layout). Owner 185 = jboss user no UBI.
COPY --from=build --chown=185 /app/target/quarkus-app/lib/      /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar     /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/      /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/  /deployments/quarkus/

# Copia fontes de dados. Os paths espelham o que esta em
# %prod.app.data.* em src/main/resources/application.properties.
COPY --chown=185 docs/ibama/areas_embargadas.csv  /deployments/data/areas_embargadas.csv
COPY --chown=185 docs/ibama/autos/                /deployments/data/autos/
COPY --chown=185 docs/adm_publica/                /deployments/data/adm_publica/
COPY --chown=185 docs/mte/                        /deployments/data/mte/
COPY --chown=185 docs/icmbio/                     /deployments/data/icmbio/

EXPOSE 8080
USER 185

ENV JAVA_OPTS_APPEND="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
