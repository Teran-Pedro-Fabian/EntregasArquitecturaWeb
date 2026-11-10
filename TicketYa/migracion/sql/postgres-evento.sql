BEGIN;
SET client_encoding = 'UTF8';
CREATE TABLE IF NOT EXISTS evento (
    id VARCHAR(64) PRIMARY KEY,
    nombre VARCHAR(255),
    descripcion TEXT,
    tipo VARCHAR(255),
    organizador VARCHAR(255),
    fecha_hora VARCHAR(40),
    id_lugar BIGINT,
    estado VARCHAR(255),
    demanda VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS participantes (
    id VARCHAR(64) PRIMARY KEY,
    nombre VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS participante_evento (
    id VARCHAR(64) PRIMARY KEY,
    id_evento VARCHAR(255),
    id_participante VARCHAR(255),
    UNIQUE (id_evento, id_participante)
);

DO $check$
BEGIN
    IF EXISTS (SELECT 1 FROM evento) OR
       EXISTS (SELECT 1 FROM participantes) OR
       EXISTS (SELECT 1 FROM participante_evento) THEN
        RAISE EXCEPTION 'Base Evento no vacia: no se permite sobreescribir datos';
    END IF;
END
$check$;