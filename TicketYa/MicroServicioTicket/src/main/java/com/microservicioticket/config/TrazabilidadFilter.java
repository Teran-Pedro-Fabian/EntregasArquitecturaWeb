package com.microservicioticket.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Identificador compartido para buscar una solicitud entre servicios */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrazabilidadFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TrazabilidadFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String trace = request.getHeader("X-Trace-Id");
        if (trace == null || !trace.matches("[A-Za-z0-9-]{8,64}")) trace = UUID.randomUUID().toString();
        long inicio = System.nanoTime();
        MDC.put("traceId", trace);
        response.setHeader("X-Trace-Id", trace);
        try {
            chain.doFilter(request, response);
        } finally {
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            Object usuario = request.getAttribute("ticketya.usuario");
            log.info("trace={} metodo={} ruta={} usuario={} estado={} duracionMs={}",
                    trace, request.getMethod(), request.getRequestURI(),
                    usuario == null ? "anonimo" : usuario, response.getStatus(), duracionMs);
            MDC.remove("traceId");
        }
    }
}
