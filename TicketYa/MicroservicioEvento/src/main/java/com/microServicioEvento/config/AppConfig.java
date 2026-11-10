package com.microServicioEvento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * Crea el RestTemplate para comunicar los microservicios
     *
     * @return instancia de RestTemplate
     */
    @Bean //@Bean: registra el objeto RestTemplate para que Spring pueda inyectarlo donde sea necesario
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}