package com.microservicioticket.cleaner;

import com.microservicioticket.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketCleaner {
    @Autowired
    private TicketRepository ticketRepo;

    public void clear() {
        ticketRepo.deleteAll();
        System.out.println("Base de datos de Tickets vaciada");
    }
}