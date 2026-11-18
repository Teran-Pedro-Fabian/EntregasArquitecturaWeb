package com.ticketya.gateway.config;

import com.ticketya.gateway.filter.JwtPropagationGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    // Secret para la firma HMAC simetrica (HS256) de los JWT del Gateway
    // (Debe tener al menos 32 bytes / 256 bits para HS256; el tuyo tiene 47, asi que cumple bien)
    private static final String SECRET = "ticketya_gateway_jwt_secret_phase1_2026_abcdefg";

    // 1. Bean REACTIVO: Lo usara tanto GatewaySecurityConfig como tu filtro
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    // 2. Registro del filtro (IMPORTANTE: Quita @Component de JwtPropagationGatewayFilterFactory si usas este @Bean)
    @Bean
    public JwtPropagationGatewayFilterFactory jwtPropagationGatewayFilterFactory(ReactiveJwtDecoder reactiveJwtDecoder) {
        return new JwtPropagationGatewayFilterFactory(reactiveJwtDecoder);
    }
}
