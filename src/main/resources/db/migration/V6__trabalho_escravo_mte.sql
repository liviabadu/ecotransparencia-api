-- MTE Lista Suja: Cadastro de Empregadores que tenham submetido
-- trabalhadores a condicoes analogas as de escravo.
-- O CSV usa CPF/CNPJ formatado (XX.XXX.XXX/XXXX-XX); guardamos tanto a
-- forma normalizada (digit-only, indexada) quanto a original.

CREATE TABLE trabalho_escravo_mte (
    id                              BIGSERIAL PRIMARY KEY,
    id_origem                       INTEGER,
    ano_acao_fiscal                 INTEGER,
    uf                              VARCHAR(2),
    empregador                      VARCHAR(255),
    cpf_cnpj                        VARCHAR(255),
    cpf_cnpj_formatado              VARCHAR(255),
    estabelecimento                 VARCHAR(1000),
    trabalhadores_envolvidos        INTEGER,
    cnae                            VARCHAR(255),
    decisao_adm_procedencia         DATE,
    inclusao_cadastro_empregadores  DATE
);

CREATE INDEX idx_trabalho_escravo_mte_cpf_cnpj ON trabalho_escravo_mte(cpf_cnpj);
