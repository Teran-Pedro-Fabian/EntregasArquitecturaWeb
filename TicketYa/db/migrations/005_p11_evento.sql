-- Ejecutar en ticketya_eventos antes de reiniciar Evento
-- Eventos historicos no generan avisos retroactivos
ALTER TABLE evento ADD COLUMN IF NOT EXISTS publicacion_avisada BOOLEAN NOT NULL DEFAULT TRUE;
-- Buzon persistente: un aviso por CADA cambio de evento publicado.
CREATE TABLE IF NOT EXISTS aviso_cambio_evento (
    id VARCHAR(64) PRIMARY KEY,
    id_evento VARCHAR(64) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    entregado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_aviso_cambio_evento_pendiente
    ON aviso_cambio_evento(entregado, fecha_creacion);
