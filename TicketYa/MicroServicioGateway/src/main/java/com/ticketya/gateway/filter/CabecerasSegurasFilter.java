package com.ticketya.gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CabecerasSegurasFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Los clientes externos no pueden simular una llamada entre microservicios
        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> {
            headers.remove("X-TicketYa-Internal");
            headers.remove("X-User-Id");
            headers.remove("X-User-Roles");
        }).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() { return -100; }
}
