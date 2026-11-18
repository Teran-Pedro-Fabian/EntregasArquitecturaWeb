package com.microServicioEvento.config;

import com.microServicioEvento.security.GatewayHeaderAuthFilter;
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
                    // 1. Rutas públicas de sistema, login y comunicación interna
                    .requestMatchers("/evento/login", "/evento/interno/**", "/actuator/**").permitAll()
                    // 2. Catálogo público: Cualquiera puede ver eventos, participantes y usar el buscador
                    .requestMatchers(HttpMethod.GET, "/evento", "/evento/**").permitAll()
                    // 3. Todo lo demás (crear, editar, publicar, cancelar, etc.) exige autenticación y pasa por @PreAuthorize
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
