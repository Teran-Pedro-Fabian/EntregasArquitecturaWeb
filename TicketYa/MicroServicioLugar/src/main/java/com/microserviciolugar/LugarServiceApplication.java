package com.microserviciolugar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LugarServiceApplication {

    public static void main(String[] args) {
        // Arrancar el microservicio de Lugares
        SpringApplication.run(LugarServiceApplication.class, args);
        System.out.println("Microservicio de Lugares corriendo en el puerto 8083...");
    }
}