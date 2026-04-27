-- ICMBio Autos de Infracao.
-- Atributos vem do XLSX (40.934 linhas, 26 colunas) via Apache POI streaming.
-- Geometria (Point, EPSG:4674 SIRGAS 2000) vem do shapefile via GeoTools.
-- Join entre XLSX e SHP por vw_num_auto (PK natural em ambos formatos).

CREATE TABLE icmbio_auto_infracao (
    vw_num_auto                     INTEGER PRIMARY KEY,
    numero_ai                       TEXT,
    serie                           VARCHAR(50),
    origem                          VARCHAR(50),
    tipo                            TEXT,
    valor_multa                     NUMERIC(18,2),
    embargo                         VARCHAR(50),
    apreensao                       VARCHAR(50),
    autuado                         TEXT,
    cpf_cnpj                        VARCHAR(20),
    cpf_cnpj_formatado              VARCHAR(30),
    desc_ai                         TEXT,
    desc_sanc                       TEXT,
    data                            DATE,
    ano                             INTEGER,
    artigo_1                        VARCHAR(100),
    artigo_2                        VARCHAR(100),
    tipo_infra                      TEXT,
    nome_uc                         TEXT,
    cnuc                            VARCHAR(50),
    municipio                       TEXT,
    uf                              VARCHAR(8),
    termos_emb                      TEXT,
    termos_apr                      TEXT,
    ordem_fisc                      TEXT,
    processo                        TEXT,
    julgamento                      TEXT,
    localizacao                     geometry(Point, 4674)
);

CREATE INDEX idx_icmbio_auto_cpf_cnpj ON icmbio_auto_infracao(cpf_cnpj);
CREATE INDEX idx_icmbio_auto_localizacao ON icmbio_auto_infracao USING GIST (localizacao);
