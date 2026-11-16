package com.microservicioticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketServiceApplication {

    public static void main(String[] args) {
        // Arranca el microservicio de tickets
        SpringApplication.run(TicketServiceApplication.class, args);
        System.out.println("🎫 Microservicio de Tickets corriendo en el puerto 8084...");
    }
}