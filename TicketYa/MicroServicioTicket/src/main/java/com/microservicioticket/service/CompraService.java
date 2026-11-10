package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PagoMockService pagoMockService;

    @Value("${ticketya.compra.max-entradas:4}")
    private int maxEntradas;

    /**
     * Comprar uno o varios asientos numerados
     * Consultar precio y reservas antes de solicitar el pago
     */
    @Transactional
    public CompraResponse comprar(CompraRequest request) {
        // 1) Verificar los datos de la compra
        validarDatos(request);

        List<Long> ids = request.getIdsAsientos();
        String idCliente = request.getIdCliente();
        String idEvento = request.getIdEvento();
        Long idSector = request.getIdSector();

        // 2) Comprobar que el evento esta publicado
        String urlEvento = "http://localhost:8082/evento/" + idEvento;
        Map<String, Object> evento = restTemplate.getForObject(urlEvento, Map.class);
        if (evento == null || !"PUBLICADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta publicado");
        }

        // 3) Verificar que el cliente no supere el limite
        long activos = ticketRepository.contarActivos(idCliente, idEvento);
        if (activos + ids.size() > maxEntradas) {
            throw new IllegalStateException("Limite de entradas por cliente y evento superado");
        }

        // 4) Consultar el precio oficial de Lugar
        String urlPrecio = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8083/lugares/interno/sectores/" + idSector + "/precio")
                .queryParam("idEvento", idEvento).toUriString();
        Map<String, Object> datosPrecio = restTemplate.getForObject(urlPrecio, Map.class);
        if (datosPrecio == null || !(datosPrecio.get("precio") instanceof Number)) {
            throw new IllegalStateException("No se pudo consultar el precio del sector");
        }
        Double precioUnitario = ((Number) datosPrecio.get("precio")).doubleValue();
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalStateException("Precio del sector no valido");
        }

        // 5) Validar que el cliente tenga reservados todos los asientos
        for (Long idAsiento : ids) {
            Map<String, Object> datosReserva = Map.of(
                    "idAsiento", idAsiento,
                    "idCliente", idCliente,
                    "idSector", idSector,
                    "idEvento", idEvento);
            restTemplate.postForEntity(
                    "http://localhost:8083/lugares/interno/reservas/validar",
                    datosReserva, String.class);
        }

        // 6) Calcular el total desde el precio oficial
        Double total = Math.round(precioUnitario * ids.size() * 100.0) / 100.0;

        // 7) Simular el pago de la compra
        boolean aprobado = pagoMockService.procesarPago(idCliente, total);
        if (!aprobado) {
            // Rechazar el pago y liberar las reservas del cliente
            cancelarReservas(ids, idCliente);
            throw new IllegalStateException("El pago fue rechazado");
        }

        // 8) Registrar la compra y el medio de pago
        CompraEntity compra = new CompraEntity();
        compra.setIdCliente(idCliente);
        compra.setIdEvento(idEvento);
        compra.setIdSector(idSector);
        compra.setCantidadEntradas(ids.size());
        compra.setTotal(total);
        compra.setMedioPago(request.getMedioPago().toUpperCase());
        compra.setEstado("PENDIENTE_CONFIRMACION");
        compra.setFechaCompra(Instant.now());
        compra = compraRepository.saveAndFlush(compra);

        // 9) Generar un ticket con QR unico por cada asiento
        List<TicketEntity> tickets = new ArrayList<>();
        for (Long idAsiento : ids) {
            TicketEntity ticket = new TicketEntity();
            ticket.setIdCompra(compra.getId());
            ticket.setIdCliente(idCliente);
            ticket.setIdEvento(idEvento);
            ticket.setIdSector(idSector);
            ticket.setIdAsiento(idAsiento);
            ticket.setPrecio(precioUnitario);
            ticket.setQrCode("TICKETYA-" + UUID.randomUUID().toString().toUpperCase());
            ticket.setEstadoDevolucion("SIN_SOLICITUD");
            ticket.setUsado(false);
            tickets.add(ticketRepository.saveAndFlush(ticket));
        }

        // 10) Confirmar cada venta en Lugar
        List<TicketEntity> confirmados = new ArrayList<>();
        try {
            for (TicketEntity ticket : tickets) {
                Map<String, Object> datosVenta = Map.of(
                        "idAsiento", ticket.getIdAsiento(),
                        "idCliente", idCliente,
                        "idTicket", ticket.getId());
                restTemplate.postForEntity(
                        "http://localhost:8083/lugares/asiento/confirmar",
                        datosVenta, String.class);
                confirmados.add(ticket);
            }
        } catch (RuntimeException error) {
            // Intentar deshacer confirmaciones parciales
            for (TicketEntity ticket : confirmados) {
                try {
                    String urlLiberar = UriComponentsBuilder.fromHttpUrl(
                            "http://localhost:8083/lugares/interno/asientos/"
                                    + ticket.getIdAsiento() + "/liberar-venta")
                            .queryParam("idTicket", ticket.getId()).toUriString();
                    restTemplate.postForEntity(urlLiberar, null, String.class);
                } catch (RuntimeException compensacion) {
                    // Si falla esta compensacion requiere recuperacion manual
                    System.err.println("No se pudo liberar el asiento " + ticket.getIdAsiento()
                            + " del ticket " + ticket.getId() + ": " + compensacion.getMessage());
                }
            }
            cancelarReservas(ids, idCliente);
            throw new IllegalStateException("No se pudo confirmar la compra en Lugar", error);
        }

        // 11) Marcar la compra como confirmada
        compra.setEstado("CONFIRMADA");
        compraRepository.save(compra);
        return new CompraResponse(compra.getId(), compra.getEstado(),
                compra.getMedioPago(), compra.getTotal(), tickets);
    }

    /**
     * Comprar entradas de un sector NO_NUMERADO
     * El stock se confirma en Lugar con bloqueo del sector
     */
    @Transactional
    public CompraResponse comprarGeneral(CompraGeneralRequest request) {
        validarDatosGeneral(request);

        String idCliente = request.getIdCliente();
        String idEvento = request.getIdEvento();
        Long idSector = request.getIdSector();
        Long idReserva = request.getIdReserva();
        int cantidad = request.getCantidad();

        // 1) Comprobar que el evento esta publicado
        Map<String, Object> evento = restTemplate.getForObject(
                "http://localhost:8082/evento/" + idEvento, Map.class);
        if (evento == null || !"PUBLICADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta publicado");
        }

        // 2) Verificar el limite de entradas del cliente
        long activos = ticketRepository.contarActivos(idCliente, idEvento);
        if (activos + cantidad > maxEntradas) {
            throw new IllegalStateException("Limite de entradas por cliente y evento superado");
        }

        // 3) Consultar precio desde Lugar, nunca desde el cliente
        String urlPrecio = UriComponentsBuilder.fromHttpUrl(
                "http://localhost:8083/lugares/interno/sectores/" + idSector + "/precio")
                .queryParam("idEvento", idEvento).toUriString();
        Map<String, Object> datosPrecio = restTemplate.getForObject(urlPrecio, Map.class);
        if (datosPrecio == null || !(datosPrecio.get("precio") instanceof Number)) {
            throw new IllegalStateException("No se pudo consultar el precio general");
        }
        double precioUnitario = ((Number) datosPrecio.get("precio")).doubleValue();
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalStateException("Precio del sector no valido");
        }

        // 4) Comprobar la reserva general de este cliente
        Map<String, Object> datosReserva = Map.of(
                "idSector", idSector, "idReserva", idReserva,
                "idCliente", idCliente, "idEvento", idEvento, "cantidad", cantidad);
        restTemplate.postForEntity(
                "http://localhost:8083/lugares/interno/sectores/generales/reservas/validar",
                datosReserva, String.class);

        double total = Math.round(precioUnitario * cantidad * 100.0) / 100.0;

        // 5) Simular el pago y cancelar la reserva si fue rechazado
        boolean aprobado = pagoMockService.procesarPago(idCliente, total);
        if (!aprobado) {
            cancelarReservaGeneral(idReserva, idSector, idCliente);
            throw new IllegalStateException("El pago fue rechazado");
        }

        // 6) Guardar la compra y un QR por cada entrada general
        CompraEntity compra = new CompraEntity();
        compra.setIdCliente(idCliente);
        compra.setIdEvento(idEvento);
        compra.setIdSector(idSector);
        compra.setIdReservaGeneral(idReserva);
        compra.setCantidadEntradas(cantidad);
        compra.setTotal(total);
        compra.setMedioPago(request.getMedioPago().toUpperCase());
        compra.setEstado("PENDIENTE_CONFIRMACION");
        compra.setFechaCompra(Instant.now());
        compra = compraRepository.saveAndFlush(compra);

        List<TicketEntity> tickets = new ArrayList<>();
        List<Long> idsTickets = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            TicketEntity ticket = new TicketEntity();
            ticket.setIdCompra(compra.getId());
            ticket.setIdCliente(idCliente);
            ticket.setIdEvento(idEvento);
            ticket.setIdSector(idSector);
            ticket.setIdAsiento(null);
            ticket.setIdReservaGeneral(idReserva);
            ticket.setPrecio(precioUnitario);
            ticket.setQrCode("TICKETYA-" + UUID.randomUUID().toString().toUpperCase());
            ticket.setEstadoDevolucion("SIN_SOLICITUD");
            ticket.setUsado(false);
            ticket = ticketRepository.saveAndFlush(ticket);
            tickets.add(ticket);
            idsTickets.add(ticket.getId());
        }

        // 7) Confirmar todos los cupos en una operacion de Lugar
        try {
            restTemplate.postForEntity(
                    "http://localhost:8083/lugares/interno/sectores/generales/confirmar",
                    Map.of("idSector", idSector, "idReserva", idReserva,
                            "idCliente", idCliente, "idCompra", compra.getId(),
                            "idsTickets", idsTickets), String.class);
        } catch (RuntimeException e) {
            // Si Lugar no confirmo, se intenta liberar la reserva.
            // Si confirmo pero la respuesta se perdio, hace falta recuperacion (punto 5).
            cancelarReservaGeneral(idReserva, idSector, idCliente);
            throw new IllegalStateException("No se pudo confirmar la compra general", e);
        }

        compra.setEstado("CONFIRMADA");
        compraRepository.save(compra);
        return new CompraResponse(compra.getId(), compra.getEstado(),
                compra.getMedioPago(), compra.getTotal(), tickets);
    }

    private void validarDatosGeneral(CompraGeneralRequest request) {
        if (request == null || request.getIdCliente() == null ||
                request.getIdCliente().isBlank() || request.getIdEvento() == null ||
                request.getIdEvento().isBlank() || request.getIdSector() == null ||
                request.getIdReserva() == null || request.getCantidad() == null ||
                request.getCantidad() <= 0 || request.getCantidad() > maxEntradas ||
                request.getMedioPago() == null || request.getMedioPago().isBlank()) {
            throw new IllegalArgumentException("Datos de la compra general no validos");
        }
        String medio = request.getMedioPago().toUpperCase();
        if (!"TARJETA".equals(medio) && !"TRANSFERENCIA".equals(medio)
                && !"BILLETERA_VIRTUAL".equals(medio)) {
            throw new IllegalArgumentException("Medio de pago no valido");
        }
    }

    private void cancelarReservaGeneral(Long idReserva, Long idSector, String idCliente) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(
                    "http://localhost:8083/lugares/sectores/generales/reservas/" + idReserva)
                    .queryParam("idSector", idSector)
                    .queryParam("idCliente", idCliente).toUriString();
            restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (RuntimeException e) {
            System.err.println("No se pudo cancelar la reserva general " + idReserva
                    + ": " + e.getMessage());
        }
    }

    /**
     * Consultar el detalle de una compra ya registrada
     */
    @Transactional(readOnly = true)
    public CompraResponse obtenerCompra(Long idCompra) {
        CompraEntity compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        List<TicketEntity> tickets = ticketRepository.findByIdCompra(idCompra);
        return new CompraResponse(compra.getId(), compra.getEstado(),
                compra.getMedioPago(), compra.getTotal(), tickets);
    }

    /**
     * Verificar los datos recibidos
     * El usuario no puede enviar precio ni tickets creados
     */
    private void validarDatos(CompraRequest request) {
        if (request == null || request.getIdCliente() == null ||
                request.getIdCliente().isBlank() || request.getIdEvento() == null ||
                request.getIdEvento().isBlank() || request.getIdSector() == null ||
                request.getIdsAsientos() == null || request.getIdsAsientos().isEmpty() ||
                request.getMedioPago() == null || request.getMedioPago().isBlank()) {
            throw new IllegalArgumentException("Datos de la compra incompletos");
        }
        List<Long> ids = request.getIdsAsientos();
        if (maxEntradas < 1 || ids.size() > maxEntradas) {
            throw new IllegalArgumentException("Cantidad de entradas no valida");
        }
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.size() != ids.size() || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Asientos repetidos o invalidos");
        }
        String medio = request.getMedioPago().toUpperCase();
        if (!"TARJETA".equals(medio) && !"TRANSFERENCIA".equals(medio)
                && !"BILLETERA_VIRTUAL".equals(medio)) {
            throw new IllegalArgumentException("Medio de pago no valido");
        }
    }

    /**
     * Cancelar reservas luego de un pago rechazado
     * Se verifica el propietario en Lugar
     */
    private void cancelarReservas(List<Long> ids, String idCliente) {
        for (Long idAsiento : ids) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl(
                        "http://localhost:8083/lugares/reservas/" + idAsiento)
                        .queryParam("idCliente", idCliente).toUriString();
                restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            } catch (RuntimeException e) {
                // La limpieza de Lugar libera las reservas vencidas
                System.err.println("No se pudo cancelar la reserva " + idAsiento
                        + ": " + e.getMessage());
            }
        }
    }
}
