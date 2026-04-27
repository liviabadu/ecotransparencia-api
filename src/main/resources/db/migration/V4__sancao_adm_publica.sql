-- Sancoes administrativas: CEIS (Cadastro de Empresas Inidoneas e Suspensas)
-- e CNEP (Cadastro Nacional de Empresas Punidas) na mesma tabela,
-- discriminadas pelo campo `cadastro`.
--
-- CEIS publica 24 colunas; CNEP publica 25 (apenas `valor_multa` adicional).
-- Schema unificado com `valor_multa` nullable.

CREATE TABLE sancao_adm_publica (
    id                              BIGSERIAL PRIMARY KEY,
    cadastro                        VARCHAR(8)   NOT NULL,
    codigo_sancao                   VARCHAR(255),
    tipo_pessoa                     VARCHAR(1),
    cpf_cnpj                        VARCHAR(255),
    nome_sancionado                 VARCHAR(255),
    nome_orgao_sancionador          VARCHAR(255),
    razao_social_receita            VARCHAR(255),
    nome_fantasia_receita           VARCHAR(255),
    numero_processo                 VARCHAR(255),
    categoria_sancao                VARCHAR(255),
    valor_multa                     NUMERIC(18,2),
    data_inicio_sancao              DATE,
    data_fim_sancao                 DATE,
    data_publicacao                 DATE,
    publicacao                      VARCHAR(1000),
    detalhamento_meio_publicacao    VARCHAR(1000),
    data_transito_julgado           DATE,
    abrangencia_sancao              VARCHAR(255),
    orgao_sancionador               VARCHAR(255),
    uf_orgao                        VARCHAR(2),
    esfera_orgao                    VARCHAR(255),
    fundamentacao_legal             TEXT,
    data_origem_informacao          DATE,
    origem_informacoes              VARCHAR(255),
    observacoes                     TEXT
);

-- Indice em cpf_cnpj redundantemente declarado aqui e em V7 (V7 cobre todas
-- as fontes de Fase B). Mantido aqui via @Index na entity para manter a
-- coerencia entre schema test (Hibernate drop-and-create) e schema prod.
CREATE INDEX idx_sancao_adm_publica_cpf_cnpj ON sancao_adm_publica(cpf_cnpj);
