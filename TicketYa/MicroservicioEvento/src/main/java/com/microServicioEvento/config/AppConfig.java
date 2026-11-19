package com.microServicioEvento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate(
            @Value("${ticketya.internal.key}") String claveInterna,
            @Value("${ticketya.http.connect-timeout-ms:2000}") int connectTimeout,
            @Value("${ticketya.http.read-timeout-ms:5000}") int readTimeout,
            @Value("${ticketya.http.get-retries:1}") int getRetries,
            @Value("${ticketya.http.retry-delay-ms:150}") long retryDelayMs,
            @Value("${ticketya.cb.fallos:3}") int circuitFallos,
            @Value("${ticketya.cb.pausa-ms:10000}") long circuitPausaMs) {
        // No dejar llamadas REST esperando indefinidamente
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("X-TicketYa-Internal", claveInterna);
            String traceId = org.slf4j.MDC.get("traceId");
            if (traceId != null) request.getHeaders().set("X-Trace-Id", traceId);
            return execution.execute(request, body);
        });
        // El Circuit Breaker observa el resultado final despues del reintento GET
        restTemplate.getInterceptors().add(new CircuitBreakerGetInterceptor(circuitFallos, circuitPausaMs));
        // Reintentar solo GET por fallos transitorios (nunca compras ni compensaciones)
        restTemplate.getInterceptors().add(new ReintentoGetInterceptor(getRetries, retryDelayMs));
        return restTemplate;
    }
}
