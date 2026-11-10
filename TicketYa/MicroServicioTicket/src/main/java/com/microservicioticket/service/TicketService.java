package com.microservicioticket.service;

import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.Repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PagoMockService pagoMockService;


    /**
     * Emitir un ticket con una reserva valida
     * Consultar Lugar antes de guardar
     *
     * @param ticket datos del ticket
     * @return ticket emitido
     */
    @Transactional
    public TicketEntity emitirTicket(TicketEntity ticket) {
        // 1) Verificar los datos obligatorios
        if (ticket.getIdAsiento() == null ||
                ticket.getIdSector() == null ||
                ticket.getIdCliente() == null ||
                ticket.getIdCliente().isBlank() ||
                ticket.getIdEvento() == null ||
                ticket.getIdEvento().isBlank() ||
                ticket.getPrecio() == null ||
                ticket.getPrecio() <= 0) {
            throw new IllegalArgumentException("Datos del ticket incompletos");
        }
        // 2) Preparar los datos de la reserva
        Map<String, Object> datosReserva = Map.of(
                "idAsiento", ticket.getIdAsiento(),
                "idCliente", ticket.getIdCliente(),
                "idSector", ticket.getIdSector(),
                "idEvento", ticket.getIdEvento()
        );
        // 3) Validar la reserva en Lugar
        String url = "http://localhost:8083/lugares/interno/reservas/validar";
        restTemplate.postForEntity(
                url,
                datosReserva,
                String.class
        );
        // Procesar el pago simulado
        boolean pagoAprobado = pagoMockService.procesarPago(ticket.getIdCliente(), ticket.getPrecio());
        // Verificar que el pago fue aprobado
        if (!pagoAprobado) {
            throw new IllegalStateException("El pago fue rechazado");
        }
        // 6) Generar el codigo QR
        String codigoQR = "TICKETYA-" + UUID.randomUUID().toString().toUpperCase();
        // 7) Preparar el ticket
        ticket.setId(null);
        ticket.setQrCode(codigoQR);
        ticket.setEstadoDevolucion("SIN_SOLICITUD");
        ticket.setUsado(false);
        ticket.setIdAdministradorRevision(null);
        ticket.setFechaRevision(null);
        // 8) Guardar el ticket en PostgreSQL
        TicketEntity ticketGuardado = ticketRepository.saveAndFlush(ticket);
        // 9) Preparar los datos para confirmar la venta
        Map<String, Object> datosVenta = Map.of(
                "idAsiento", ticketGuardado.getIdAsiento(),
                "idCliente", ticketGuardado.getIdCliente(),
                "idTicket", ticketGuardado.getId()
        );
        // 10) Confirmar la venta en MicroServicioLugar
        String urlConfirmacion = "http://localhost:8083/lugares/asiento/confirmar";

        restTemplate.postForEntity(
                urlConfirmacion,
                datosVenta,
                String.class
        );
        // 11) Devolver el ticket emitido
        return ticketGuardado;
    }

    public TicketEntity obtenerTicket(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    /**
     * Solicitar la devolucion de una entrada
     * Verificar el cliente y el limite de 72 horas
     *
     * @param idTicket ID del ticket
     * @param idCliente ID del cliente
     * @return ticket con solicitud pendiente
     */
    @Transactional
    public TicketEntity solicitarDevolucion(Long idTicket, String idCliente) {
        // 1) Buscar el ticket
        TicketEntity ticket = ticketRepository.findById(idTicket).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        // 2) Verificar el cliente
        if (idCliente == null || !idCliente.equals(ticket.getIdCliente())) {
            throw new RuntimeException("El ticket no pertenece al cliente");
        }
        // 3) Verificar que no exista otra solicitud
        if (ticket.getEstadoDevolucion() != null && !"SIN_SOLICITUD".equals(ticket.getEstadoDevolucion())) {
            throw new RuntimeException("El ticket ya tiene una solicitud de devolucion");
        }
        // 4) Consultar la fecha del evento
        String urlEvento = "http://localhost:8082/evento/" + ticket.getIdEvento();
        Map<String, Object> evento = restTemplate.getForObject(urlEvento, Map.class);
        if (evento == null || evento.get("fechaHora") == null) {
            throw new RuntimeException("No se pudo consultar la fecha del evento");
        }
        LocalDateTime fechaEvento = LocalDateTime.parse(evento.get("fechaHora").toString());
        // 5) Verificar el limite de 72 horas
        Duration tiempoRestante = Duration.between(LocalDateTime.now(), fechaEvento);
        if (tiempoRestante.compareTo(Duration.ofHours(72)) < 0) {
            throw new RuntimeException("No se puede solicitar la devolucion: faltan menos de 72 horas");
        }
        // 6) Registrar la solicitud
        ticket.setEstadoDevolucion("PENDIENTE");
        // 7) Guardar los cambios
        return ticketRepository.save(ticket);
    }

    /**
     * Revisar una solicitud de devolucion
     * Aprobar o rechazar segun la decision del administrador
     *
     * @param idTicket ID del ticket
     * @param idAdministrador administrador que revisa
     * @param decision APROBAR o RECHAZAR
     * @return ticket actualizado
     */
    @Transactional
    public TicketEntity revisarDevolucion(Long idTicket, String idAdministrador, String decision) {
        // 1) Verificar el administrador
        if (idAdministrador == null || idAdministrador.isBlank()) {
            throw new RuntimeException("Administrador no valido");
        }
        // 2) Verificar la decision
        if (decision == null || (!"APROBAR".equalsIgnoreCase(decision) && !"RECHAZAR".equalsIgnoreCase(decision))) {
            throw new RuntimeException("Decision no valida");
        }
        // 3) Buscar el ticket y bloquearlo
        TicketEntity ticket = ticketRepository.findByIdParaRevision(idTicket).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        // 4) Verificar que exista una solicitud pendiente
        if (!"PENDIENTE".equals(ticket.getEstadoDevolucion())) {
            throw new RuntimeException("El ticket no tiene una solicitud pendiente");
        }
        // 5) Registrar la decision
        if ("APROBAR".equalsIgnoreCase(decision)) {
            ticket.setEstadoDevolucion("APROBADA_PENDIENTE_REINTEGRO");
        } else {
            ticket.setEstadoDevolucion("RECHAZADA");
        }
        // 6) Registrar quien reviso y cuando
        ticket.setIdAdministradorRevision(idAdministrador);
        ticket.setFechaRevision(Instant.now());
        // 7) Guardar los cambios
        return ticketRepository.save(ticket);
    }

    /**
     * Registrar la devolucion obligatoria por evento cancelado
     * No hay limite de 72 horas ni aprobacion individual
     */
    @Transactional
    public TicketEntity marcarDevolucionPorCancelacion(Long idTicket, String idAdministrador) {
        if (idAdministrador == null || idAdministrador.isBlank()) {
            throw new IllegalArgumentException("Administrador no valido");
        }
        TicketEntity ticket = ticketRepository.findByIdParaRevision(idTicket)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        String estado = ticket.getEstadoDevolucion();
        if ("DEVUELTA".equals(estado) || "REINTEGRADA_PENDIENTE_LIBERACION".equals(estado)
                || "CANCELACION_PENDIENTE_REINTEGRO".equals(estado)) {
            return ticket;
        }
        // La cancelacion del evento prevalece sobre una solicitud individual pendiente o rechazada
        ticket.setEstadoDevolucion("CANCELACION_PENDIENTE_REINTEGRO");
        ticket.setIdAdministradorRevision(idAdministrador);
        ticket.setFechaRevision(Instant.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Procesar el reintegro de una solicitud aprobada
     * Simular la respuesta de la pasarela de pagos
     *
     * @param idTicket ID del ticket
     * @return ticket actualizado
     */
    @Transactional
    public TicketEntity procesarReintegro(Long idTicket) {
        // 1) Buscar el ticket y bloquearlo
        TicketEntity ticket = ticketRepository.findByIdParaRevision(idTicket).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        // 2) Verificar si el reintegro ya fue procesado
        if ("REINTEGRADA_PENDIENTE_LIBERACION".equals(ticket.getEstadoDevolucion())) {
            return ticket;
        }
        // 3) Verificar que la devolucion haya sido aprobada
        if (!"APROBADA_PENDIENTE_REINTEGRO".equals(ticket.getEstadoDevolucion()) &&
                !"CANCELACION_PENDIENTE_REINTEGRO".equals(ticket.getEstadoDevolucion())) {
            throw new RuntimeException("La devolucion no esta aprobada");
        }
        // 4) Solicitar el reintegro a la pasarela simulada
        boolean aprobado = pagoMockService.solicitarReintegro(ticket.getId(), ticket.getPrecio());
        // 5) Verificar la respuesta del reintegro
        if (!aprobado) {
            throw new RuntimeException("El reintegro fue rechazado por la pasarela");
        }
        // 6) Registrar el reintegro aprobado
        ticket.setEstadoDevolucion("REINTEGRADA_PENDIENTE_LIBERACION");
        // 7) Guardar el nuevo estado
        return ticketRepository.save(ticket);
    }

    /**
     * Finalizar la devolucion de un ticket
     * Liberar el asiento luego del reintegro
     *
     * @param idTicket ID del ticket
     * @return ticket actualizado
     */
    @Transactional
    public TicketEntity finalizarDevolucion(Long idTicket) {
        // 1) Buscar el ticket y bloquearlo
        TicketEntity ticket = ticketRepository.findByIdParaRevision(idTicket).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        // 2) Verificar si la devolucion ya finalizo
        if ("DEVUELTA".equals(ticket.getEstadoDevolucion())) {
            return ticket;
        }
        // 3) Verificar que el reintegro este procesado
        if (!"REINTEGRADA_PENDIENTE_LIBERACION".equals(ticket.getEstadoDevolucion())) {
            throw new RuntimeException("El ticket no tiene un reintegro procesado");
        }
        // 4) Liberar el asiento numerado o un cupo de Campo General
        String urlLugar;
        if (ticket.getIdAsiento() != null) {
            urlLugar = "http://localhost:8083/lugares/interno/asientos/"
                    + ticket.getIdAsiento() + "/liberar-venta?idTicket=" + ticket.getId();
        } else if (ticket.getIdReservaGeneral() != null && ticket.getIdSector() != null) {
            urlLugar = "http://localhost:8083/lugares/interno/sectores/generales/"
                    + ticket.getIdSector() + "/liberar-venta?idTicket=" + ticket.getId();
        } else {
            throw new RuntimeException("El ticket no tiene un asiento ni una reserva general");
        }
        // 5) Solicitar la liberacion en Lugar
        restTemplate.postForEntity(urlLugar, null, String.class);
        // 7) Registrar la devolucion finalizada
        ticket.setEstadoDevolucion("DEVUELTA");
        // 8) Guardar el ticket en PostgreSQL
        return ticketRepository.save(ticket);
    }
}
