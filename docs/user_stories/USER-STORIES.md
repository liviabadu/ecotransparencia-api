# Histórias de Usuário - EcoTransparência API

Este documento descreve as histórias de usuário derivadas do contrato Pact entre o **EcoTransparenciaFrontend** (consumidor) e o **EcoTransparenciaBackend** (provedor), utilizando dados reais de embargos ambientais do IBAMA.

---

## Épico: Consulta de Áreas Embargadas pelo IBAMA

O sistema EcoTransparência permite que usuários consultem informações sobre pessoas físicas e jurídicas que possuem áreas embargadas registradas no sistema de fiscalização do IBAMA.

**Fonte de Dados:** Áreas Embargadas - IBAMA (Instituto Brasileiro do Meio Ambiente e dos Recursos Naturais Renováveis)

### Características do Dataset

- **Volume:** Dataset de grande escala (~125MB) com registros históricos desde 1987
- **Formato:** CSV delimitado por ponto-e-vírgula (`;`)
- **Abrangência:** Todos os estados brasileiros (UFs)
- **Biomas:** Amazônia, Mata Atlântica, Cerrado, Caatinga, Pampa, Costeiro e Marinho
- **Georreferenciamento:** Coordenadas decimais e WKT para visualização espacial

---

## US-001: Buscar entidade por CNPJ

**Como** um usuário do sistema
**Quero** pesquisar uma empresa pelo seu CNPJ
**Para** verificar se ela possui áreas embargadas pelo IBAMA

### Critérios de Aceitação

- [ ] O sistema deve aceitar um CNPJ válido (14 dígitos) como parâmetro de busca
- [ ] O sistema deve consultar o campo `CPF_CNPJ_EMBARGADO` na base de dados
- [ ] O sistema deve retornar os dados da entidade quando encontrada
- [ ] O sistema deve retornar `found: true` quando a entidade existir
- [ ] O sistema deve retornar `found: false` quando a entidade não existir
- [ ] O sistema deve agregar todas as ocorrências (embargos) da mesma entidade

### Endpoint

```
GET /api/search/document?document={cnpj}&type=cnpj
```

### Exemplo de Resposta (Sucesso)

```json
{
  "found": true,
  "entity": {
    "id": "1872430",
    "name": "PREF MUN DE TERESOPOLIS",
    "document": "29138369000147",
    "documentType": "cnpj",
    "riskLevel": "Baixo",
    "score": 15,
    "occurrences": [
      {
        "id": "1872430",
        "category": "Atividade",
        "date": "1990-03-27T00:00:00.000Z",
        "description": "Exercer atividades potencialmente degradadoras do meio ambiente.",
        "source": "IBAMA",
        "sourceUrl": "https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php",
        "status": "Ativo",
        "location": {
          "uf": "RJ",
          "municipio": "Teresópolis",
          "imovel": "Prefeitura Municipal de Teresópolis"
        },
        "autoInfracao": "598101-A"
      }
    ]
  }
}
```

---

## US-002: Buscar entidade por CPF

**Como** um usuário do sistema
**Quero** pesquisar uma pessoa física pelo seu CPF
**Para** verificar se ela possui áreas embargadas pelo IBAMA

### Critérios de Aceitação

- [ ] O sistema deve aceitar um CPF válido (11 dígitos) como parâmetro de busca
- [ ] O sistema deve consultar o campo `CPF_CNPJ_EMBARGADO` na base de dados
- [ ] O sistema deve retornar os dados da pessoa quando encontrada
- [ ] O sistema deve retornar `found: true` quando a pessoa existir
- [ ] O sistema deve retornar `found: false` quando a pessoa não existir

### Endpoint

```
GET /api/search/document?document={cpf}&type=cpf
```

### Exemplo de Resposta (Sucesso)

```json
{
  "found": true,
  "entity": {
    "id": "1600976",
    "name": "RAIMUNDO CIRINO DE SOUZA",
    "document": "02330709234",
    "documentType": "cpf",
    "riskLevel": "Alto",
    "score": 65,
    "occurrences": [
      {
        "id": "1600976",
        "category": "Desmatamento",
        "date": "1988-08-03T00:00:00.000Z",
        "description": "Por ter desmatado aproximadamente 10,00 ha de mata, atingindo as margens do córrego sem nome, na Fazenda Mautra (Estrada Mutuom, Apiacás, município de Alta Floresta-MT), sem autorização do IBDF no ato da fiscalização.",
        "source": "IBAMA",
        "sourceUrl": "https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php",
        "status": "Ativo",
        "location": {
          "uf": "MT",
          "municipio": "Apiacás",
          "imovel": "Fazenda Mautra"
        },
        "biome": "Amazônia",
        "areaEmbargada": 10.0,
        "autoInfracao": "789209-A",
        "desmatamento": true
      }
    ]
  }
}
```

