# 🌱 EcoTransparencia API

![Java](https://img.shields.io/badge/Java-21_LTS-007396?logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30.2-4695EB?logo=quarkus&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16_+_PostGIS-336791?logo=postgresql&logoColor=white)
![Cloud Run](https://img.shields.io/badge/Cloud_Run-deployed-4285F4?logo=googlecloud&logoColor=white)
![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)

API REST para análise de risco ASG (Ambiental, Social, Governança) de pessoas físicas e jurídicas a partir de **fontes públicas brasileiras**: IBAMA, ICMBio, CEIS/CNEP/CEPIM (Portal da Transparência) e a Lista Suja do Trabalho Escravo do MTE. Consultando por CPF ou CNPJ, a API agrega ocorrências das 7 fontes, calcula um Score ASG calibrado e devolve um diagnóstico unificado.

> 💡 **Em uma frase:** consulte um CNPJ e receba uma nota de risco ASG de 0 a 100, agregando 8 bases públicas (IBAMA, ICMBio, CEIS, CNEP, CEPIM, MTE) com decay temporal e validação na Receita Federal.

## Sumário

- [Visão de negócio](#visão-de-negócio)
  - [Para que serve](#para-que-serve)
  - [Fontes de dados](#fontes-de-dados)
  - [Score ASG](#score-asg)
- [Visão técnica](#visão-técnica)
  - [Stack](#stack)
  - [Arquitetura](#arquitetura)
  - [Banco de dados e migrações](#banco-de-dados-e-migrações)
  - [Carga inicial e idempotência](#carga-inicial-e-idempotência)
  - [Endpoints](#endpoints)
  - [Testes](#testes)
- [Desenvolvimento local](#desenvolvimento-local)
- [Deploy (Google Cloud)](#deploy-google-cloud)
- [Pendências e próximos passos](#pendências-e-próximos-passos)

---

## Visão de negócio

### Para que serve

Empresas, instituições financeiras e órgãos públicos consultam o histórico ambiental, trabalhista e administrativo de fornecedores, parceiros e clientes antes de fechar negócio. O EcoTransparência consolida 7 bases públicas heterogêneas em uma única consulta por documento (CNPJ), aplicando um score calibrado para classificar o risco.

Casos de uso típicos:

- **Onboarding de fornecedores**: bloquear contratação se houver embargo IBAMA ativo, presença na Lista Suja do MTE, ou inidoneidade no CEIS.
- **Compliance de crédito**: enriquecer a análise de risco com sinais ambientais e de governança que não aparecem em birôs tradicionais.
- **Due diligence M&A**: histórico ambiental + sanções administrativas em um único endpoint.

#### Visão geral do ecossistema

```mermaid
flowchart LR
    subgraph Consumidores["👥 Consumidores"]
        WEB[Frontend Web]
        APIS[APIs de Compliance/ESG]
    end

    subgraph Core["☁️ EcoTransparencia API · Cloud Run"]
        REST["REST · SearchResource"]
        SVC["SearchService<br/>+ AsgScoreCalculator"]
        DB[("PostgreSQL 16<br/>+ PostGIS<br/>(Cloud SQL)")]
    end

    subgraph FontesE["🌳 Fontes Ambientais (E)"]
        IBE[IBAMA Embargos]
        IBA[IBAMA Autos]
        ICE[ICMBio Embargos]
        ICA[ICMBio Autos]
    end

    subgraph FontesS["🤝 Fontes Sociais (S)"]
        MTE[MTE Lista Suja]
    end

    subgraph FontesG["🏛️ Fontes Governança (G)"]
        CNEP[CNEP]
        CEIS[CEIS]
        CEPIM[CEPIM]
    end

    subgraph Externo["🌐 Validação externa"]
        CNPJA[CNPJA API<br/>Receita Federal]
    end

    WEB --> REST
    APIS --> REST
    REST --> SVC
    SVC --> DB
    SVC -->|Validação CNPJ| CNPJA

    IBE -.->|CSV boot| DB
    IBA -.->|CSV boot| DB
    ICE -.->|XLSX + SHP| DB
    ICA -.->|XLSX + SHP| DB
    MTE -.->|CSV boot| DB
    CNEP -.->|CSV boot| DB
    CEIS -.->|CSV boot| DB
    CEPIM -.->|CSV boot| DB

    classDef core fill:#10b981,stroke:#047857,color:#fff
    classDef e fill:#16a34a,stroke:#14532d,color:#fff
    classDef s fill:#0284c7,stroke:#0c4a6e,color:#fff
    classDef g fill:#9333ea,stroke:#581c87,color:#fff
    classDef ext fill:#f59e0b,stroke:#b45309,color:#fff
    classDef cli fill:#3b82f6,stroke:#1d4ed8,color:#fff
    class REST,SVC,DB core
    class IBE,IBA,ICE,ICA e
    class MTE s
    class CNEP,CEIS,CEPIM g
    class CNPJA ext
    class WEB,APIS cli
```

### Fontes de dados

| Fonte | Origem | Bloco ESG | Volume típico | Modo de carga |
|---|---|:---:|---:|---|
| **IBAMA — Embargos** | Portal IBAMA (CSV) | E | ~88k linhas | CSV no boot |
| **IBAMA — Autos de Infração** | Portal IBAMA (CSVs por ano 1977-2025) | E | ~700k linhas | CSV no boot |
| **ICMBio — Autos de Infração** | Dados abertos ICMBio (XLSX + SHP) | E | ~41k linhas | XLSX (POI streaming) + SHP (GeoTools) |
| **ICMBio — Embargos** | Dados abertos ICMBio (XLSX + SHP) | E | ~14k linhas | XLSX + SHP, geometria poligonal |
| **CEIS** — Cadastro de Empresas Inidôneas e Suspensas | Portal da Transparência / CGU | G | ~22k linhas | CSV no boot (ISO-8859-1) |
| **CNEP** — Cadastro Nacional de Empresas Punidas | Portal da Transparência / CGU | G | ~1,6k linhas | CSV no boot (ISO-8859-1) |
| **CEPIM** — Entidades sem fins lucrativos impedidas | Portal da Transparência / CGU | G | ~3,5k linhas | CSV no boot |
| **MTE — Lista Suja do Trabalho Escravo** | Ministério do Trabalho e Emprego | S | ~600 linhas | CSV no boot (Cp1252) |

A geometria do ICMBio (Point para autos, Polygon/MultiPolygon para embargos, EPSG:4674 SIRGAS 2000) é persistida em colunas PostGIS e fica disponível para futuros endpoints de mapa. Não é exposta em DTOs no momento.

### Score ASG

O score é uma **média ponderada** que considera as 8 fontes (IBAMA Embargos, IBAMA Autos, ICMBio Embargos, ICMBio Autos, MTE, CNEP, CEIS, CEPIM). Os pesos somam **1,00** e seguem distribuição **ESG 60/20/20**:

| Fonte | Bloco | Peso |
|---|:---:|---:|
| IBAMA Embargos | E | **0,25** |
| IBAMA Autos | E | 0,18 |
| ICMBio Embargos | E | 0,10 |
| ICMBio Autos | E | 0,07 |
| MTE Trabalho Escravo | S | **0,20** |
| CNEP | G | 0,08 |
| CEIS | G | 0,07 |
| CEPIM | G | 0,05 |
| **Total** | | **1,00** |

#### Composição ESG do Score (60/20/20)

```mermaid
pie showData
    title Distribuição de pesos por bloco ESG
    "E · IBAMA Embargos (0,25)" : 25
    "E · IBAMA Autos (0,18)" : 18
    "E · ICMBio Embargos (0,10)" : 10
    "E · ICMBio Autos (0,07)" : 7
    "S · MTE Trabalho Escravo (0,20)" : 20
    "G · CNEP (0,08)" : 8
    "G · CEIS (0,07)" : 7
    "G · CEPIM (0,05)" : 5
```

#### Pipeline de cálculo do Score ASG

```mermaid
flowchart LR
    DOC([CPF / CNPJ]) --> COL[Coleta paralela<br/>nas 8 fontes]
    COL --> E1[/IBAMA Embargos/]
    COL --> E2[/IBAMA Autos/]
    COL --> E3[/ICMBio Embargos/]
    COL --> E4[/ICMBio Autos/]
    COL --> S1[/MTE/]
    COL --> G1[/CNEP/]
    COL --> G2[/CEIS/]
    COL --> G3[/CEPIM/]

    E1 & E2 & E3 & E4 --> RULES["Regras por fonte<br/>+ decay temporal<br/>+ esfera/trânsito julgado<br/>+ bioma sensível"]
    S1 --> RULES
    G1 & G2 & G3 --> RULES

    RULES --> WEIGHT["Média ponderada<br/>(pesos somam 1,00)"]
    WEIGHT --> FINAL["Score Final 0–100"]
    FINAL --> CLASS{Classificação}

    CLASS -->|0–25| BAIXO[🟢 Baixo]
    CLASS -->|26–50| MEDIO[🟡 Médio]
    CLASS -->|51–79| ALTO[🟠 Alto]
    CLASS -->|80–100| CRITICO[🔴 Crítico]

    classDef src fill:#0ea5e9,stroke:#0369a1,color:#fff
    classDef calc fill:#8b5cf6,stroke:#5b21b6,color:#fff
    class E1,E2,E3,E4,S1,G1,G2,G3 src
    class RULES,WEIGHT,FINAL calc
```

**Critérios calibrados (2026-04):**

- **Categoria de sanção (CEIS/CNEP)**: inidoneidade=25 · impedimento/proibição=12 · suspensão=10 · multa/publicação=8 · demais=6.
- **Esfera do órgão (CEIS/CNEP)**: federal ×1,0 · estadual ×0,7 · municipal ×0,5.
- **Trânsito em julgado (CEIS/CNEP)**: confirmado ×1,0 · em recurso ×0,6.
- **Recência (todas as fontes)**: ≤5 anos ×1,0 · 5-10 anos ×0,7 · 10-20 anos ×0,4 · >20 anos ×0,2 (decay temporal).
- **UC de proteção integral (ICMBio)**: PARNA, REBIO, ESEC, MONA, REVIS recebem +8 pontos sobre uso sustentável (RESEX, FLONA, APA).
- **Bonus**: CNEP soma faixa por valor da multa · MTE soma +5 com decisão administrativa de procedência + 1 por trabalhador envolvido · CEPIM pondera por motivo (TCE > irregularidade > omissão).
- **Embargos IBAMA**: +15 base · +10 desmatamento · +5 bioma sensível (Amazônia, Mata Atlântica) · +1/10ha (max +10) · ×0,1 se baixado.
- **Autos IBAMA**: +8 base · +5 intencional · +3 efeito grave · +5 bioma sensível · +2 a +12 por faixa de multa.

**Classificação de risco** (`src/.../service/AsgScoreCalculator.java#classifyRiskLevel`):

| Score | Nível |
|---:|---|
| 0-25 | Baixo |
| 26-50 | Médio |
| 51-79 | Alto |
| 80-100 | Crítico |

```mermaid
flowchart LR
    B["🟢 BAIXO<br/>0 – 25"] --> M["🟡 MÉDIO<br/>26 – 50"] --> A["🟠 ALTO<br/>51 – 79"] --> C["🔴 CRÍTICO<br/>80 – 100"]

    classDef baixo fill:#22c55e,stroke:#15803d,color:#fff,stroke-width:2px
    classDef medio fill:#eab308,stroke:#a16207,color:#000,stroke-width:2px
    classDef alto fill:#f97316,stroke:#c2410c,color:#fff,stroke-width:2px
    classDef critico fill:#ef4444,stroke:#991b1b,color:#fff,stroke-width:2px
    class B baixo
    class M medio
    class A alto
    class C critico
```

> Os pesos e critérios estão documentados in-line nos `@TODO` do `AsgScoreCalculator` e em `FonteDados.java`. Calibrações futuras (de produto) devem atualizar tanto a fórmula quanto os testes em `AsgScoreCalculatorTest`.

### Validação de situação cadastral (CNPJ)

Antes da análise ASG, todo CNPJ é validado contra a Receita Federal via [CNPJA API Open](https://cnpja.com/api/open):

- **ATIVA** → segue para a análise.
- **BAIXADA / SUSPENSA / INAPTA / INDISPONÍVEL** → bloqueia a análise; resposta contém `bloqueadoPorSituacaoCadastral: true` e a `situacaoCadastral`.

O bean `ReceitaFederalServiceStub` (default em dev/test) sempre retorna ATIVA. O bean real (`ReceitaFederalServiceImpl`) é ativado em build com `-Decotransparencia.receita-federal.use-real-api=true` (ligado em `%prod`).

```mermaid
stateDiagram-v2
    [*] --> ConsultaCNPJ
    ConsultaCNPJ --> ChamaCNPJA: GET /office/{cnpj}
    ChamaCNPJA --> AvaliaSituacao

    AvaliaSituacao --> Ativa: ATIVA
    AvaliaSituacao --> Bloqueada: BAIXADA · SUSPENSA<br/>INAPTA · INDISPONÍVEL

    Ativa --> AnaliseASG: prossegue
    AnaliseASG --> RespondeScore: score + breakdown
    RespondeScore --> [*]

    Bloqueada --> RespondeBloqueio: bloqueadoPorSituacaoCadastral=true
    RespondeBloqueio --> [*]
```

---

## Visão técnica

### Stack

| Componente | Tecnologia | Versão |
|---|---|---|
| Framework | [Quarkus](https://quarkus.io/) | 3.30.2 |
| Linguagem | Java | 21 LTS |
| Banco (dev/staging/prod) | **PostgreSQL 16 + PostGIS** | — |
| Banco (testes unitários) | H2 + H2GIS | — |
| Migrações de schema | Flyway | (BOM Quarkus) |
| ORM | Hibernate ORM Panache + Hibernate Spatial | 7.1.10.Final |
| Geometria | JTS (`org.locationtech.jts`) | 1.20.0 |
| CSV parsing | OpenCSV | 5.9 |
| XLSX streaming | Apache POI (XSSFReader SAX) | 5.4.0 |
| Shapefile | GeoTools (gt-shapefile, gt-referencing, gt-epsg-hsql) | 33.0 |
| Spatial em testes | H2GIS (orbisgis) | 2.2.3 |
| OpenAPI / Swagger | SmallRye OpenAPI | (BOM) |
| REST Client (CNPJA) | Quarkus REST Client | (BOM) |
| Testes | JUnit 5, Mockito, REST Assured, Pact (consumer + provider) | — |

### Arquitetura

#### Camadas e dependências

```mermaid
flowchart TB
    subgraph L1["🎯 Apresentação · resource/"]
        REST[SearchResource]
    end

    subgraph L2["⚙️ Serviços · service/"]
        SS[SearchService<br/>orquestra 7 fontes]
        ASG[AsgScoreCalculator<br/>score calibrado]
        RF[ReceitaFederalService<br/>Impl + Stub]
    end

    subgraph L3["📦 Domínio · domain/ + dto/"]
        FONTE[FonteDados · CadastroSancao]
        DTOS["EntityDto · SearchResponse<br/>AsgScoreDto · ScoreComponentDto<br/>5 *Occurrence DTOs"]
    end

    subgraph L4["🗄️ Persistência · repository/ + entity/"]
        REPOS["8 Panache Repositories"]
        ENT["8 Entities (PostGIS-aware)<br/>+ DataLoadMarker"]
        DB[("PostgreSQL 16 + PostGIS<br/>Flyway V1–V10")]
    end

    subgraph L5["🌐 Integração · client/"]
        CLIENT[CnpjaApiClient]
    end

    subgraph L6["🚀 Bootstrap · startup/"]
        LOADERS["5 Loaders<br/>IBAMA · ICMBio · Sanção · CEPIM · MTE"]
    end

    subgraph L7["🛠️ Util · util/"]
        UTILS["DocumentoUtil · CsvParserBuilder<br/>XlsxStreamReader · ShapefileGeometryReader<br/>LatestCsvByPattern"]
    end

    REST --> SS
    SS --> ASG
    SS --> RF
    SS --> REPOS
    RF --> CLIENT
    CLIENT -.->|HTTPS| EXT[(CNPJA API)]
    REPOS --> ENT
    ENT --> DB
    LOADERS ==>|carga + marker| DB
    LOADERS --> UTILS
    ASG --> FONTE
    SS --> DTOS

    classDef present fill:#3b82f6,stroke:#1d4ed8,color:#fff
    classDef svc fill:#8b5cf6,stroke:#6d28d9,color:#fff
    classDef dom fill:#06b6d4,stroke:#0e7490,color:#fff
    classDef pers fill:#10b981,stroke:#047857,color:#fff
    classDef integ fill:#f59e0b,stroke:#b45309,color:#fff
    classDef boot fill:#ef4444,stroke:#991b1b,color:#fff
    classDef util fill:#64748b,stroke:#1e293b,color:#fff
    class REST present
    class SS,ASG,RF svc
    class FONTE,DTOS dom
    class REPOS,ENT,DB pers
    class CLIENT,EXT integ
    class LOADERS boot
    class UTILS util
```

#### Estrutura de diretórios

```
src/main/java/br/com/ecotransparencia/
├── client/                        # CnpjaApiClient + DTOs externos
├── domain/                        # CadastroSancao, FonteDados (enum + pesos)
├── dto/                           # Camada de saída
│   ├── EntityDto.java             # Entidade unificada
│   ├── SearchResponse.java        # Resposta com 5 listas (IBAMA + Fase B + Fase C)
│   ├── AsgScoreDto.java + ScoreComponentDto.java
│   └── *Occurrence.java           # SancaoAdmPublica, Cepim, TrabalhoEscravo, IcmbioAuto, IcmbioEmbargo
├── entity/                        # JPA entities (PostGIS-aware)
│   ├── Embargo.java               # IBAMA
│   ├── AutoInfracao.java          # IBAMA
│   ├── SancaoAdmPublica.java      # CEIS+CNEP unificados (discriminator)
│   ├── Cepim.java                 # CEPIM
│   ├── TrabalhoEscravoMte.java    # MTE
│   ├── IcmbioAutoInfracao.java    # ICMBio + Point geometry
│   ├── IcmbioEmbargo.java         # ICMBio + Geometry (poligonos)
│   └── DataLoadMarker.java        # idempotencia de carga
├── repository/                    # Panache repositories (1 por entidade)
├── resource/SearchResource.java   # endpoints REST
├── service/
│   ├── SearchService.java         # orquestra busca em 7 fontes + Receita Federal
│   ├── AsgScoreCalculator.java    # score calibrado, breakdown por fonte
│   └── ReceitaFederalService(Impl|Stub).java
├── startup/                       # @Observes StartupEvent loaders
│   ├── IbamaDataLoader.java       # embargos IBAMA
│   ├── IbamaAutoInfracaoDataLoader.java
│   ├── SancaoAdmPublicaLoader.java # CEIS+CNEP
│   ├── CepimLoader.java
│   ├── TrabalhoEscravoMteLoader.java
│   └── IcmbioLoader.java          # XLSX (POI) + SHP (GeoTools), join por vw_num_*
└── util/
    ├── DocumentoUtil.java         # CPF/CNPJ
    ├── CsvParserBuilder.java      # OpenCSV factory por charset
    ├── LatestCsvByPattern.java    # glob de arquivos com prefixo de data
    ├── XlsxStreamReader.java      # POI XSSFReader + SAX (40k+ linhas sem OOM)
    └── ShapefileGeometryReader.java # GeoTools, retorna Map<id, Geometry>
```

### Banco de dados e migrações

Schema gerenciado **exclusivamente por Flyway** — Hibernate `database.generation=none`. Migrações em `src/main/resources/db/migration/`:

| Versão | Conteúdo |
|---|---|
| `V1__enable_postgis.sql` | `CREATE EXTENSION IF NOT EXISTS postgis;` |
| `V2__embargo_and_auto_infracao.sql` | DDL das tabelas IBAMA |
| `V3__data_load_marker.sql` | tabela de marker de idempotência |
| `V4__sancao_adm_publica.sql` | CEIS+CNEP unificado |
| `V5__cepim.sql` | CEPIM |
| `V6__trabalho_escravo_mte.sql` | MTE |
| `V7__indices_cpf_cnpj.sql` | índices em todas as tabelas para perf de busca |
| `V8__icmbio_auto_infracao.sql` | tabela + `geometry(Point, 4674)` + GIST index |
| `V9__icmbio_embargo.sql` | tabela + `geometry(Geometry, 4674)` + GIST index |
| `V10__icmbio_numeric_unbounded.sql` | ALTER em colunas numéricas (XLSX tem valores extremos) |

**Em testes** (`%test`), Flyway é desabilitado e Hibernate gera o schema via `drop-and-create`. H2GIS é carregado via `INIT=...CALL H2GIS_SPATIAL()` na URL JDBC para suportar tipos `geometry`.

#### Modelo de dados

```mermaid
erDiagram
    ENTIDADE ||--o{ EMBARGO : "IBAMA"
    ENTIDADE ||--o{ AUTO_INFRACAO : "IBAMA"
    ENTIDADE ||--o{ ICMBIO_EMBARGO : "ICMBio"
    ENTIDADE ||--o{ ICMBIO_AUTO_INFRACAO : "ICMBio"
    ENTIDADE ||--o{ SANCAO_ADM_PUBLICA : "CEIS+CNEP"
    ENTIDADE ||--o{ CEPIM : "CEPIM"
    ENTIDADE ||--o{ TRABALHO_ESCRAVO_MTE : "MTE"

    DATA_LOAD_MARKER {
        string fonte PK "ibama_embargo, icmbio_auto_v1, ..."
        timestamp loaded_at
        long records_loaded
    }

    ENTIDADE {
        string documento PK "CPF/CNPJ"
        string nome
        string tipo "PF/PJ"
    }
    EMBARGO {
        long seq_tad PK
        date dat_embargo
        double qtd_area_embargada
        string sit_desmatamento
        string des_tipo_bioma
        string situacao "ATIVO/BAIXADO"
    }
    AUTO_INFRACAO {
        long seq_auto_infracao PK
        decimal val_auto_infracao
        string gravidade_infracao
        boolean conduta_dolosa
    }
    ICMBIO_EMBARGO {
        string num_emb PK
        geometry geometria "Polygon EPSG:4674"
        string uc_nome
        string uc_categoria
    }
    ICMBIO_AUTO_INFRACAO {
        string num_auto PK
        geometry geometria "Point EPSG:4674"
        string uc_categoria
        decimal valor_multa
    }
    SANCAO_ADM_PUBLICA {
        long id PK
        string tipo_cadastro "CEIS/CNEP discriminator"
        string categoria_sancao
        string esfera "federal/estadual/municipal"
        boolean transito_julgado
        date data_inicio
    }
    CEPIM {
        long id PK
        string motivo
        date data_inicio
    }
    TRABALHO_ESCRAVO_MTE {
        long id PK
        string decisao_administrativa
        int trabalhadores_envolvidos
        date data_publicacao
    }
```

### Carga inicial e idempotência

A primeira inicialização carrega os ~852k registros das 7 fontes em **~4 minutos**. A tabela `data_load_marker` registra cada fonte carregada (ex.: `ibama_embargo`, `icmbio_auto_v1`, `sancao_adm_publica_v1`); boots subsequentes saltam fontes já marcadas (~30s).

Cada loader:

1. Verifica marker → skip se já carregado.
2. Faz **dedupe em memória** por chave primária natural (CSV/XLSX podem ter duplicatas — IBAMA tem ao menos 1 `seq_tad` repetido).
3. Persiste em batches de 500-1000 registros.
4. Insere o marker no commit final.

Os arquivos de origem ficam em `docs/<fonte>/` em desenvolvimento; em produção, `cloudbuild.yaml` baixa do bucket GCS para `/deployments/data/<fonte>/` antes do `COPY` no Dockerfile.

**ICMBio** usa estratégia híbrida: atributos vêm do XLSX via Apache POI streaming (40k+ linhas sem carregar em memória); geometria vem do `.shp` via GeoTools, joined por `vw_num_auto`/`vw_num_emb`. Linhas sem geometria correspondente são persistidas com `geometria=null` e contadas no log.

```mermaid
flowchart TD
    BOOT([StartupEvent]) --> CHECK{marker existe<br/>em data_load_marker?}
    CHECK -->|Sim| SKIP[⏭️ Skip ~30s]
    CHECK -->|Não| READ[📥 Lê fonte<br/>CSV / XLSX+SHP]
    READ --> DEDUPE[🧹 Dedupe em memória<br/>por chave natural]
    DEDUPE --> BATCH[💾 Persist em batches<br/>500–1000 reg]
    BATCH --> MARK[✅ Insere marker<br/>no commit final]
    MARK --> READY([App pronta])
    SKIP --> READY

    classDef ok fill:#22c55e,stroke:#15803d,color:#fff
    classDef step fill:#3b82f6,stroke:#1d4ed8,color:#fff
    class READ,DEDUPE,BATCH,MARK step
    class READY,SKIP ok
```

### Endpoints

#### `GET /api/search/document?document={doc}&type={cpf|cnpj}`

Busca agregada nas 7 fontes. Para CNPJ, valida situação cadastral antes.

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant API as SearchResource
    participant SS as SearchService
    participant RF as ReceitaFederalService
    participant CNPJA as CNPJA API
    participant REPOS as 8× Repositories
    participant CALC as AsgScoreCalculator

    C->>API: GET /api/search/document?document=X&type=cnpj
    API->>SS: searchByDocument(doc, tipo)

    alt tipo == cnpj
        SS->>RF: consultarSituacao(cnpj)
        RF->>CNPJA: GET /office/{cnpj}
        CNPJA-->>RF: { status: "ATIVA" }
        RF-->>SS: SituacaoCadastralDto
        opt situação ≠ ATIVA
            SS-->>API: { bloqueadoPorSituacaoCadastral: true }
            API-->>C: 200 OK (bloqueado)
        end
    end

    par Coleta paralela em todas as fontes
        SS->>REPOS: findByDocumento(IBAMA Embargos)
        SS->>REPOS: findByDocumento(IBAMA Autos)
        SS->>REPOS: findByDocumento(ICMBio Embargos + Autos)
        SS->>REPOS: findByDocumento(CEIS + CNEP)
        SS->>REPOS: findByDocumento(CEPIM)
        SS->>REPOS: findByDocumento(MTE)
    end
    REPOS-->>SS: Listas de ocorrências

    SS->>CALC: calculate(ocorrências das 8 fontes)
    CALC-->>SS: AsgScoreDto (score + breakdown por fonte)
    SS-->>API: SearchResponse (entity + 5 listas)
    API-->>C: 200 OK
```

**Resposta com ocorrências:**
```json
{
  "found": true,
  "entity": {
    "id": "1829644",
    "name": "DELZI MACHADO ALVES",
    "document": "75776849000150",
    "documentType": "cnpj",
    "score": 1,
    "riskLevel": "Baixo",
    "asgScore": {
      "score": 1,
      "riskLevel": "Baixo",
      "totalOcorrencias": 1,
      "breakdown": [
        {"fonte": "Embargos IBAMA",            "score": 3, "peso": 0.25, "quantidadeOcorrencias": 1},
        {"fonte": "Autos de Infracao IBAMA",   "score": 0, "peso": 0.18, "quantidadeOcorrencias": 0},
        {"fonte": "Embargos ICMBio",           "score": 0, "peso": 0.10, "quantidadeOcorrencias": 0},
        {"fonte": "Autos de Infracao ICMBio",  "score": 0, "peso": 0.07, "quantidadeOcorrencias": 0},
        {"fonte": "MTE - Lista Suja Trabalho Escravo", "score": 0, "peso": 0.20, "quantidadeOcorrencias": 0},
        {"fonte": "CNEP - Empresas Punidas",   "score": 0, "peso": 0.08, "quantidadeOcorrencias": 0},
        {"fonte": "CEIS - Empresas Inidoneas/Suspensas", "score": 0, "peso": 0.07, "quantidadeOcorrencias": 0},
        {"fonte": "CEPIM - Entidades Impedidas", "score": 0, "peso": 0.05, "quantidadeOcorrencias": 0}
      ]
    },
    "ocorrencias": { "embargos": [...], "autosInfracao": [...] },
    "situacaoCadastral": { "situacao": "ATIVA", "valido": true }
  },
  "sancoesAdmPublica": [],
  "impedimentosCepim": [],
  "trabalhoEscravo": [],
  "icmbioAutos": [],
  "icmbioEmbargos": []
}
```

**Bloqueado por situação cadastral:**
```json
{
  "found": false,
  "bloqueadoPorSituacaoCadastral": true,
  "situacaoCadastral": { "situacao": "BAIXADA", "valido": false }
}
```

**Sem ocorrências:**
```json
{ "found": false }
```

#### `GET /api/search/name?name={termo}`

Busca parcial por nome (case-insensitive `LIKE`). Atualmente busca apenas em IBAMA (Phase B/C apenas via documento).

#### `GET /q/swagger-ui`

Swagger UI completo, habilitado em todos os perfis.

#### `GET /q/dev`

Quarkus Dev UI (perfil `dev` apenas).

### Testes

171 testes unitários e de serviço (JUnit 5 + Mockito + Quarkus Test). Pact consumer/provider para contratos.

```bash
./mvnw test                                    # todos os testes
./mvnw -Dtest=AsgScoreCalculatorTest test      # classe específica
./mvnw -Dtest='*ConsumerPactTest' test         # gera pacts em target/pacts/
./mvnw -Dtest=ProviderPactVerificationTest test
```

Os pacts gerados em `target/pacts/` devem ser copiados para `src/test/resources/pacts/` para que o provider verifique-os no CI.

---

## Desenvolvimento local

### Pré-requisitos

- **Java 21+**
- **Maven 3.9+** (ou use o `./mvnw` wrapper)
- **Docker** rodando — necessário para o **DevServices Quarkus** subir Postgres+PostGIS automaticamente em modo dev. Sem Docker, `quarkus:dev` falha por não conseguir conectar ao banco.
- **Arquivos de dados** em `docs/`:
  - `docs/ibama/areas_embargadas.csv` (versionado)
  - `docs/ibama/autos/auto_infracao_ano_*.csv` (versionado, 1977-2025)
  - `docs/adm_publica/*.csv`, `docs/icmbio/*.{xlsx,shp,dbf,prj}`, `docs/mte/tr_escravo.csv` — **não versionados** (download manual ou via cloudbuild)

### Subindo

```bash
./mvnw quarkus:dev
```

O DevServices puxa a imagem `postgis/postgis:16-3.4` (~600 MB no primeiro pull) e sobe um container Postgres dedicado. Flyway aplica V1-V10 e os 5 loaders carregam tudo em ~4 minutos no primeiro boot. Boots subsequentes (com o container preservado e marker presente) sobem em ~30s.

URLs:
- App: http://localhost:8080
- Swagger: http://localhost:8080/q/swagger-ui
- Dev UI: http://localhost:8080/q/dev

### Testes

```bash
./mvnw test
```

Roda em H2 + H2GIS, sem Docker. As flags `app.data.load-*-on-startup` ficam `false` em `%test`, então a suite não tenta carregar CSVs.

### Smoke local

```bash
# DELZI MACHADO ALVES (1 embargo IBAMA de 1987 — score baixo por decay temporal)
curl -s 'http://localhost:8080/api/search/document?document=75776849000150&type=cnpj' | jq

# CNPJ multi-source ICMBio
curl -s 'http://localhost:8080/api/search/document?document=33050071000158&type=cnpj' | jq

# Busca por nome
curl -s 'http://localhost:8080/api/search/name?name=DELZI' | jq
```

### Properties relevantes (`application.properties`)

```properties
# Datasource (default = postgresql; %test = h2; %prod = env vars sem default)
quarkus.datasource.db-kind=postgresql
quarkus.flyway.migrate-at-start=true
quarkus.datasource.devservices.image-name=postgis/postgis:16-3.4

# %prod fail-fast: sem DB_URL/DB_USER/DB_PASSWORD a app não sobe
%prod.quarkus.datasource.jdbc.url=${DB_URL}

# Toggles de carga (todas false em default; ligadas em %dev e %prod)
app.data.load-on-startup=false
app.data.load-autos-on-startup=true
app.data.load-sancao-adm-publica-on-startup=false
app.data.load-cepim-on-startup=false
app.data.load-trabalho-escravo-on-startup=false
app.data.load-icmbio-on-startup=false
```

---

## Deploy (Google Cloud)

A aplicação roda em **Cloud Run** (us-central1) com **Cloud SQL para PostgreSQL 16 + PostGIS** como banco gerenciado.

### Provisionamento (one-time, humano-gated)

```bash
# Cloud SQL Postgres 16 + PostGIS
gcloud sql instances create ecotransparencia-db \
  --database-version=POSTGRES_16 \
  --tier=db-custom-2-7680 \
  --region=southamerica-east1 \
  --database-flags=cloudsql.enable_pg_extensions=postgis

gcloud sql databases create ecotransparencia --instance=ecotransparencia-db
# Conectar e rodar: CREATE EXTENSION IF NOT EXISTS postgis;
```

### Pipeline `cloudbuild.yaml`

1. `gsutil cp gs://ecotransparencia2/...` → baixa CSVs/XLSX/SHP para `docs/`.
2. `docker build` (multi-stage) com `COPY docs/* /deployments/data/`.
3. `docker push` para Artifact Registry.
4. `gcloud run deploy` com:
   - `--add-cloudsql-instances=PROJECT:REGION:INSTANCE`
   - `DB_URL`, `DB_USER`, `DB_PASSWORD` via Secret Manager
   - `--memory=2Gi --timeout=900` no primeiro deploy (carga inicial)

```mermaid
flowchart LR
    DEV([👨‍💻 Dev]) -->|git push| GIT[(GitHub)]
    GIT -->|trigger| CB{Cloud Build}

    CB --> S1["📥 gsutil cp<br/>GCS bucket → docs/"]
    S1 --> S2["🔨 docker build<br/>multi-stage"]
    S2 --> S3["📦 docker push<br/>Artifact Registry"]
    S3 --> S4["🚀 gcloud run deploy<br/>--add-cloudsql-instances"]

    S4 --> RUN[(☁️ Cloud Run<br/>us-central1)]
    RUN <-->|JDBC| SQL[(☁️ Cloud SQL<br/>Postgres 16 + PostGIS)]
    SECRET[🔐 Secret Manager<br/>DB_URL · DB_USER · DB_PASSWORD] --> RUN

    classDef step fill:#4285F4,stroke:#1a56db,color:#fff
    classDef infra fill:#34A853,stroke:#1e7e34,color:#fff
    classDef sec fill:#fbbc04,stroke:#b45309,color:#000
    class S1,S2,S3,S4 step
    class GIT,CB,RUN,SQL infra
    class SECRET sec
```

### Configurações de runtime

| Setting | Valor |
|---|---|
| Memória | 2 GB (1º deploy) → 1 GB (steady state) |
| CPU | 1 vCPU |
| Min/Max instances | 0 / 10 |
| Cloud Run timeout | 900s (1º deploy) → 300s |
| `quarkus.startup-timeout` | 900s |

URL atual: https://ecotransparencia-api-860516408210.us-central1.run.app

---

## Pendências e próximos passos

- **Calibração de pesos e thresholds** (decisão de produto): atualmente o threshold "Baixo" vai até score 25 — pode ficar permissivo demais com a soma dos pesos = 1,00.
- **Endpoints de mapa** com geometria do ICMBio (`GET /api/icmbio/embargos?bbox=...`).
- **Refresh agendado** das fontes do Portal da Transparência (hoje é boot-only; refresh = redeploy).
- **`ProviderPactVerificationTest`** ainda em H2 — quando contratos passarem a usar geometria ou Phase B/C, migrar para Testcontainers Postgres.
- **Native image (`-Pnative`)**: GeoTools + POI usam reflection pesada; geração nativa quebra até produzir `reflect-config.json`. Cloud Run JVM continua funcional.

---

## Links úteis

- [Portal IBAMA — Áreas Embargadas](https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php)
- [Portal da Transparência — CEIS / CNEP / CEPIM](https://portaldatransparencia.gov.br/sancoes)
- [MTE — Lista Suja](https://www.gov.br/trabalho-e-emprego/pt-br/assuntos/inspecao-do-trabalho/areas-de-atuacao/cadastro_de_empregadores)
- [ICMBio — Dados abertos](https://dadosabertos.icmbio.gov.br/)
- [CNPJA API](https://cnpja.com/api/open)
- [Quarkus](https://quarkus.io/guides/) · [Pact](https://docs.pact.io/) · [PostGIS](https://postgis.net/) · [GeoTools](https://geotools.org/)

---

## Licença

Apache 2.0
