-- Punto 5, PostgreSQL de Ticket (puerto 5433).
-- Puede ejecutarse mas de una vez; no modifica las compras existentes.
ALTER TABLE compra ADD COLUMN IF NOT EXISTS clave_idempotencia VARCHAR(64);
ALTER TABLE compra ADD COLUMN IF NOT EXISTS huella_solicitud VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS uq_compra_clave_idempotencia
    ON compra (clave_idempotencia);