---

## US-003: Buscar entidade por nome

**Como** um usuário do sistema
**Quero** pesquisar entidades pelo nome
**Para** encontrar empresas ou pessoas quando não possuo o documento

### Critérios de Aceitação

- [ ] O sistema deve aceitar um termo de busca (nome parcial ou completo)
- [ ] O sistema deve consultar o campo `NOME_PESSOA_EMBARGADA` na base de dados
- [ ] O sistema deve realizar busca parcial (contém o termo, case-insensitive)
- [ ] O sistema deve retornar `found: true` quando pelo menos uma entidade for encontrada
- [ ] O sistema deve retornar `found: false` quando nenhuma entidade corresponder

### Endpoint

```
GET /api/search/name?name={termo}
```

---

## US-004: Exibir entidade não encontrada

**Como** um usuário do sistema
**Quero** receber feedback claro quando uma entidade não for encontrada
**Para** saber que a busca foi executada mas não há registros de embargo

### Critérios de Aceitação

- [ ] O sistema deve retornar status HTTP 200 mesmo quando a entidade não existe
- [ ] O sistema deve retornar `found: false` no corpo da resposta
- [ ] O sistema NÃO deve retornar o objeto `entity` quando não encontrado

### Exemplo de Resposta (Não Encontrado)

```json
{
  "found": false
}
```

---

## US-005: Identificar entidades com risco crítico

**Como** um analista de compliance
**Quero** visualizar claramente entidades com nível de risco crítico
**Para** priorizar análises e tomadas de decisão

### Critérios de Aceitação

- [ ] O sistema deve calcular o score de risco baseado em:
  - Quantidade de embargos ativos
  - Área total embargada (hectares)
  - Recência dos embargos
  - Tipo de infração (desmatamento = maior peso)
  - Bioma afetado (Amazônia = maior peso)
- [ ] O campo `riskLevel` deve refletir a classificação de risco
- [ ] Embargos com `SIT_DESMATAMENTO = 'D'` devem ter maior peso no cálculo

### Níveis de Risco

| Score | Nível de Risco |
|-------|----------------|
| 0-25  | Baixo          |
| 26-50 | Médio          |
| 51-79 | Alto           |
| 80+   | Crítico        |

---

## US-006: Visualizar detalhes do embargo

**Como** um usuário do sistema
**Quero** ver os detalhes completos de cada embargo
**Para** entender a natureza e gravidade da infração ambiental

### Critérios de Aceitação

- [ ] Cada ocorrência deve exibir os campos do termo de embargo
- [ ] Deve incluir localização geográfica quando disponível
- [ ] Deve incluir informações do auto de infração vinculado
- [ ] Deve indicar o bioma afetado

### Campos da Ocorrência

| Campo API       | Campo IBAMA              | Descrição                                |
|-----------------|--------------------------|------------------------------------------|
| id              | SEQ_TAD                  | Identificador único do termo de embargo  |
| category        | TP_AREA_EMBARGADA        | Tipo de área/dano embargado              |
| date            | DAT_EMBARGO              | Data da lavratura do embargo             |
| description     | DES_TAD                  | Descrição/justificativa do embargo       |
| source          | "IBAMA"                  | Fonte fixa                               |
| sourceUrl       | -                        | URL para consulta (construída)           |
| status          | -                        | Calculado: "Ativo" ou "Baixado"          |

---

## US-007: Filtrar por bioma

**Como** um analista ambiental
**Quero** filtrar embargos por bioma
**Para** focar análises em regiões específicas

### Critérios de Aceitação

- [ ] O sistema deve permitir filtro pelo campo `DES_TIPO_BIOMA`
- [ ] Biomas disponíveis: Amazônia, Mata Atlântica, Cerrado, Caatinga, Pampa, Costeiro e Marinho

### Endpoint (futuro)

