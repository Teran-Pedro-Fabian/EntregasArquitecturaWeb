package com.microservicioticket.config;

import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TicketDataInitializer implements CommandLineRunner {
    @Autowired
    private TicketRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            TicketEntity t = new TicketEntity();
            repository.save(t);
            System.out.println("✅ Ticket de prueba creado");
        }
    }
}

