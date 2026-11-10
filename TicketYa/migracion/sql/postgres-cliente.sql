BEGIN;
SET client_encoding = 'UTF8';
CREATE TABLE IF NOT EXISTS cliente (
    id VARCHAR(64) PRIMARY KEY,
    nombre VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS preferencias (
    id VARCHAR(64) PRIMARY KEY,
    id_cliente VARCHAR(255),
    id_evento VARCHAR(255),
    id_participante VARCHAR(255),
    id_lugar VARCHAR(255)
);
DO $check$
BEGIN
    IF EXISTS (SELECT 1 FROM cliente) OR
       EXISTS (SELECT 1 FROM preferencias) THEN
        RAISE EXCEPTION 'Base Cliente no vacia: no se permite sobreescribir datos';
    END IF;
END
$check$;