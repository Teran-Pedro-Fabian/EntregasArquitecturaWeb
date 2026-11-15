package com.microServicioEvento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EventoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventoServiceApplication.class, args);

        System.out.println("Microservicio de Eventos iniciado en el puerto 8082");
    }
}