```
GET /api/search/document?document={doc}&type={type}&biome={bioma}
```

---

## US-008: Filtrar por UF/Município

**Como** um usuário do sistema
**Quero** filtrar embargos por localização
**Para** analisar entidades em regiões específicas

### Critérios de Aceitação

- [ ] O sistema deve permitir filtro por UF (`SIG_UF_TAD`)
- [ ] O sistema deve permitir filtro por município (`NOM_MUNICIPIO_TAD`)

### Endpoint (futuro)

```
GET /api/search/document?document={doc}&type={type}&uf={uf}&city={municipio}
```

---

## US-009: Visualizar áreas embargadas no mapa

**Como** um analista ambiental
**Quero** visualizar a geometria das áreas embargadas em um mapa
**Para** compreender a extensão espacial e localização exata dos embargos

### Critérios de Aceitação

- [ ] O sistema deve retornar coordenadas geográficas quando disponíveis (`NUM_LATITUDE_TAD`, `NUM_LONGITUDE_TAD`)
- [ ] O sistema deve retornar geometria WKT quando disponível (`WKT_GEOM_AREA_EMBARGADA`)
- [ ] O sistema deve suportar geometrias do tipo MULTIPOLYGON para áreas complexas
- [ ] As coordenadas devem estar no formato decimal (graus)

### Campos Geográficos

| Campo API       | Campo IBAMA                | Descrição                                    |
|-----------------|----------------------------|----------------------------------------------|
| latitude        | NUM_LATITUDE_TAD           | Latitude em formato decimal                  |
| longitude       | NUM_LONGITUDE_TAD          | Longitude em formato decimal                 |
| geometry        | WKT_GEOM_AREA_EMBARGADA    | Geometria WKT (Well-Known Text)              |

### Exemplo de Geometria WKT

```
MULTIPOLYGON (((-49.190015 -26.534576, -49.190022 -26.53457, ...)))
```

---

## US-010: Consultar por operação de fiscalização

**Como** um auditor governamental
**Quero** buscar embargos por operação de fiscalização
**Para** analisar resultados de operações específicas do IBAMA

### Critérios de Aceitação

- [ ] O sistema deve permitir busca pelo campo `OPERACAO`
- [ ] O sistema deve retornar todos os embargos vinculados à operação
- [ ] O sistema deve informar a unidade do IBAMA responsável (`UNID_IBAMA_CONTROLE`)

### Endpoint (futuro)

```
GET /api/search/operation?name={operacao}
```

---

## Modelo de Dados

### Entity (Resposta da API)

| Campo        | Tipo         | Origem IBAMA                | Descrição                                    |
|--------------|--------------|------------------------------|----------------------------------------------|
| id           | String       | SEQ_TAD (primeiro registro)  | Identificador da entidade                    |
| name         | String       | NOME_PESSOA_EMBARGADA        | Nome da empresa ou pessoa física             |
| document     | String       | CPF_CNPJ_EMBARGADO           | CPF (11 dígitos) ou CNPJ (14 dígitos)        |
| documentType | String       | -                            | Tipo do documento: "cpf" ou "cnpj"           |
| riskLevel    | String       | -                            | Nível de risco: Baixo, Médio, Alto, Crítico  |
| score        | Integer      | -                            | Score numérico de risco (0-100)              |
| occurrences  | Occurrence[] | -                            | Lista de embargos                            |

### Occurrence (Embargo)

| Campo           | Tipo    | Origem IBAMA              | Descrição                                      |
|-----------------|---------|---------------------------|------------------------------------------------|
| id              | String  | SEQ_TAD                   | Identificador único do termo de embargo        |
| numTad          | String  | NUM_TAD + SER_TAD         | Número e série do termo de embargo             |
| category        | String  | TP_AREA_EMBARGADA         | Tipo de área embargada                         |
| date            | String  | DAT_EMBARGO               | Data do embargo (ISO 8601)                     |
| description     | String  | DES_TAD                   | Descrição/justificativa do embargo             |
| infracao        | String  | DES_INFRACAO              | Descrição da infração ambiental                |
| source          | String  | -                         | Fonte: "IBAMA"                                 |
| sourceUrl       | String  | -                         | URL para consulta na fonte                     |
| status          | String  | -                         | Status: "Ativo" ou "Baixado"                   |
| desmatamento    | Boolean | SIT_DESMATAMENTO          | true se D (desmatamento), false se N           |
| areaEmbargada   | Decimal | QTD_AREA_EMBARGADA        | Área embargada em hectares                     |
| areaDesmatada   | Decimal | QTD_AREA_DESMATADA        | Área autuada em hectares                       |
| autoInfracao    | String  | NUM_AUTO_INFRACAO + SER   | Número e série do auto de infração             |
| numProcesso     | String  | NUM_PROCESSO              | Número do processo administrativo              |
| operacao        | String  | OPERACAO                  | Nome da operação de fiscalização               |
| unidadeIbama    | String  | UNID_IBAMA_CONTROLE       | Unidade IBAMA responsável                      |

