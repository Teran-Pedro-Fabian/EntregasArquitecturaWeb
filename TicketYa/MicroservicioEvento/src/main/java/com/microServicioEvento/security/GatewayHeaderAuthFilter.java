package com.microServicioEvento.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extraer los headers inyectados por el API Gateway
        String userId = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-User-Roles");

        // 2. Si viene el ID del usuario, construir la autenticacion en Spring Security
        if (userId != null && !userId.isBlank()) {
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();

            if (rolesHeader != null && !rolesHeader.isBlank()) {
                authorities = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        // Agrega el prefijo "ROLE_" para que funcionen hasRole() y hasAnyRole() en @PreAuthorize
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            }

            // 3. Guardar el usuario y sus roles en el contexto de seguridad de la peticion actual
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 4. Continuar con la cadena de filtros hacia el Controller
        filterChain.doFilter(request, response);
    }
}
