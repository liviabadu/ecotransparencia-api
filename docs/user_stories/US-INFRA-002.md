# US-INFRA-002: Expor documentação interativa da API via Swagger UI

## Descrição

**Como** um desenvolvedor ou consumidor da API
**Quero** acessar uma interface interativa do Swagger UI
**Para** explorar, testar e entender os endpoints disponíveis na API EcoTransparência

## Contexto

A API EcoTransparência expõe endpoints REST para consulta de entidades com áreas embargadas pelo IBAMA. Para facilitar a integração por times de frontend, parceiros externos e desenvolvedores, é essencial disponibilizar uma documentação interativa baseada na especificação OpenAPI (Swagger).

O Quarkus oferece suporte nativo via extensão `quarkus-smallrye-openapi`, que gera automaticamente a especificação OpenAPI e disponibiliza o Swagger UI.

---

## Critérios de Aceitação

### AC-01: Swagger UI acessível via navegador
- **Dado** que o serviço está em execução
- **Quando** o usuário acessar `/q/swagger-ui`
- **Então** deve exibir a interface gráfica do Swagger UI
- **E** deve listar todos os endpoints disponíveis na API

**Teste:** Acessar `http://localhost:8080/q/swagger-ui` e verificar carregamento da UI

### AC-02: Especificação OpenAPI disponível em JSON/YAML
- **Dado** que o serviço está em execução
- **Quando** o usuário acessar `/q/openapi`
- **Então** deve retornar a especificação OpenAPI em formato YAML
- **E** deve incluir informações de título, versão e descrição da API

**Teste:** `curl http://localhost:8080/q/openapi` deve retornar documento YAML válido

### AC-03: Endpoints documentados com descrições
- **Dado** que um endpoint possui anotações OpenAPI
- **Quando** a especificação for gerada
- **Então** deve incluir:
  - Descrição do endpoint
  - Parâmetros de entrada com tipos e exemplos
  - Códigos de resposta possíveis
  - Exemplos de request/response

**Teste:** Verificar que `/api/search/document` possui documentação completa

### AC-04: Swagger UI habilitado apenas em desenvolvimento
- **Dado** que o serviço está em produção (`%prod`)
- **Quando** o usuário tentar acessar `/q/swagger-ui`
- **Então** o endpoint NÃO deve estar disponível
- **E** deve retornar 404 Not Found

**Teste:** Configurar perfil `prod` e verificar que Swagger UI não está acessível

### AC-05: Informações da API configuradas
- **Dado** que a especificação OpenAPI é gerada
- **Então** deve incluir:
  - Título: "EcoTransparência API"
  - Versão: "1.0.0"
  - Descrição: Informações sobre o propósito da API
  - Contato e licença (opcional)

**Teste:** Verificar campos `info` na especificação OpenAPI

---

## Implementação

### Dependência Maven (pom.xml)

```xml
<!-- OpenAPI e Swagger UI -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

### Configuração (application.properties)

```properties
# OpenAPI - Informações da API
quarkus.smallrye-openapi.info-title=EcoTransparencia API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=API para consulta de entidades com areas embargadas pelo IBAMA
quarkus.smallrye-openapi.info-contact-name=EcoTransparencia Team
quarkus.smallrye-openapi.info-license-name=Apache 2.0
quarkus.smallrye-openapi.info-license-url=https://www.apache.org/licenses/LICENSE-2.0

# Swagger UI - Habilitado apenas em dev/test
quarkus.swagger-ui.always-include=false
%dev.quarkus.swagger-ui.always-include=true
%test.quarkus.swagger-ui.always-include=true
```

### Anotações nos Resources

```java
@Path("/api/search")
@Tag(name = "Search", description = "Endpoints para busca de entidades embargadas")
public class SearchResource {

    @GET
    @Path("/document")
    @Operation(
        summary = "Buscar entidade por documento",
        description = "Busca uma entidade (pessoa física ou jurídica) pelo CPF ou CNPJ"
    )
    @APIResponse(
        responseCode = "200",
        description = "Busca realizada com sucesso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SearchResponse.class)
        )
    )
    @Parameter(name = "document", description = "CPF (11 dígitos) ou CNPJ (14 dígitos)", required = true)
    @Parameter(name = "type", description = "Tipo do documento: cpf ou cnpj", required = true)
    public SearchResponse searchByDocument(
        @QueryParam("document") String document,
        @QueryParam("type") String type
    ) {
        // ...
    }
}
```

---

## URLs Disponíveis

| URL                          | Descrição                              | Ambiente     |
|------------------------------|----------------------------------------|--------------|
| `/q/swagger-ui`              | Interface gráfica Swagger UI           | dev, test    |
| `/q/openapi`                 | Especificação OpenAPI (YAML)           | dev, test    |
| `/q/openapi?format=json`     | Especificação OpenAPI (JSON)           | dev, test    |

---

## Testes Automatizados

### Teste de Integração

```java
@QuarkusTest
class SwaggerUITest {

    @Test
    void testSwaggerUIAvailable() {
        given()
            .when().get("/q/swagger-ui")
            .then()
            .statusCode(200)
            .contentType(containsString("text/html"));
    }

    @Test
    void testOpenAPISpecAvailable() {
        given()
            .when().get("/q/openapi")
            .then()
            .statusCode(200)
            .body(containsString("openapi:"))
            .body(containsString("EcoTransparencia API"));
    }

    @Test
    void testSearchEndpointDocumented() {
        given()
            .when().get("/q/openapi?format=json")
            .then()
            .statusCode(200)
            .body("paths./api/search/document.get.summary", notNullValue())
            .body("paths./api/search/document.get.parameters", hasSize(2));
    }
}
```

---

## Checklist de Implementação

- [x] Adicionar dependência `quarkus-smallrye-openapi` no `pom.xml`
- [x] Configurar informações da API no `application.properties`
- [x] Configurar Swagger UI para estar disponível apenas em dev/test
- [x] Adicionar anotações `@Operation`, `@Tag`, `@Parameter` nos Resources
- [x] Adicionar anotações `@Schema` nos DTOs
- [ ] Criar testes de integração para validar Swagger UI
- [x] Verificar que endpoints existentes aparecem documentados
- [x] Testar acesso via navegador em `localhost:8080/q/swagger-ui`

---

## Dependências

- **Quarkus SmallRye OpenAPI:** Extensão do Quarkus para geração de especificação OpenAPI
- **SearchResource:** Resource principal que deve ser documentado
- **DTOs (SearchResponse, EntityDto, etc.):** Devem ter anotações `@Schema` para documentação

---

## Considerações de Segurança

### Produção
O Swagger UI **NÃO** deve estar acessível em produção por padrão, pois:
1. Expõe informações sobre a estrutura da API
2. Permite testar endpoints diretamente
3. Pode revelar parâmetros e schemas internos

A configuração `quarkus.swagger-ui.always-include=false` garante que o Swagger UI só estará disponível quando explicitamente habilitado via perfil.

### Ambientes não-produtivos
Em ambientes de desenvolvimento, staging ou homologação, o Swagger UI pode ser habilitado para facilitar testes e integração.

---

## Referências

- [Quarkus OpenAPI Guide](https://quarkus.io/guides/openapi-swaggerui)
- [SmallRye OpenAPI](https://github.com/smallrye/smallrye-open-api)
- [OpenAPI Specification 3.0](https://swagger.io/specification/)
- [MicroProfile OpenAPI](https://microprofile.io/project/eclipse/microprofile-open-api)
