package com.microserviciolugar.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Datos de lugares, sectores y asientos para la demo. */
@Component
@ConditionalOnProperty(name = "ticketya.demo.seed", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public DemoDataInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearLugares();
        crearSectores();
        crearAsientosRataBlanca();
        crearVentaGeneralDemo();
    }

    private void crearLugares() {
        lugar(1001L, "Estadio Unión y Progreso", "Quintana 450", "Tandil", -37.3217, -59.1332);
        lugar(1002L, "Diagonal Arturo Illia - Parque Independencia", "Diagonal Arturo Illia", "Tandil", -37.3292, -59.1340);
        lugar(1003L, "Estadio Municipal General San Martín", "Av. Rivadavia 350", "Tandil", -37.3190, -59.1265);
        lugar(1004L, "Anfiteatro Martín Fierro", "Juldain 43 - Parque Independencia", "Tandil", -37.3307, -59.1336);
    }

    private void lugar(long id, String nombre, String direccion, String ciudad,
                       double latitud, double longitud) {
        jdbc.update("""
                INSERT INTO lugar (id, nombre, direccion, ciudad, latitud, longitud)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                  nombre = EXCLUDED.nombre,
                  direccion = EXCLUDED.direccion,
                  ciudad = EXCLUDED.ciudad,
                  latitud = EXCLUDED.latitud,
                  longitud = EXCLUDED.longitud
                """, id, nombre, direccion, ciudad, latitud, longitud);
    }

    private void crearSectores() {
        sector(2001L, 1001L, "evento-rata-blanca", "Platea Baja", "NUMERADO", 30, 45000.0, 0, 0);
        sector(2002L, 1001L, "evento-rata-blanca", "Platea Alta", "NUMERADO", 20, 35000.0, 0, 0);
        sector(2003L, 1001L, "evento-rata-blanca", "Preferencial", "NUMERADO", 20, 60000.0, 0, 0);

        sector(2101L, 1002L, "evento-chacinar", "General", "NO_NUMERADO", 500, 10000.0, 0, 0);
        sector(2102L, 1002L, "evento-chacinar", "Preferencial", "NO_NUMERADO", 100, 18000.0, 0, 0);

        sector(2201L, 1003L, "evento-santamarina-mitre", "Popular Local", "NO_NUMERADO", 1000, 18000.0, 0, 1);
        sector(2202L, 1003L, "evento-santamarina-mitre", "Popular Visitante", "NO_NUMERADO", 500, 18000.0, 0, 0);
        sector(2203L, 1003L, "evento-santamarina-mitre", "Platea", "NO_NUMERADO", 300, 30000.0, 0, 0);

        sector(2301L, 1004L, "evento-festival-sierra", "General", "NO_NUMERADO", 800, 12000.0, 0, 0);
        sector(2302L, 1004L, "evento-festival-sierra", "Preferencial", "NO_NUMERADO", 150, 20000.0, 0, 0);
    }

    private void sector(long id, long idLugar, String idEvento, String nombre,
                        String tipo, int cupos, double precio,
                        int reservados, int vendidos) {
        jdbc.update("""
                INSERT INTO sector
                  (id, id_lugar, id_evento, nombre, tipo, cupos, precio, cupos_reservados, cupos_vendidos)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                  id_lugar = EXCLUDED.id_lugar,
                  id_evento = EXCLUDED.id_evento,
                  nombre = EXCLUDED.nombre,
                  tipo = EXCLUDED.tipo,
                  cupos = EXCLUDED.cupos,
                  precio = EXCLUDED.precio,
                  cupos_reservados = EXCLUDED.cupos_reservados,
                  cupos_vendidos = EXCLUDED.cupos_vendidos
                """, id, idLugar, idEvento, nombre, tipo, cupos, precio, reservados, vendidos);
    }

    private void crearAsientosRataBlanca() {
        long id = 3001L;
        for (String fila : new String[]{"A", "B", "C"}) {
            for (int columna = 1; columna <= 10; columna++) {
                long ticket = ("B".equals(fila) && columna == 4) ? 9001L : -1L;
                asiento(id++, 2001L, fila, columna, ticket);
            }
        }
        id = 3101L;
        for (String fila : new String[]{"D", "E"}) {
            for (int columna = 1; columna <= 10; columna++) asiento(id++, 2002L, fila, columna, -1L);
        }
        id = 3201L;
        for (String fila : new String[]{"A", "B"}) {
            for (int columna = 1; columna <= 10; columna++) asiento(id++, 2003L, fila, columna, -1L);
        }
    }

    private void asiento(long id, long idSector, String fila, int columna, long idTicket) {
        boolean vendido = idTicket > 0;
        jdbc.update("""
                INSERT INTO asiento
                  (id, id_sector, fila, columna, estado, cliente_reserva, reserva_hasta, id_ticket_vendido)
                VALUES (?, ?, ?, ?, ?, NULL, NULL, ?)
                ON CONFLICT (id) DO UPDATE SET
                  id_sector = EXCLUDED.id_sector,
                  fila = EXCLUDED.fila,
                  columna = EXCLUDED.columna,
                  estado = EXCLUDED.estado,
                  cliente_reserva = NULL,
                  reserva_hasta = NULL,
                  id_ticket_vendido = EXCLUDED.id_ticket_vendido
                """, id, idSector, fila, columna, vendido ? "VENDIDO" : "DISPONIBLE",
                vendido ? idTicket : null);
    }

    private void crearVentaGeneralDemo() {
        jdbc.update("""
                INSERT INTO reserva_general
                  (id, id_sector, id_cliente, cantidad, estado, reserva_hasta, id_compra)
                VALUES (4001, 2201, 'cliente-demo', 1, 'CONFIRMADA', NULL, 8002)
                ON CONFLICT (id) DO UPDATE SET
                  id_sector = EXCLUDED.id_sector,
                  id_cliente = EXCLUDED.id_cliente,
                  cantidad = EXCLUDED.cantidad,
                  estado = EXCLUDED.estado,
                  reserva_hasta = NULL,
                  id_compra = EXCLUDED.id_compra
                """);

        jdbc.update("""
                INSERT INTO venta_general (id_ticket, id_sector, id_reserva_general, estado)
                VALUES (9002, 2201, 4001, 'VENDIDO')
                ON CONFLICT (id_ticket) DO UPDATE SET
                  id_sector = EXCLUDED.id_sector,
                  id_reserva_general = EXCLUDED.id_reserva_general,
                  estado = EXCLUDED.estado
                """);
    }

}
