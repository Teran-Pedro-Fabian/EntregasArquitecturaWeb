package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.ReporteEventoDTO;
import com.microservicioticket.dto.ReporteSectorDTO;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReporteService {

    private final TicketRepository ticketRepository;
    private final CompraRepository compraRepository;
    private final RestTemplate restTemplate;
    private final String urlEvento;
    private final String urlLugar;

    public ReporteService(TicketRepository ticketRepository, CompraRepository compraRepository,
                          RestTemplate restTemplate,
                          @Value("${TICKETYA_EVENTO_URL:http://localhost:8082}") String urlEvento,
                          @Value("${TICKETYA_LUGAR_URL:http://localhost:8083}") String urlLugar) {
        this.ticketRepository = ticketRepository;
        this.compraRepository = compraRepository;
        this.restTemplate = restTemplate;
        this.urlEvento = urlEvento;
        this.urlLugar = urlLugar;
    }

    /**
     * Punto 12: ventas, devoluciones, ocupacion y recaudacion de un evento.
     * El organizador se comprueba contra MicroServicioEvento.
     * Cuando este JWT, el ID del organizador debe salir del token y no del parametro.
     */
    @Transactional(readOnly = true)
    public ReporteEventoDTO reportePorEvento(String idEvento, String idOrganizador) {
        if (idEvento == null || idEvento.isBlank()
                || idOrganizador == null || idOrganizador.isBlank()) {
            throw new IllegalArgumentException("Evento u organizador no validos");
        }

        // 1) Verificar que el evento exista y pertenezca al organizador.
        Map<?, ?> evento = restTemplate.getForObject(urlEvento + "/evento/" + idEvento, Map.class);
        if (evento == null || evento.get("organizador") == null) {
            throw new IllegalStateException("No se pudo consultar el organizador del evento");
        }
        if (!idOrganizador.equals(evento.get("organizador"))) {
            throw new SecurityException("El evento pertenece a otro organizador");
        }

        // 2) Consultar los sectores de Lugar. La capacidad se calcula alli.
        ReporteSectorLugar[] sectoresLugar = restTemplate.getForObject(
                urlLugar + "/lugares/interno/reportes/eventos/" + idEvento + "/sectores",
                ReporteSectorLugar[].class);
        if (sectoresLugar == null) {
            throw new IllegalStateException("No se pudo consultar la capacidad de los sectores");
        }

        ReporteEventoDTO reporte = new ReporteEventoDTO();
        reporte.setIdEvento(idEvento);
        reporte.setNombreEvento((String) evento.get("nombre"));
        reporte.setOrganizador(idOrganizador);

        Map<Long, ReporteSectorDTO> porSector = new HashMap<>();
        for (ReporteSectorLugar sectorLugar : sectoresLugar) {
            ReporteSectorDTO sector = new ReporteSectorDTO();
            sector.setIdSector(sectorLugar.getIdSector());
            sector.setNombre(sectorLugar.getNombre());
            sector.setTipo(sectorLugar.getTipo());
            sector.setCapacidad(sectorLugar.getCapacidad());
            reporte.getSectores().add(sector);
            porSector.put(sector.getIdSector(), sector);
        }

        // 3) Buscar los tickets y las compras relacionadas en una sola consulta por lote.
        List<TicketEntity> tickets = ticketRepository.findByIdEventoOrderByIdAsc(idEvento);
        Set<Long> idsCompras = new HashSet<>();
        for (TicketEntity ticket : tickets) {
            if (ticket.getIdCompra() != null) idsCompras.add(ticket.getIdCompra());
        }
        Map<Long, CompraEntity> compras = new HashMap<>();
        if (!idsCompras.isEmpty()) {
            compraRepository.findAllById(idsCompras).forEach(compra ->
                    compras.put(compra.getId(), compra));
        }

        for (TicketEntity ticket : tickets) {
            // No contar tickets de compras que aun estan PENDIENTE_CONFIRMACION.
            if (ticket.getIdCompra() != null) {
                CompraEntity compra = compras.get(ticket.getIdCompra());
                if (compra == null || !"CONFIRMADA".equals(compra.getEstado())) continue;
            }
            // Un ticket sin compra pertenece al circuito historico de emision.
            ReporteSectorDTO sector = porSector.get(ticket.getIdSector());
            if (sector == null) {
                throw new IllegalStateException("Ticket sin sector del evento: " + ticket.getId());
            }
            if (ticket.getPrecio() == null || !Double.isFinite(ticket.getPrecio())
                    || ticket.getPrecio() < 0) {
                throw new IllegalStateException("Ticket con precio no valido: " + ticket.getId());
            }

            BigDecimal precio = BigDecimal.valueOf(ticket.getPrecio());
            sector.setEntradasVendidas(sector.getEntradasVendidas() + 1);
            sector.setRecaudacionBruta(sector.getRecaudacionBruta().add(precio));

            String estado = ticket.getEstadoDevolucion();
            if ("DEVUELTA".equals(estado)) {
                sector.setEntradasDevueltas(sector.getEntradasDevueltas() + 1);
            }
            // El reintegro ya se realizo aunque falte liberar fisicamente el asiento.
            if ("DEVUELTA".equals(estado) || "REINTEGRADA_PENDIENTE_LIBERACION".equals(estado)) {
                sector.setReintegrosRealizados(sector.getReintegrosRealizados().add(precio));
            }
        }

        // 4) Calcular ocupacion y recaudacion por sector y totales del evento.
        for (ReporteSectorDTO sector : reporte.getSectores()) {
            long activas = sector.getEntradasVendidas() - sector.getEntradasDevueltas();
            sector.setEntradasActivas(activas);
            sector.setOcupacionPorcentaje(porcentaje(activas, sector.getCapacidad()));
            sector.setRecaudacionBruta(dinero(sector.getRecaudacionBruta()));
            sector.setReintegrosRealizados(dinero(sector.getReintegrosRealizados()));
            sector.setRecaudacionNeta(dinero(
                    sector.getRecaudacionBruta().subtract(sector.getReintegrosRealizados())));

            reporte.setCapacidadTotal(reporte.getCapacidadTotal() + sector.getCapacidad());
            reporte.setEntradasVendidas(reporte.getEntradasVendidas() + sector.getEntradasVendidas());
            reporte.setEntradasDevueltas(reporte.getEntradasDevueltas() + sector.getEntradasDevueltas());
            reporte.setEntradasActivas(reporte.getEntradasActivas() + activas);
            reporte.setRecaudacionBruta(reporte.getRecaudacionBruta().add(sector.getRecaudacionBruta()));
            reporte.setReintegrosRealizados(reporte.getReintegrosRealizados().add(sector.getReintegrosRealizados()));
        }
        reporte.setOcupacionPorcentaje(porcentaje(reporte.getEntradasActivas(), reporte.getCapacidadTotal()));
        reporte.setRecaudacionBruta(dinero(reporte.getRecaudacionBruta()));
        reporte.setReintegrosRealizados(dinero(reporte.getReintegrosRealizados()));
        reporte.setRecaudacionNeta(dinero(
                reporte.getRecaudacionBruta().subtract(reporte.getReintegrosRealizados())));
        return reporte;
    }

    private BigDecimal porcentaje(long cantidad, long capacidad) {
        if (capacidad <= 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(cantidad).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacidad), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    // DTO local para leer la respuesta JSON de MicroServicioLugar.
    public static class ReporteSectorLugar {
        private Long idSector;
        private String nombre;
        private String tipo;
        private long capacidad;

        public ReporteSectorLugar() { }
        public ReporteSectorLugar(Long idSector, String nombre, String tipo, long capacidad) {
            this.idSector = idSector;
            this.nombre = nombre;
            this.tipo = tipo;
            this.capacidad = capacidad;
        }
        public Long getIdSector() { return idSector; }
        public void setIdSector(Long idSector) { this.idSector = idSector; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public long getCapacidad() { return capacidad; }
        public void setCapacidad(long capacidad) { this.capacidad = capacidad; }
    }
}
