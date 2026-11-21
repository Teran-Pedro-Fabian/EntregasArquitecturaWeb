package com.ticketya.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(reglas -> reglas
                        // Nadie puede entrar a las rutas internas por el Gateway
                        .pathMatchers(
                                "/clientes/interno", "/clientes/interno/**",
                                "/evento/interno", "/evento/interno/**",
                                "/lugares/interno", "/lugares/interno/**",
                                "/ticket/interno", "/ticket/interno/**"
                        ).denyAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.POST, "/clientes", "/clientes/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/evento", "/evento/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/lugares", "/lugares/buscarPorCiudad", "/lugares/buscarPorUbicacion", "/lugares/sectores/*/disponibilidad", "/lugares/eventos/*/sectores", "/lugares/sectores/*/asientos").permitAll()
                        // Todas las escrituras y datos privados exigen JWT
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
