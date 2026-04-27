-- Indices em cpf_cnpj para suportar a perf do SearchService que faz
-- equal-match em todas as fontes por documento.
--
-- Os indices das fontes de Fase B (sancao_adm_publica, cepim, trabalho_escravo_mte)
-- ja foram criados nas suas migracoes (V4, V5, V6) via @Index. Esta migracao:
--  1. cobre as fontes de Fase A (embargo, auto_infracao), que ainda nao tinham indice
--  2. usa IF NOT EXISTS para nao falhar caso o indice ja exista
--
-- Postgres 9.5+ suporta CREATE INDEX IF NOT EXISTS.

CREATE INDEX IF NOT EXISTS idx_embargo_cpf_cnpj
    ON embargo(cpf_cnpj_embargado);

CREATE INDEX IF NOT EXISTS idx_auto_infracao_cpf_cnpj
    ON auto_infracao(cpf_cnpj_infrator);

-- Idempotencia para indices das tabelas de Fase B (caso a migracao seja
-- re-aplicada ou ordem de execucao seja alterada).
CREATE INDEX IF NOT EXISTS idx_sancao_adm_publica_cpf_cnpj
    ON sancao_adm_publica(cpf_cnpj);

CREATE INDEX IF NOT EXISTS idx_cepim_cnpj_entidade
    ON cepim(cnpj_entidade);

CREATE INDEX IF NOT EXISTS idx_trabalho_escravo_mte_cpf_cnpj
    ON trabalho_escravo_mte(cpf_cnpj);
