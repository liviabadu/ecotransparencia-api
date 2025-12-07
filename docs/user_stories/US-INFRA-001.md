# US-INFRA-001: Carregar base de dados do IBAMA no startup

## Descrição

**Como** um desenvolvedor
**Quero** que a base do IBAMA disponível em `docs/ibama` seja carregada no banco de dados assim que o serviço for executado
**Para** não fazer consultas à API do IBAMA, e sim ao nosso banco de dados quando precisar de dados do IBAMA

## Contexto

O sistema EcoTransparência utiliza dados públicos de áreas embargadas do IBAMA. Para evitar dependência de APIs externas e garantir performance nas consultas, os dados do arquivo CSV (`docs/ibama/areas_embargadas.csv`) devem ser carregados no banco de dados local durante a inicialização do serviço.

### Características do Dataset

| Característica    | Valor                                    |
|-------------------|------------------------------------------|
| Arquivo           | `docs/ibama/areas_embargadas.csv`        |
| Tamanho           | ~125 MB                                  |
| Registros         | ~88.000 embargos                         |
| Delimitador       | Ponto-e-vírgula (`;`)                    |
| Encoding          | UTF-8                                    |
| Formato de data   | `dd/MM/yyyy HH:mm:ss`                    |

---

## Critérios de Aceitação

### AC-01: Carregar dados automaticamente no startup
- **Dado** que o serviço está iniciando
- **Quando** o banco de dados estiver vazio
- **Então** deve carregar os dados do arquivo CSV automaticamente
- **E** não deve carregar se já houver dados no banco

**Teste:** Verificar que dados são carregados apenas quando banco está vazio

### AC-02: Mapear campos do CSV para entidade Embargo
- **Dado** que uma linha do CSV está sendo processada
- **Então** deve mapear corretamente todos os campos para a entidade `Embargo`
- **E** deve converter datas do formato `dd/MM/yyyy HH:mm:ss` para `LocalDateTime`
- **E** deve converter valores numéricos (áreas, coordenadas) para `BigDecimal`

**Teste:** Validação do mapeamento de campos

### AC-03: Tratar campos vazios ou nulos
- **Dado** que um campo no CSV está vazio
- **Então** deve ser tratado como `null` na entidade
- **E** não deve causar erro de parsing

**Teste:** Validação de campos vazios

### AC-04: Processar em lotes (batch)
- **Dado** que o arquivo possui ~88.000 registros
- **Quando** o carregamento é executado
- **Então** deve processar em lotes de 1.000 registros
- **E** deve fazer commit após cada lote
- **E** deve logar progresso a cada lote

**Teste:** Verificar processamento em batch

### AC-05: Executar apenas em ambiente de desenvolvimento/teste
- **Dado** que o serviço está em ambiente de produção
- **Quando** o startup é executado
- **Então** NÃO deve carregar dados automaticamente (configurável)

**Teste:** Verificar que carga pode ser desabilitada via configuração

### AC-06: Logar estatísticas de carga
- **Dado** que o carregamento foi concluído
- **Então** deve logar:
  - Total de registros processados
  - Total de registros inseridos com sucesso
  - Total de erros (se houver)
  - Tempo total de execução

**Teste:** Verificar logs de estatísticas

---

## Modelo de Dados

### Mapeamento CSV → Entity Embargo

| Campo CSV                 | Campo Entity             | Tipo              | Transformação                    |
|---------------------------|--------------------------|-------------------|----------------------------------|
| SEQ_TAD                   | seqTad                   | Long              | Direto                           |
| NUM_TAD                   | numTad                   | String            | Direto                           |
| SER_TAD                   | serTad                   | String            | Direto                           |
| DAT_EMBARGO               | datEmbargo               | LocalDateTime     | Parse dd/MM/yyyy HH:mm:ss        |
| DAT_ULT_ALTERACAO         | datUltAlteracao          | LocalDateTime     | Parse dd/MM/yyyy HH:mm:ss        |
| COD_UF_TAD                | codUfTad                 | Integer           | Direto                           |
| SIG_UF_TAD                | sigUfTad                 | String            | Direto                           |
| COD_MUNICIPIO_TAD         | codMunicipioTad          | Integer           | Direto                           |
| NOM_MUNICIPIO_TAD         | nomMunicipioTad          | String            | Direto                           |
| NUM_LONGITUDE_TAD         | numLongitudeTad          | BigDecimal        | Parse decimal                    |
| NUM_LATITUDE_TAD          | numLatitudeTad           | BigDecimal        | Parse decimal                    |
| NOME_IMOVEL               | nomeImovel               | String            | Direto                           |
| DES_LOCALIZACAO_TAD       | desLocalizacaoTad        | String            | Direto                           |
| NOME_PESSOA_EMBARGADA     | nomePessoaEmbargada      | String            | Direto                           |
| CPF_CNPJ_EMBARGADO        | cpfCnpjEmbargado         | String            | Direto                           |
| SIT_DESMATAMENTO          | sitDesmatamento          | String            | Direto (D ou N)                  |
| TP_AREA_EMBARGADA         | tpAreaEmbargada          | String            | Direto                           |
| QTD_AREA_EMBARGADA        | qtdAreaEmbargada         | BigDecimal        | Parse decimal (vírgula → ponto)  |
| OPERACAO                  | operacao                 | String            | Direto                           |
| UNID_IBAMA_CONTROLE       | unidIbamaControle        | String            | Direto                           |
| NUM_PROCESSO              | numProcesso              | String            | Direto                           |
| DES_TAD                   | desTad                   | String            | Direto                           |
| NUM_AUTO_INFRACAO         | numAutoInfracao          | String            | Direto                           |
| SER_AUTO_INFRACAO         | serAutoInfracao          | String            | Direto                           |
| QTD_AREA_DESMATADA        | qtdAreaDesmatada         | BigDecimal        | Parse decimal                    |
| DES_INFRACAO              | desInfracao              | String            | Direto                           |
| COD_TIPO_BIOMA            | codTipoBioma             | Integer           | Direto                           |
| DES_TIPO_BIOMA            | desTipoBioma             | String            | Direto                           |

