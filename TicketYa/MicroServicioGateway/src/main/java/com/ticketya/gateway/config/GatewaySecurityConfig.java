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
                .authorizeExchange(exchanges -> exchanges
                        // 1. Monitoreo y salud del sistema
                        .pathMatchers("/actuator/**", "/actuator", "/health", "/info").permitAll()
                        
                        // 2. Registro de nuevos clientes y endpoints de Login
                        .pathMatchers(HttpMethod.POST, "/clientes").permitAll()
                        .pathMatchers("/clientes/login", "/evento/login", "/ticket/login").permitAll()
                        
                        // 3. AJUSTE CLAVE: Consultas publicas (solo GET) para ver eventos y lugares sin iniciar sesion
                        .pathMatchers(HttpMethod.GET, "/evento", "/evento/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/lugares", "/lugares/buscarPorCiudad", "/lugares/buscarPorUbicacion").permitAll()
                        
                        // 4. Seguridad extra: Nadie desde afuera (Internet) puede llamar a endpoints internos
                        .pathMatchers("/**/interno/**").denyAll()
                        
                        // 5. Todo lo demas (comprar, reservar, crear eventos, ver perfil) exige token JWT
                        .pathMatchers("/ticket/**", "/evento/**", "/lugares/**", "/clientes/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> { }))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}


