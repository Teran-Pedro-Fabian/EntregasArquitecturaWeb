CREATE UNIQUE INDEX IF NOT EXISTS uq_ticket_asiento_activo
ON ticket (id_asiento)
WHERE id_asiento IS NOT NULL
AND estado_devolucion IS DISTINCT FROM 'DEVUELTA';
