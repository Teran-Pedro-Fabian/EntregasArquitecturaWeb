-- Ejecutar en ticketya_lugares con Lugar detenido.
-- En caso de duplicados existentes, la migracion NO modifica ni elimina datos:
-- hay que resolver manualmente esos registros antes de continuar.

SELECT id_sector, UPPER(BTRIM(fila)) AS fila_normalizada, columna, COUNT(*) AS cantidad,
       STRING_AGG(id::text, ', ' ORDER BY id) AS ids
FROM asiento
GROUP BY id_sector, UPPER(BTRIM(fila)), columna
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX IF NOT EXISTS ux_asiento_sector_fila_columna
ON asiento (id_sector, UPPER(BTRIM(fila)), columna);
