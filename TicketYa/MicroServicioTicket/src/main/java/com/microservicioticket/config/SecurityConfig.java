package com.microservicioticket.config;

import com.microservicioticket.security.GatewayHeaderAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// 1. Obligatorio para que funcionen los @PreAuthorize de ClienteController
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    // 2. Inyectamos el filtro que lee X-User-Id y X-User-Roles del Gateway
    public SecurityConfig(GatewayHeaderAuthFilter gatewayHeaderAuthFilter) {
        this.gatewayHeaderAuthFilter = gatewayHeaderAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // 3. API sin estado (no guarda sesiones en memoria)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. Reglas reales de acceso (reemplaza el permitAll global)
                .authorizeHttpRequests(auth -> auth
                    // 1. Login de personal de acceso, endpoints internos (ej. cancelacion masiva desde Evento) y monitoreo
                    .requestMatchers("/ticket/login", "/ticket/interno/**", "/actuator/**").permitAll()
                    // 2. El resto (consultar tickets, comprar en CompraController, devolver, escanear QR) exige autenticación
                    .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 5. Ejecutar nuestro filtro antes de verificar los permisos
                .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 6. Cierre obligatorio para construir el SecurityFilterChain
                .build();
    }
}