### Location (Localização do Embargo)

| Campo       | Tipo    | Origem IBAMA           | Descrição                                      |
|-------------|---------|------------------------|------------------------------------------------|
| uf          | String  | SIG_UF_TAD             | Sigla da UF                                    |
| codUf       | Integer | COD_UF_TAD             | Código IBGE da UF                              |
| municipio   | String  | NOM_MUNICIPIO_TAD      | Nome do município                              |
| codMunicipio| Integer | COD_MUNICIPIO_TAD      | Código IBGE do município                       |
| imovel      | String  | NOME_IMOVEL            | Nome do imóvel embargado                       |
| descricao   | String  | DES_LOCALIZACAO_TAD    | Descrição textual da localização               |
| latitude    | Decimal | NUM_LATITUDE_TAD       | Latitude decimal                               |
| longitude   | Decimal | NUM_LONGITUDE_TAD      | Longitude decimal                              |
| geometry    | String  | WKT_GEOM_AREA_EMBARGADA| Geometria WKT                                  |

### Biome (Bioma)

| Campo       | Tipo    | Origem IBAMA           | Descrição                                      |
|-------------|---------|------------------------|------------------------------------------------|
| codigo      | Integer | COD_TIPO_BIOMA         | Código do bioma                                |
| nome        | String  | DES_TIPO_BIOMA         | Nome do bioma                                  |

---

## Dicionário de Dados IBAMA - Campos Completos

| Campo IBAMA               | Tipo    | Descrição                                                    |
|---------------------------|---------|--------------------------------------------------------------|
| SEQ_TAD                   | Integer | Chave primária - identificador do termo de embargo           |
| NUM_TAD                   | String  | Número impresso no formulário do termo                       |
| SER_TAD                   | String  | Série do termo de embargo (A, D, E ou vazio)                 |
| DAT_EMBARGO               | Date    | Data de lavratura do termo de embargo                        |
| DAT_ULT_ALTERACAO         | Date    | Data da última alteração dos atributos                       |
| DAT_ULT_ALTER_GEOM        | Date    | Data da última alteração da geometria                        |
| COD_UF_TAD                | Integer | Código IBGE da UF do local embargado                         |
| SIG_UF_TAD                | String  | Sigla da UF do local embargado                               |
| COD_MUNICIPIO_TAD         | Integer | Código IBGE do município do local embargado                  |
| NOM_MUNICIPIO_TAD         | String  | Nome do município do local embargado                         |
| NUM_LONGITUDE_TAD         | Decimal | Longitude do local embargado (decimal)                       |
| NUM_LATITUDE_TAD          | Decimal | Latitude do local embargado (decimal)                        |
| NUM_LONGITUDE_GMS_TAD     | String  | Longitude em graus, minutos e segundos                       |
| NUM_LATITUDE_GMS_TAD      | String  | Latitude em graus, minutos e segundos                        |
| NOME_IMOVEL               | String  | Denominação do imóvel objeto do embargo                      |
| DES_LOCALIZACAO_TAD       | String  | Descrição do local embargado                                 |
| NOME_PESSOA_EMBARGADA     | String  | Pessoa responsável pelo local embargado                      |
| CPF_CNPJ_EMBARGADO        | String  | CPF (11 dígitos) ou CNPJ (14 dígitos) da pessoa embargada    |
| SIT_DESMATAMENTO          | String  | D=Desmatamento/Queimada, N=Outras situações                  |
| TP_AREA_EMBARGADA         | String  | Tipo de dano da área embargada                               |
| DS_OUTROS_TIPO_AREA       | String  | Descrição complementar quando tipo = "Outros"                |
| ST_AREA_DESMATADA_ILEGAL  | String  | Indicador de uso econômico de área desmatada ilegalmente     |
| QTD_AREA_EMBARGADA        | Decimal | Área total embargada em hectares                             |
| OPERACAO                  | String  | Nome da operação de fiscalização                             |
| UNID_IBAMA_CONTROLE       | String  | Unidade IBAMA responsável                                    |
| ORDEM_FISCALIZACAO        | String  | Identificador da ordem de fiscalização                       |
| ACAO_FISCALIZATORIA       | String  | Número da ação fiscalizatória de origem                      |
| NUM_PROCESSO              | String  | Número do processo administrativo                            |
| DES_TAD                   | String  | Descrição/justificativa do embargo                           |
| WKT_GEOM_AREA_EMBARGADA   | String  | Geometria espacial (WKT - MULTIPOLYGON, POINT, etc.)         |
| NUM_AUTO_INFRACAO         | String  | Número do auto de infração vinculado                         |
| SER_AUTO_INFRACAO         | String  | Série do auto de infração (A, D, E, O ou vazio)              |
| QTD_AREA_DESMATADA        | Decimal | Área total autuada em hectares                               |
| DES_INFRACAO              | String  | Descrição da infração ambiental                              |
| COD_TIPO_BIOMA            | Integer | Código do bioma                                              |
| DES_TIPO_BIOMA            | String  | Descrição do bioma                                           |