---

## Regras de Negócio

### RN-01: Condição para carga
A carga só deve ocorrer se:
1. A propriedade `app.data.load-on-startup` for `true`
2. O banco de dados estiver vazio (count = 0)

### RN-02: Formato de datas
Datas no CSV estão no formato `"dd/MM/yyyy HH:mm:ss"` com aspas. Devem ser:
1. Removidas as aspas
2. Parseadas para `LocalDateTime`
3. Tratadas como null se vazias ou inválidas

### RN-03: Formato de números decimais
Números podem usar vírgula como separador decimal (padrão brasileiro):
1. Substituir vírgula por ponto
2. Parsear para `BigDecimal`
3. Tratar como null se vazios

### RN-04: Processamento em batch
- Tamanho do lote: 1.000 registros
- Commit após cada lote para evitar estouro de memória
- Clear do EntityManager após cada lote

---

## Implementação

### Configuração (application.properties)

```properties
# Habilitar carga de dados no startup
app.data.load-on-startup=true

# Caminho do arquivo CSV
app.data.csv-path=docs/ibama/areas_embargadas.csv

# Tamanho do lote para processamento
app.data.batch-size=1000
```

### IbamaDataLoader.java

```java
@ApplicationScoped
public class IbamaDataLoader {

    @ConfigProperty(name = "app.data.load-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.csv-path", defaultValue = "docs/ibama/areas_embargadas.csv")
    String csvPath;

    @ConfigProperty(name = "app.data.batch-size", defaultValue = "1000")
    int batchSize;

    @Inject
    EmbargoRepository repository;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("Data loading disabled");
            return;
        }
        if (repository.count() > 0) {
            Log.info("Database already populated, skipping load");
            return;
        }
        loadData();
    }

    @Transactional
    void loadData() {
        // Implementação do carregamento em batch
    }
}
```

---

## Testes Automatizados

### Testes de Unidade

| Classe                  | Método                              | Descrição                              |
|-------------------------|-------------------------------------|----------------------------------------|
| IbamaDataLoaderTest     | testLoadWhenDatabaseEmpty()         | Carrega quando banco vazio             |
| IbamaDataLoaderTest     | testSkipLoadWhenDatabasePopulated() | Não carrega quando já há dados         |
| IbamaDataLoaderTest     | testSkipLoadWhenDisabled()          | Não carrega quando desabilitado        |
| CsvParserTest           | testParseDateFormat()               | Parse de data dd/MM/yyyy HH:mm:ss      |
| CsvParserTest           | testParseDecimalWithComma()         | Parse de decimal com vírgula           |
| CsvParserTest           | testParseEmptyFields()              | Tratamento de campos vazios            |
| CsvParserTest           | testMapToEmbargo()                  | Mapeamento CSV → Entity                |

### Teste de Integração

```java
@QuarkusTest
class IbamaDataLoaderIntegrationTest {

    @Inject
    EmbargoRepository repository;

    @Test
    void testDataLoadedOnStartup() {
        // Verificar que dados foram carregados
        assertTrue(repository.count() > 0);
    }
}
```

---

## Dependências

- **Entity Embargo:** Deve suportar todos os campos do CSV
- **EmbargoRepository:** Deve suportar operações em batch
- **Quarkus Scheduler:** Para observar evento de startup

---

## Checklist de Implementação

- [x] Entity Embargo já possui todos os campos necessários
- [x] Criar classe `IbamaDataLoader` com `@Observes StartupEvent`
- [x] Implementar parser de CSV com OpenCSV
- [x] Implementar processamento em batch com commit intermediário
- [x] Adicionar configurações no `application.properties`
- [x] Verificar que testes de contrato continuam passando (6 testes OK)
- [ ] Criar testes de unidade para parser (futuro)
- [ ] Criar teste de integração para carga (futuro)
- [ ] Documentar configurações no README (futuro)

---

## Considerações de Performance

### Estratégias para otimização

1. **Batch Insert:** Inserir registros em lotes de 1.000
2. **Clear EntityManager:** Limpar contexto de persistência após cada lote
3. **Disable Hibernate Statistics:** Desabilitar coleta de estatísticas durante carga
4. **Native Insert (opcional):** Usar SQL nativo para maior performance

### Tempo estimado de carga

| Registros | Tempo estimado |
|-----------|----------------|
| 10.000    | ~5 segundos    |
| 50.000    | ~25 segundos   |
| 88.000    | ~45 segundos   |

---

## Logs esperados

```
INFO  [IbamaDataLoader] Starting data load from docs/ibama/areas_embargadas.csv
INFO  [IbamaDataLoader] Processing batch 1/89 (1000 records)
INFO  [IbamaDataLoader] Processing batch 2/89 (2000 records)
...
INFO  [IbamaDataLoader] Processing batch 89/89 (88192 records)
INFO  [IbamaDataLoader] Data load completed:
INFO  [IbamaDataLoader]   - Total records: 88192
INFO  [IbamaDataLoader]   - Inserted: 88192
INFO  [IbamaDataLoader]   - Errors: 0
INFO  [IbamaDataLoader]   - Time: 42.3 seconds
```
