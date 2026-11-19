package com.ticketya.gateway.filter;

import java.security.Principal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Registra entrada, usuario, codigo HTTP, duracion y traceId */
@Component
public class TrazabilidadGatewayFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(TrazabilidadGatewayFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String recibido = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        String trace = recibido != null && recibido.matches("[A-Za-z0-9-]{8,64}")
                ? recibido : UUID.randomUUID().toString();
        long inicio = System.nanoTime();
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Trace-Id", trace).build();
        exchange.getResponse().getHeaders().set("X-Trace-Id", trace);
        ServerWebExchange conTrace = exchange.mutate().request(request).build();
        return conTrace.getPrincipal().map(Principal::getName).defaultIfEmpty("anonimo")
                .flatMap(usuario -> chain.filter(conTrace).doFinally(senal -> {
                    long ms = (System.nanoTime() - inicio) / 1_000_000;
                    var status = conTrace.getResponse().getStatusCode();
                    log.info("trace={} metodo={} ruta={} usuario={} estado={} duracionMs={}",
                            trace, request.getMethod(), request.getPath().value(), usuario,
                            status == null ? "sin-estado" : status.value(), ms);
                }));
    }
    @Override public int getOrder() { return -110; }
}
