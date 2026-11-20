package com.microserviciolugar.config;

import com.microserviciolugar.security.GatewayHeaderAuthFilter;
import com.microserviciolugar.security.InternoAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
// 1. Obligatorio para que funcionen los @PreAuthorize de ClienteController
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayHeaderAuthFilter gatewayHeaderAuthFilter;
    private final InternoAuthFilter internoAuthFilter;

    // 2. Inyectamos los filtros que leen X-User-Id y X-User-Roles del Gateway
    //    o la clave interna X-TicketYa-Internal
    public SecurityConfig(GatewayHeaderAuthFilter gatewayHeaderAuthFilter, InternoAuthFilter internoAuthFilter) {
        this.gatewayHeaderAuthFilter = gatewayHeaderAuthFilter;
        this.internoAuthFilter = internoAuthFilter;
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
                    .requestMatchers("/lugares/interno/**", "/actuator/**").permitAll()
                    // 2. Catálogo público: Cualquiera puede ver lugares y buscar
                    .requestMatchers(HttpMethod.GET, "/lugares", "/lugares/buscarPorCiudad", "/lugares/buscarPorUbicacion").permitAll()
                    // 3. Todo lo demás exige autenticación y pasa por @PreAuthorize
                    .anyRequest().authenticated()
                )
                // Sin autenticacion la peticion se rechaza con 401 en lugar de 403
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 5. Ejecutar la validacion de clave interna y el header del Gateway antes de verificar los permisos
                .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internoAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // 7. Cierre obligatorio para construir el SecurityFilterChain
                .build();
    }
}
