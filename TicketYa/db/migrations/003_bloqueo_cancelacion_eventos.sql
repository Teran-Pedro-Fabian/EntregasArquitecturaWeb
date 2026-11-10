-- Ejecutar en ticketya_tickets con Ticket detenido.
-- La tabla es una barrera permanente: un evento cancelado no vuelve a vender.
CREATE TABLE IF NOT EXISTS evento_bloqueo_venta (
    id_evento VARCHAR(255) PRIMARY KEY,
    fecha_bloqueo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
