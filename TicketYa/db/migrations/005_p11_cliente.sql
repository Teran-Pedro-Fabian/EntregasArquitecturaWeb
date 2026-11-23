-- Ejecutar UNA vez en ticketya_clientes antes de reiniciar MicroServicioCliente
-- Conservar notificaciones anteriores y ampliar tipos de avisos
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS email VARCHAR(254);
ALTER TABLE notificacion_cliente ADD COLUMN IF NOT EXISTS tipo VARCHAR(32) NOT NULL DEFAULT 'CANCELACION';
ALTER TABLE notificacion_cliente ADD COLUMN IF NOT EXISTS referencia VARCHAR(128) NOT NULL DEFAULT 'evento';
ALTER TABLE notificacion_cliente ADD COLUMN IF NOT EXISTS estado_email VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE notificacion_cliente ADD COLUMN IF NOT EXISTS intentos_email INTEGER NOT NULL DEFAULT 0;
ALTER TABLE notificacion_cliente ADD COLUMN IF NOT EXISTS ultimo_error VARCHAR(500);
ALTER TABLE notificacion_cliente DROP CONSTRAINT IF EXISTS uq_notificacion_evento_cliente;
CREATE UNIQUE INDEX IF NOT EXISTS uq_notificacion_tipo_ref_evento_cliente
    ON notificacion_cliente(tipo, referencia, id_evento, id_cliente);
CREATE INDEX IF NOT EXISTS ix_notificacion_email_pendiente
    ON notificacion_cliente(estado_email, fecha_creacion);
