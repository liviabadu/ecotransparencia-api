-- Habilita a extensao PostGIS para suporte a geometria nativa.
-- Necessario antes de qualquer DDL que use tipos `geometry(...)`.
CREATE EXTENSION IF NOT EXISTS postgis;
