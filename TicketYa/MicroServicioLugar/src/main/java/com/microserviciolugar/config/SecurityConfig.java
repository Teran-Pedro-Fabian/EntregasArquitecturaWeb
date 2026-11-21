package com.microserviciolugar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${ticketya.internal.key}") private String claveInterna;

    @Bean
    public JwtDecoder jwtDecoder(@Value("${ticketya.jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("TICKETYA_JWT_SECRET debe tener al menos 32 bytes");
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("ticketya"));
        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Estas rutas solo aceptan la clave privada entre microservicios
                        .requestMatchers("/lugares/interno/**")
                        .access((usuario, contexto) -> new AuthorizationDecision(esInterna(contexto.getRequest().getHeader("X-TicketYa-Internal"))))
                        .requestMatchers(HttpMethod.POST, "/lugares/asiento/confirmar")
                        .access((usuario, contexto) -> new AuthorizationDecision(esInterna(contexto.getRequest().getHeader("X-TicketYa-Internal"))))
                        // Ticket puede cancelar reservas con su clave interna, sin JWT de cliente
                        // Los clientes autenticados siguen sujetos a @PreAuthorize (propietario o admin)
                        .requestMatchers(HttpMethod.DELETE, "/lugares/reservas/*", "/lugares/sectores/generales/reservas/*")
                        .access((usuario, contexto) -> new AuthorizationDecision(
                                esInterna(contexto.getRequest().getHeader("X-TicketYa-Internal"))
                                        || usuario.get() instanceof JwtAuthenticationToken))
                        .requestMatchers(HttpMethod.GET, "/lugares", "/lugares/buscarPorCiudad", "/lugares/buscarPorUbicacion", "/lugares/sectores/*/disponibilidad", "/lugares/eventos/*/sectores").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    private boolean esInterna(String recibida) {
        return claveInterna != null && !claveInterna.isBlank() && recibida != null
                && MessageDigest.isEqual(claveInterna.getBytes(StandardCharsets.UTF_8), recibida.getBytes(StandardCharsets.UTF_8));
    }
}
