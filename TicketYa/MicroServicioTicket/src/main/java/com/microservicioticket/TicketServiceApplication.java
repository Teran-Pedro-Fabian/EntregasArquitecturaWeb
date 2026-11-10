package com.microservicioticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TicketServiceApplication {

    public static void main(String[] args) {
        // Arranca el microservicio de tickets
        SpringApplication.run(TicketServiceApplication.class, args);
        System.out.println("🎫 Microservicio de Tickets corriendo en el puerto 8084...");
    }
}