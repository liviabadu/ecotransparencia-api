# EcoTransparencia API

API REST para consulta de entidades com registros ambientais no IBAMA (Instituto Brasileiro do Meio Ambiente e dos Recursos Naturais Renováveis). Permite verificar se pessoas físicas ou jurídicas possuem áreas embargadas ou autos de infração ambientais, calculando um Score ASG (Ambiental, Social e Governança) para avaliação de risco.

## Sumário

- [Visão de Negócio](#visão-de-negócio)
  - [Contexto](#contexto)
  - [Funcionalidades](#funcionalidades)
  - [Score ASG](#score-asg)
  - [Validação de Situação Cadastral](#validação-de-situação-cadastral)
- [Visão Técnica](#visão-técnica)
  - [Stack Tecnológico](#stack-tecnológico)
  - [Arquitetura](#arquitetura)
  - [Fontes de Dados](#fontes-de-dados)
  - [Endpoints da API](#endpoints-da-api)
  - [Infraestrutura](#infraestrutura)
  - [Testes](#testes)
- [Desenvolvimento](#desenvolvimento)
  - [Pré-requisitos](#pré-requisitos)
  - [Executando localmente](#executando-localmente)
  - [Docker](#docker)
- [Deploy](#deploy)

---

## Visão de Negócio

### Contexto

O **EcoTransparencia** é uma solução voltada para **compliance ambiental** e **análise de risco ASG** (Ambiental, Social e Governança). A API permite que empresas, instituições financeiras e órgãos públicos consultem o histórico ambiental de fornecedores, parceiros comerciais e clientes antes de estabelecer relações comerciais.

O sistema utiliza dados públicos do **IBAMA**, agregando informações de:

- **Áreas Embargadas**: Termos de Embargo lavrados contra pessoas físicas e jurídicas por infrações ambientais como desmatamento ilegal, queimadas e outras atividades degradadoras do meio ambiente.
- **Autos de Infração**: Multas aplicadas pelo IBAMA por descumprimento da legislação ambiental.

### Funcionalidades

#### 1. Consulta por Documento (CPF/CNPJ)

Permite buscar uma entidade pelo seu documento fiscal, retornando:

- Dados da entidade (nome, documento)
- Score ASG calculado
- Nível de risco (Baixo, Médio, Alto, Crítico)
- Lista de embargos ambientais
- Lista de autos de infração
- Situação cadastral na Receita Federal (para CNPJ)

#### 2. Consulta por Nome

Busca parcial por nome da pessoa física ou jurídica, útil quando não se possui o documento completo.

#### 3. Bloqueio por Situação Cadastral

Para consultas de CNPJ, a API valida a situação cadastral na Receita Federal:
- **ATIVA**: Permite a análise ASG completa
- **BAIXADA/INAPTA/SUSPENSA**: Bloqueia a consulta, retornando apenas a situação cadastral

### Score ASG

O Score ASG é um indicador numérico (0-100) que representa o risco ambiental de uma entidade, calculado com base em múltiplos critérios:

#### Critérios de Cálculo - Embargos (Peso: 50%)

| Critério | Pontos |
|----------|--------|
| Por cada embargo ativo | +15 |
| Relacionado a desmatamento (SIT_DESMATAMENTO = 'D') | +10 |
| Em bioma sensível (Amazônia, Mata Atlântica) | +5 |
| Por área embargada (a cada 10 hectares, máx. +10) | +1 a +10 |
| Embargo baixado | 10% do valor normal |

#### Critérios de Cálculo - Autos de Infração (Peso: 35%)

| Critério | Pontos |
|----------|--------|
| Por cada auto de infração (não cancelado) | +8 |
| Conduta intencional | +5 |
| Efeito grave/severo no meio ambiente | +3 |
| Bioma sensível atingido | +5 |
| Valor da multa até R$ 10.000 | +2 |
| Valor da multa R$ 10.001 a R$ 50.000 | +5 |
| Valor da multa R$ 50.001 a R$ 100.000 | +8 |
| Valor da multa acima de R$ 100.000 | +12 |

#### Classificação de Risco

| Score | Nível de Risco | Descrição |
|-------|----------------|-----------|
| 0-25 | Baixo | Entidade com histórico ambiental limpo ou ocorrências antigas/baixadas |
| 26-50 | Médio | Entidade com algumas ocorrências que merecem atenção |
| 51-79 | Alto | Entidade com histórico significativo de infrações ambientais |
| 80-100 | Crítico | Entidade com grave histórico ambiental, requer análise aprofundada |

### Validação de Situação Cadastral

Antes de realizar a análise ASG de um CNPJ, a API consulta a situação cadastral na Receita Federal através da [CNPJA API Open](https://cnpja.com/api/open):

- **Objetivo**: Evitar análises de empresas inativas ou em situação irregular
- **Comportamento**: Se a empresa não estiver com situação "ATIVA", a consulta é bloqueada
- **Transparência**: O motivo do bloqueio é informado na resposta da API

---

## Visão Técnica

### Stack Tecnológico

| Componente | Tecnologia | Versão | Justificativa |
|------------|------------|--------|---------------|
| **Framework** | [Quarkus](https://quarkus.io/) | 3.30.2 | Framework cloud-native Java com startup rápido, baixo consumo de memória e suporte nativo a containers |
| **Linguagem** | Java | 21 LTS | Versão LTS mais recente com recursos modernos (records, pattern matching, virtual threads) |
| **REST** | Quarkus REST (RESTEasy Reactive) | - | Implementação reativa de JAX-RS otimizada para Quarkus |
| **ORM** | Hibernate ORM com Panache | - | Simplifica operações de banco de dados com padrão Active Record |
| **Banco de Dados (dev/prod)** | H2 (in-memory) | - | Banco embarcado para simplificar deploy, dados carregados no startup |
| **Banco de Dados (futuro)** | MySQL | - | Preparado para migração quando necessário |
| **Serialização** | Jackson | - | Serialização JSON padrão do ecossistema Java |
| **OpenAPI/Swagger** | SmallRye OpenAPI | - | Documentação automática da API com Swagger UI |
| **REST Client** | Quarkus REST Client | - | Cliente HTTP declarativo para APIs externas (CNPJA) |
| **CSV Parsing** | OpenCSV | 5.9 | Parsing eficiente de arquivos CSV grandes do IBAMA |
| **Testes de Contrato** | Pact JVM | 4.7.0-beta.1 | Consumer-Driven Contract Testing entre frontend e backend |
| **Testes** | JUnit 5, Mockito, REST Assured | - | Stack padrão de testes para APIs Java |

### Arquitetura

```
src/main/java/br/com/ecotransparencia/
├── client/                    # Clientes REST para APIs externas
│   ├── CnpjaApiClient.java    # Cliente para CNPJA API (Receita Federal)
│   └── CnpjaApiResponse.java  # DTO de resposta da CNPJA
├── domain/                    # Domínio e regras de negócio
│   └── FonteDados.java        # Enum com fontes de dados e pesos do Score ASG
├── dto/                       # Data Transfer Objects
│   ├── AsgScoreDto.java       # Score ASG com breakdown por fonte
│   ├── AutoInfracaoDto.java   # DTO de auto de infração
│   ├── EntityDto.java         # Entidade consultada (pessoa/empresa)
│   ├── LocationDto.java       # Localização geográfica
│   ├── OccurrenceDto.java     # Ocorrência de embargo
│   ├── OcorrenciasAgrupadasDto.java # Agrupamento de ocorrências
│   ├── ScoreComponentDto.java # Componente do score (breakdown)
│   ├── SearchResponse.java    # Resposta da API de busca
│   └── SituacaoCadastralDto.java # Situação na Receita Federal
├── entity/                    # Entidades JPA
│   ├── AutoInfracao.java      # Mapeamento da tabela auto_infracao
│   └── Embargo.java           # Mapeamento da tabela embargo
├── repository/                # Repositórios de dados
│   ├── AutoInfracaoRepository.java
│   └── EmbargoRepository.java
├── resource/                  # Controllers REST (JAX-RS)
│   └── SearchResource.java    # Endpoints de busca
├── service/                   # Serviços de negócio
│   ├── AsgScoreCalculator.java        # Cálculo do Score ASG
│   ├── ReceitaFederalService.java     # Interface de consulta RF
│   ├── ReceitaFederalServiceImpl.java # Implementação real (CNPJA API)
│   ├── ReceitaFederalServiceStub.java # Stub para dev/testes
│   └── SearchService.java             # Orquestração de buscas
├── startup/                   # Inicialização da aplicação
│   ├── IbamaAutoInfracaoDataLoader.java # Carrega autos de infração
│   └── IbamaDataLoader.java           # Carrega embargos
└── util/                      # Utilitários
    └── DocumentoUtil.java     # Validação de CPF/CNPJ
```

### Fontes de Dados

#### 1. Áreas Embargadas IBAMA

- **Origem**: [Portal IBAMA - Áreas Embargadas](https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php)
- **Formato**: CSV delimitado por `;` (~125MB)
- **Registros**: Histórico desde 1987
- **Campos principais**: SEQ_TAD, CPF_CNPJ_EMBARGADO, NOME_PESSOA_EMBARGADA, DAT_EMBARGO, SIT_DESMATAMENTO, QTD_AREA_EMBARGADA, DES_TIPO_BIOMA

#### 2. Autos de Infração IBAMA

- **Origem**: Dados públicos do IBAMA
- **Formato**: CSVs por ano (`auto_infracao_ano_YYYY.csv`)
- **Campos principais**: SEQ_AUTO_INFRACAO, CPF_CNPJ_INFRATOR, NOME_INFRATOR, VAL_AUTO_INFRACAO, GRAVIDADE_INFRACAO, DS_BIOMAS_ATINGIDOS

#### 3. CNPJA API Open

- **Origem**: [CNPJA API](https://cnpja.com/api/open)
- **Função**: Consulta situação cadastral de CNPJ na Receita Federal
- **Uso**: Validação prévia antes da análise ASG

### Endpoints da API

#### Buscar por Documento

```http
GET /api/search/document?document={cpf_ou_cnpj}&type={cpf|cnpj}
```

**Exemplo de resposta (encontrado):**
```json
{
  "found": true,
  "entity": {
    "id": "1872430",
    "name": "EMPRESA EXEMPLO LTDA",
    "document": "12345678000190",
    "documentType": "cnpj",
    "riskLevel": "Alto",
    "score": 65,
    "situacaoCadastral": {
      "situacao": "ATIVA",
      "mensagem": "Cadastro ativo na Receita Federal",
      "valido": true
    },
    "asgScore": {
      "score": 65,
      "riskLevel": "Alto",
      "totalOcorrencias": 5,
      "breakdown": [
        {"fonte": "Embargos IBAMA", "score": 45, "peso": 0.5, "quantidadeOcorrencias": 2},
        {"fonte": "Autos de Infracao IBAMA", "score": 38, "peso": 0.35, "quantidadeOcorrencias": 3}
      ]
    },
    "ocorrencias": {
      "embargos": [...],
      "autosInfracao": [...]
    }
  }
}
```

**Exemplo de resposta (não encontrado):**
```json
{
  "found": false
}
```

**Exemplo de resposta (bloqueado por situação cadastral):**
```json
{
  "found": false,
  "bloqueadoPorSituacaoCadastral": true,
  "situacaoCadastral": {
    "situacao": "BAIXADA",
    "mensagem": "CNPJ com situacao 'BAIXADA' na Receita Federal. Analise ASG nao disponivel para empresas inativas.",
    "valido": false
  }
}
```

#### Buscar por Nome

```http
GET /api/search/name?name={termo_busca}
```

### Infraestrutura

#### Google Cloud Platform

A aplicação é hospedada no **Google Cloud Run**, um serviço serverless para containers:

- **Build**: Cloud Build (CI/CD)
- **Registry**: Google Container Registry
- **Runtime**: Cloud Run (us-central1)
- **Storage**: Cloud Storage (dados CSV)

#### Configurações de Deploy

| Configuração | Valor |
|--------------|-------|
| Memória | 1 GB |
| CPU | 1 vCPU |
| Min Instances | 0 (scale to zero) |
| Max Instances | 10 |
| Timeout | 300s (startup) |

#### Pipeline CI/CD

O arquivo `cloudbuild.yaml` define o pipeline:

1. **Download CSV**: Baixa dados do IBAMA do Cloud Storage
2. **Build**: Constrói imagem Docker multi-stage
3. **Push**: Envia para Container Registry
4. **Deploy**: Publica no Cloud Run

### Testes

#### Testes de Contrato (Pact)

O projeto utiliza **Pact** para garantir compatibilidade entre frontend e backend:

```bash
# Gerar contratos (consumer)
./mvnw -Dtest=*ConsumerPactTest test

# Verificar contratos (provider)
./mvnw -Dtest=ProviderPactVerificationTest test
```

Contratos gerados em: `target/pacts/`

#### Testes Unitários e Integração

```bash
# Executar todos os testes
./mvnw test

# Executar teste específico
./mvnw -Dtest=SearchServiceTest test
```

---

## Desenvolvimento

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional)

### Executando localmente

```bash
# Modo desenvolvimento com hot-reload
./mvnw quarkus:dev
```

A aplicação estará disponível em:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Dev UI**: http://localhost:8080/q/dev

### Docker

```bash
# Build da imagem
docker build -t ecotransparencia-api .

# Executar container
docker run -i --rm -p 8080:8080 ecotransparencia-api
```

---

## Deploy

### Ambiente de Produção

**URL**: https://ecotransparencia-api-860516408210.us-central1.run.app

### Testando a API

```bash
# Consultar CNPJ
curl -s "https://ecotransparencia-api-860516408210.us-central1.run.app/api/search/document?document=00000000000191&type=cnpj" | jq

# Consultar por nome
curl -s "https://ecotransparencia-api-860516408210.us-central1.run.app/api/search/name?name=PREFEITURA" | jq
```

---

## Licença

Apache 2.0

---

## Links Úteis

- [Portal IBAMA - Áreas Embargadas](https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php)
- [CNPJA API Documentation](https://cnpja.com/api/open)
- [Quarkus Documentation](https://quarkus.io/guides/)
- [Pact Documentation](https://docs.pact.io/)
