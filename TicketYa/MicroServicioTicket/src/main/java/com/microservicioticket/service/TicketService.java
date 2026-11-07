package com.microservicioticket.service;

import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RestTemplate restTemplate;

    public TicketEntity emitirTicket(TicketEntity ticket) {
        // Generamos un código QR
        String codigoQR = "TICKETYA-" + UUID.randomUUID().toString().toUpperCase();
        ticket.setQrCode(codigoQR);

        return ticketRepository.save(ticket);
    }

    public TicketEntity obtenerTicket(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void devolverTicket(Long idTicket) {

        TicketEntity ticket = ticketRepository.findById(idTicket)//busca ticket
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        //Consultar fecha del evento al MicroServicioEvento
        // URL: http://localhost:8082/eventos/{id}
        String urlEvento = "http://localhost:8082/evento/" + ticket.getIdEvento();
        Map<String, Object> evento = restTemplate.getForObject(urlEvento, Map.class);

        String fechaStr = (String) evento.get("fechaHora");
        LocalDateTime fechaEvento = LocalDateTime.parse(fechaStr);
        LocalDateTime ahora = LocalDateTime.now();

        // Verificar que falten al menos 72 horas
        long horasRestantes = Duration.between(ahora, fechaEvento).toHours();
        if (horasRestantes < 72) {
            throw new RuntimeException("No se puede devolver: faltan menos de 72 horas para el evento");
        }

        //Liberar asiento en MicroServicioLugar
        String urlLugar = "http://localhost:8083/lugares/asientos/liberar/" + ticket.getIdAsiento();
        restTemplate.delete(urlLugar);

        // Eliminar el ticket de la base de datos
        ticketRepository.delete(ticket);
    }
}
