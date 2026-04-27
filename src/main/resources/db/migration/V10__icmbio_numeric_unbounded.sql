-- Algumas linhas dos XLSX do ICMBio tem valores numericos extremos:
-- - icmbio_embargo.area: pode estar em m^2 em alguns registros (>= 10^14),
--   estourando NUMERIC(18,4).
-- - icmbio_auto_infracao.valor_multa: idem para multas muito altas.
-- Mudamos para NUMERIC sem precision/scale (Postgres aceita: precisao
-- arbitraria de ate 131072 digitos, scale de ate 16383 digitos).

ALTER TABLE icmbio_embargo ALTER COLUMN area TYPE NUMERIC;
ALTER TABLE icmbio_auto_infracao ALTER COLUMN valor_multa TYPE NUMERIC;
