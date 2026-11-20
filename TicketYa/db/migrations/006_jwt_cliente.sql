-- Ejecutar UNA VEZ en ticketya_clientes (PostgreSQL del Cliente)
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);
