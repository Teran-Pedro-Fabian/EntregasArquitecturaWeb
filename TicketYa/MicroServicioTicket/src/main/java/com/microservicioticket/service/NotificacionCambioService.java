package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Informar cambios de un evento a los compradores confirmados */
@Service
public class NotificacionCambioService {
    @Autowired private TicketRepository ticketRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private RestTemplate restTemplate;
    @Value("${ticketya.cliente.url:http://localhost:8081}") private String clienteUrl;

    public int notificarCambio(String idEvento, String referencia, String mensaje) {
        if (idEvento == null || idEvento.isBlank() || referencia == null || referencia.isBlank() ||
                mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("Datos del cambio invalidos");
        }
        Set<String> clientes = new HashSet<>();
        for (TicketEntity ticket : ticketRepository.findByIdEventoOrderByIdAsc(idEvento)) {
            // Solo compras realmente confirmadas -> excluir las que se reintegraron
            if (ticket.getIdCompra() != null) {
                CompraEntity compra = compraRepository.findById(ticket.getIdCompra()).orElse(null);
                if (compra == null || !"CONFIRMADA".equals(compra.getEstado())) continue;
            }
            if (ticket.getIdCliente() != null && !"DEVUELTA".equals(ticket.getEstadoDevolucion())) {
                clientes.add(ticket.getIdCliente());
            }
        }
        for (String idCliente : clientes) {
            restTemplate.postForEntity(clienteUrl + "/clientes/interno/notificaciones/cambio",
                    Map.of("idEvento", idEvento, "idCliente", idCliente,
                           "referencia", referencia, "mensaje", mensaje), Void.class);
        }
        return clientes.size();
    }
}
