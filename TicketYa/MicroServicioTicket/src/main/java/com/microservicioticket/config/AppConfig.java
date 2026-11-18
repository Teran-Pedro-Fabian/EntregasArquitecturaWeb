package com.microservicioticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate(@Value("${ticketya.internal.key}") String claveInterna) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("X-TicketYa-Internal", claveInterna);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
