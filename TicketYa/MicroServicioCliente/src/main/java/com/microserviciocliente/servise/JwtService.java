package com.microserviciocliente.servise;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // Secret compartido con el Gateway para la firma de los JWT
    private static final String SECRET_JWT = "ticketya_gateway_jwt_secret_phase1_2026_abcdefg";
    private static final long EXPIRATION_TIME = 86400000L; // 24 horas en milisegundos

    /**
     * Genera un token JWT firmado con HS256 para el usuario y rol indicados.
     */
    public String generarTokenJwt(String subject, String rol) {
        String roles = (rol == null || rol.isBlank()) ? "CLIENTE" : rol.toUpperCase();
        SecretKey key = Keys.hmacShaKeyFor(SECRET_JWT.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
