package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Procesar reintegros masivos cuando Evento se cancela
 * Cada ticket se procesa en su propia transaccion para permitir reintentos
 */
@Service
public class CancelacionEventoService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private TicketService ticketService;
    @Autowired private AvisoCancelacionService avisoCancelacionService;
    @Autowired private CompraService compraService;
    @Autowired private RestTemplate restTemplate;

    public Map<String, Object> procesar(String idEvento, String idAdministrador) {
        if (idEvento == null || idEvento.isBlank() ||
                idAdministrador == null || idAdministrador.isBlank()) {
            throw new IllegalArgumentException("Evento o administrador no valido");
        }

        // 1) Validar el estado actual del evento para evitar reintegros arbitrarios
        Map<String, Object> evento = restTemplate.getForObject(
                "http://localhost:8082/evento/" + idEvento, Map.class);
        if (evento == null || !"CANCELADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta cancelado");
        }

        // 2) Registrar los avisos. Cada cliente recibe solo uno por evento.
        List<TicketEntity> tickets = ticketRepository.findByIdEventoOrderByIdAsc(idEvento);
        Set<String> clientes = new HashSet<>();
        List<String> errores = new ArrayList<>();
        for (TicketEntity ticket : tickets) {
            if (ticket.getIdCliente() != null && clientes.add(ticket.getIdCliente())) {
                try {
                    avisoCancelacionService.registrar(idEvento, ticket.getIdCliente());
                } catch (RuntimeException e) {
                    errores.add("Aviso para cliente " + ticket.getIdCliente() + ": " + e.getMessage());
                }
            }
        }

        // 3) Antes de reintegrar, intentar resolver compras con confirmacion incierta.
        // Se reusan la misma compra y tickets; NO se vuelve a cobrar.
        Set<Long> comprasPendientes = new HashSet<>();
        for (TicketEntity ticket : tickets) {
            if (ticket.getIdCompra() == null) continue;
            CompraEntity compra = compraRepository.findById(ticket.getIdCompra()).orElse(null);
            if (compra != null && "PENDIENTE_CONFIRMACION".equals(compra.getEstado())) {
                comprasPendientes.add(compra.getId());
            }
        }
        for (Long idCompra : comprasPendientes) {
            try {
                if (!"CONFIRMADA".equals(compraService.reintentarParaCancelacion(idCompra).getEstado())) {
                    errores.add("Compra " + idCompra + ": sigue pendiente de confirmacion; revisar manualmente");
                }
            } catch (RuntimeException e) {
                errores.add("Compra " + idCompra + ": " + e.getMessage());
            }
        }

        // 4) Procesar cada ticket de manera independiente
        for (TicketEntity ticket : tickets) {
            try {
                if ("DEVUELTA".equals(ticket.getEstadoDevolucion())) continue;

                // No reintegrar compras que aun no estaban confirmadas
                if (ticket.getIdCompra() != null) {
                    CompraEntity compra = compraRepository.findById(ticket.getIdCompra())
                            .orElseThrow(() -> new IllegalStateException("Compra no encontrada"));
                    if (!"CONFIRMADA".equals(compra.getEstado())) {
                        throw new IllegalStateException("La compra " + compra.getId() + " aun no esta confirmada");
                    }
                }

                ticketService.marcarDevolucionPorCancelacion(ticket.getId(), idAdministrador);
                ticketService.procesarReintegro(ticket.getId());
                ticketService.finalizarDevolucion(ticket.getId());
            } catch (RuntimeException e) {
                // El estado pendiente queda guardado y la cancelacion se puede reintentar
                errores.add("Ticket " + ticket.getId() + ": " + e.getMessage());
            }
        }
        // 4) Reflejar la devolucion total en el registro de cada compra
        Set<Long> idsCompras = new HashSet<>();
        for (TicketEntity ticket : tickets) {
            if (ticket.getIdCompra() != null) idsCompras.add(ticket.getIdCompra());
        }
        for (Long idCompra : idsCompras) {
            try {
                CompraEntity compra = compraRepository.findById(idCompra).orElse(null);
                if (compra == null || !"CONFIRMADA".equals(compra.getEstado())) continue;
                List<TicketEntity> ticketsCompra = ticketRepository.findByIdCompra(idCompra);
                if (!ticketsCompra.isEmpty() && ticketsCompra.stream()
                        .allMatch(t -> "DEVUELTA".equals(t.getEstadoDevolucion()))) {
                    compra.setEstado("REINTEGRADA");
                    compraRepository.save(compra);
                }
            } catch (RuntimeException e) {
                errores.add("Compra " + idCompra + ": " + e.getMessage());
            }
        }
        return construirResumen(idEvento, errores);
    }

    /** Consultar el avance sin iniciar otro reintegro. */
    public Map<String, Object> consultar(String idEvento) {
        if (idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Evento no valido");
        }
        return construirResumen(idEvento, List.of());
    }

    private Map<String, Object> construirResumen(String idEvento, List<String> errores) {
        List<TicketEntity> tickets = ticketRepository.findByIdEventoOrderByIdAsc(idEvento);
        int devueltos = 0;
        List<Map<String, Object>> pendientesDetalle = new ArrayList<>();
        for (TicketEntity ticket : tickets) {
            if ("DEVUELTA".equals(ticket.getEstadoDevolucion())) {
                devueltos++;
            } else {
                Map<String, Object> detalle = new LinkedHashMap<>();
                detalle.put("idTicket", ticket.getId());
                detalle.put("estado", ticket.getEstadoDevolucion());
                pendientesDetalle.add(detalle);
            }
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idEvento", idEvento);
        resultado.put("totalTickets", tickets.size());
        resultado.put("devueltos", devueltos);
        resultado.put("pendientes", pendientesDetalle.size());
        resultado.put("detallePendientes", pendientesDetalle);
        resultado.put("avisosRegistrados", avisoCancelacionService.contarPorEvento(idEvento));
        resultado.put("errores", errores);
        return resultado;
    }
}