### Biomas

| Código | Descrição          |
|--------|--------------------|
| 1      | Mata Atlântica     |
| 3      | Cerrado            |
| 4      | Amazônia           |
| 5      | Caatinga           |
| 6      | Pampa              |
| 7      | Costeiro e Marinho |

### Situação de Desmatamento

| Valor | Descrição                                                                 |
|-------|---------------------------------------------------------------------------|
| D     | Embargo classificado como Desmatamento, Queimada ou ambos                 |
| N     | Embargo não decorre de uso econômico de áreas desmatadas ilegalmente      |

---

## Mapeamento: Histórias x Interações do Contrato Pact

| História | Interação Pact                                    | Provider State                                        |
|----------|---------------------------------------------------|-------------------------------------------------------|
| US-001   | a request to search by CNPJ                       | an entity with CNPJ 29138369000147 exists             |
| US-002   | a request to search by CPF                        | a person with CPF 02330709234 exists                  |
| US-003   | a request to search by name                       | an entity with name containing "RAIMUNDO" exists      |
| US-004   | a request to search by non-existent CNPJ          | no entity with CNPJ 00000000000000 exists             |
| US-004   | a request to search by non-existent name          | no entity with name "Entidade Inexistente XYZ" exists |
| US-005   | a request to search entity with critical risk     | an entity with critical risk level exists             |
| US-009   | a request to get embargo geometry                 | an embargo with WKT geometry exists                   |
| US-010   | a request to search by operation                  | embargos from operation exist                         |

---

## Considerações de Implementação

### Importação do CSV

O arquivo `areas_embargadas.csv` possui as seguintes características:

- **Delimitador:** ponto-e-vírgula (`;`)
- **Encoding:** UTF-8
- **Formato de data:** `dd/MM/yyyy HH:mm:ss`
- **Campos vazios:** Podem ocorrer em qualquer coluna opcional
- **Campos com aspas:** Strings com caracteres especiais são envolvidas em aspas duplas

### Indexação Recomendada

Para otimizar as buscas principais:

```sql
CREATE INDEX idx_cpf_cnpj ON embargos(cpf_cnpj_embargado);
CREATE INDEX idx_nome_pessoa ON embargos(nome_pessoa_embargada);
CREATE INDEX idx_uf ON embargos(sig_uf_tad);
CREATE INDEX idx_bioma ON embargos(cod_tipo_bioma);
CREATE INDEX idx_desmatamento ON embargos(sit_desmatamento);
CREATE INDEX idx_data_embargo ON embargos(dat_embargo);
```

---

## Referências

- **Contrato Pact**: `src/test/resources/pacts/EcoTransparenciaFrontend-EcoTransparenciaBackend.json`
- **Dicionário de Dados IBAMA**: `docs/ibama/dicionario.md`
- **Dataset IBAMA**: `docs/ibama/areas_embargadas.csv` (~125MB, ~milhões de registros)
- **Portal IBAMA**: https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php
- **Especificação Pact**: v4.0
