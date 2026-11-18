package com.microservicioticket.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    // Secret compartido con el Gateway para la firma de los JWT
    private static final String SECRET_JWT = "ticketya_gateway_jwt_secret_phase1_2026_abcdefg";

public String generarTokenJwt(String subject, String rol) {
    String roles = (rol == null || rol.isBlank()) ? "PERSONAL_ACCESO" : rol.toUpperCase();
    SecretKey key = Keys.hmacShaKeyFor(SECRET_JWT.getBytes(StandardCharsets.UTF_8));
    
    return Jwts.builder()
            .setSubject(subject)
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000L))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}


}
