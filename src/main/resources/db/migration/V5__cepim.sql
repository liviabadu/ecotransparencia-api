-- CEPIM (Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas).
-- Schema simples (5 colunas), sem PK natural -> BIGSERIAL.

CREATE TABLE cepim (
    id                  BIGSERIAL PRIMARY KEY,
    cnpj_entidade       VARCHAR(255),
    nome_entidade       VARCHAR(255),
    numero_convenio     VARCHAR(255),
    orgao_concedente    VARCHAR(255),
    motivo_impedimento  TEXT
);

CREATE INDEX idx_cepim_cnpj_entidade ON cepim(cnpj_entidade);
