-- ICMBio Embargos.
-- Atributos vem do XLSX (14.363 linhas, 22 colunas) via Apache POI streaming.
-- Geometria (Polygon/MultiPolygon, EPSG:4674) vem do shapefile via GeoTools.
-- Join entre XLSX e SHP por vw_num_emb (PK natural em ambos formatos).

CREATE TABLE icmbio_embargo (
    vw_num_emb                      INTEGER PRIMARY KEY,
    numero_emb                      TEXT,
    serie                           VARCHAR(50),
    origem                          VARCHAR(50),
    numero_ai                       TEXT,
    cpf_cnpj                        VARCHAR(20),
    cpf_cnpj_formatado              VARCHAR(30),
    autuado                         TEXT,
    desc_infra                      TEXT,
    desc_sanc                       TEXT,
    artigo_1                        VARCHAR(100),
    artigo_2                        VARCHAR(100),
    tipo_infra                      TEXT,
    nome_uc                         TEXT,
    cnuc                            VARCHAR(50),
    municipio                       TEXT,
    uf                              VARCHAR(8),
    data                            DATE,
    ano                             INTEGER,
    obs                             TEXT,
    julgamento                      TEXT,
    area                            NUMERIC(18,4),
    processo                        TEXT,
    geometria                       geometry(Geometry, 4674)
);

CREATE INDEX idx_icmbio_embargo_cpf_cnpj ON icmbio_embargo(cpf_cnpj);
CREATE INDEX idx_icmbio_embargo_geometria ON icmbio_embargo USING GIST (geometria);
