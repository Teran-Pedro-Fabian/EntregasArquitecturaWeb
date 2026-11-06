package com.microservicioticket.service;

import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    public TicketEntity emitirTicket(TicketEntity ticket) {
        // Generamos un código QR
        String codigoQR = "TICKETYA-" + UUID.randomUUID().toString().toUpperCase();
        ticket.setQrCode(codigoQR);

        return ticketRepository.save(ticket);
    }

    public TicketEntity obtenerTicket(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }
}
