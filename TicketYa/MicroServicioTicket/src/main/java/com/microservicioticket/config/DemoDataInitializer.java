package com.microservicioticket.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.sql.Timestamp;

/** Compras y tickets iniciales para que el cliente demo ya tenga QR para mostrar. */
@Component
@ConditionalOnProperty(name = "ticketya.demo.seed", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public DemoDataInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearCompras();
        crearTickets();
    }

    private void crearCompras() {
        compra(8001L, "demo-rata-blanca-001", "cliente-demo", "evento-rata-blanca",
                2001L, null, 1, 45000.0, Instant.now().minus(2, ChronoUnit.DAYS));
        compra(8002L, "demo-santamarina-001", "cliente-demo", "evento-santamarina-mitre",
                2201L, 4001L, 1, 18000.0, Instant.now().minus(1, ChronoUnit.DAYS));
    }

    private void compra(long id, String clave, String idCliente, String idEvento,
                        long idSector, Long idReservaGeneral, int cantidad,
                        double total, Instant fecha) {
        jdbc.update("""
                INSERT INTO compra
                  (id, clave_idempotencia, huella_solicitud, id_cliente, id_evento,
                   id_sector, id_reserva_general, cantidad_entradas, total, medio_pago,
                   estado, fecha_compra, notificacion_enviada)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'TARJETA', 'CONFIRMADA', ?, true)
                ON CONFLICT (id) DO UPDATE SET
                  clave_idempotencia = EXCLUDED.clave_idempotencia,
                  id_cliente = EXCLUDED.id_cliente,
                  id_evento = EXCLUDED.id_evento,
                  id_sector = EXCLUDED.id_sector,
                  id_reserva_general = EXCLUDED.id_reserva_general,
                  cantidad_entradas = EXCLUDED.cantidad_entradas,
                  total = EXCLUDED.total,
                  medio_pago = EXCLUDED.medio_pago,
                  estado = EXCLUDED.estado,
                  fecha_compra = EXCLUDED.fecha_compra
                """, id, clave, "demo-" + id, idCliente, idEvento, idSector,
                idReservaGeneral, cantidad, total, Timestamp.from(fecha));
    }

    private void crearTickets() {
        ticket(9001L, 8001L, "evento-rata-blanca", "cliente-demo", 2001L,
                3014L, null, 45000.0, "TICKETYA-RATA-BLANCA-B4-9001");
        ticket(9002L, 8002L, "evento-santamarina-mitre", "cliente-demo", 2201L,
                null, 4001L, 18000.0, "TICKETYA-SANTAMARINA-MITRE-9002");
    }

    private void ticket(long id, long idCompra, String idEvento, String idCliente,
                        long idSector, Long idAsiento, Long idReservaGeneral,
                        double precio, String qr) {
        jdbc.update("""
                INSERT INTO ticket
                  (id, id_compra, id_evento, id_cliente, id_sector, id_asiento,
                   id_reserva_general, precio, qr_code, usado, estado_devolucion,
                   id_administrador_revision, fecha_revision)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false, 'SIN_SOLICITUD', NULL, NULL)
                ON CONFLICT (id) DO UPDATE SET
                  id_compra = EXCLUDED.id_compra,
                  id_evento = EXCLUDED.id_evento,
                  id_cliente = EXCLUDED.id_cliente,
                  id_sector = EXCLUDED.id_sector,
                  id_asiento = EXCLUDED.id_asiento,
                  id_reserva_general = EXCLUDED.id_reserva_general,
                  precio = EXCLUDED.precio,
                  qr_code = EXCLUDED.qr_code,
                  usado = false,
                  estado_devolucion = 'SIN_SOLICITUD'
                """, id, idCompra, idEvento, idCliente, idSector, idAsiento,
                idReservaGeneral, precio, qr);
    }

}
