package com.microserviciocliente.servise;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey clave;

    public JwtService(@Value("${ticketya.jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("TICKETYA_JWT_SECRET debe tener al menos 32 bytes");
        }
        clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String idUsuario, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .setIssuer("ticketya")
                .setSubject(idUsuario)
                .claim("roles", rol)
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + 3600000))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }
}
