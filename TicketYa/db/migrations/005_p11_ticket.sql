-- Ejecutar en ticketya_tickets antes de reiniciar Ticket
-- No vuelve a marcar compras nuevas si se ejecuta dos veces
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'compra'
          AND column_name = 'notificacion_enviada'
    ) THEN
        ALTER TABLE compra ADD COLUMN notificacion_enviada BOOLEAN NOT NULL DEFAULT FALSE;
        -- No mandar avisos retroactivos de compras historicas de prueba.
        UPDATE compra SET notificacion_enviada = TRUE WHERE estado = 'CONFIRMADA';
    END IF;
END $$;
