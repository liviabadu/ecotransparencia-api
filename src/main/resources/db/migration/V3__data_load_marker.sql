-- Marker table para idempotencia das cargas iniciais a partir de CSV/Shapefile.
-- Trocar count() > 0 por findById(source) elimina race-condition em cargas parciais
-- e permite associar versao da fonte (data/hash) caso necessario no futuro.
CREATE TABLE data_load_marker (
    source     VARCHAR(64) PRIMARY KEY,
    version    VARCHAR(32),
    loaded_at  TIMESTAMP NOT NULL
);
