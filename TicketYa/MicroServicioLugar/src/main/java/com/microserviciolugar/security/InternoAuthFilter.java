package com.microserviciolugar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class InternoAuthFilter extends OncePerRequestFilter {

    private final String claveInterna;

    public InternoAuthFilter(@Value("${ticketya.internal.key}") String claveInterna) {
        this.claveInterna = claveInterna;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String recibida = request.getHeader("X-TicketYa-Internal");
        if (recibida != null && !recibida.isBlank()) {
            if (!MessageDigest.isEqual(claveInterna.getBytes(StandardCharsets.UTF_8),
                    recibida.getBytes(StandardCharsets.UTF_8))) {
                // La clave interna es invalida: se rechaza la peticion
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            // La clave interna es valida: el servicio interno opera con rol de administrador
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("interno", null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
