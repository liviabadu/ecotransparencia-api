-- ICMBio Autos de Infracao.
-- Atributos vem do XLSX (40.934 linhas, 26 colunas) via Apache POI streaming.
-- Geometria (Point, EPSG:4674 SIRGAS 2000) vem do shapefile via GeoTools.
-- Join entre XLSX e SHP por vw_num_auto (PK natural em ambos formatos).

CREATE TABLE icmbio_auto_infracao (
    vw_num_auto                     INTEGER PRIMARY KEY,
    numero_ai                       VARCHAR(255),
    serie                           VARCHAR(50),
    origem                          VARCHAR(50),
    tipo                            VARCHAR(255),
    valor_multa                     NUMERIC(18,2),
    embargo                         VARCHAR(50),
    apreensao                       VARCHAR(50),
    autuado                         VARCHAR(500),
    cpf_cnpj                        VARCHAR(20),
    cpf_cnpj_formatado              VARCHAR(30),
    desc_ai                         TEXT,
    desc_sanc                       TEXT,
    data                            DATE,
    ano                             INTEGER,
    artigo_1                        VARCHAR(100),
    artigo_2                        VARCHAR(100),
    tipo_infra                      VARCHAR(255),
    nome_uc                         VARCHAR(255),
    cnuc                            VARCHAR(50),
    municipio                       VARCHAR(255),
    uf                              VARCHAR(2),
    termos_emb                      VARCHAR(255),
    termos_apr                      VARCHAR(255),
    ordem_fisc                      VARCHAR(255),
    processo                        VARCHAR(255),
    julgamento                      VARCHAR(255),
    localizacao                     geometry(Point, 4674)
);

CREATE INDEX idx_icmbio_auto_cpf_cnpj ON icmbio_auto_infracao(cpf_cnpj);
CREATE INDEX idx_icmbio_auto_localizacao ON icmbio_auto_infracao USING GIST (localizacao);
