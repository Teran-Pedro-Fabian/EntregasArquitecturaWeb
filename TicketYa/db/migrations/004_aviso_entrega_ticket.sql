-- Punto 7)
-- Los avisos antiguos quedan pendientes se entregan al reintentar
ALTER TABLE aviso_cancelacion
    ADD COLUMN IF NOT EXISTS fecha_entrega TIMESTAMPTZ;
