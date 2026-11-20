import com.microserviciocliente.config.SecurityConfig;
import com.microserviciocliente.servise.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// Verifica firma, emisor y rol con el decoder real usado por Spring Security
class JwtCompatibleConSeguridadTest {
    private static final String SECRET = "ticketya-jwt-test-secret-clave-de-mas-de-32-bytes";
    private static final String OTRA_SECRET = "otra-jwt-test-secret-distinta-de-mas-de-32-bytes";

    @Test
    void jwtGeneradoEsAceptadoPorElDecoderYConservaRol() {
        String token = new JwtService(SECRET).generarToken("cliente-1", "CLIENTE");
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(SECRET);
        Jwt jwt = decoder.decode(token);

        assertEquals("cliente-1", jwt.getSubject());
        assertEquals("ticketya", jwt.getIssuer().toString());
        assertEquals("CLIENTE", jwt.getClaimAsString("roles"));
        assertNotNull(jwt.getExpiresAt());

        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("ROLE_");
        Set<String> permisos = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(permisos.contains("ROLE_CLIENTE"));
        assertFalse(permisos.contains("ROLE_ADMINISTRADOR"));
    }

    @Test
    void jwtFirmadoConOtraClaveEsRechazado() {
        String token = new JwtService(SECRET).generarToken("cliente-1", "CLIENTE");
        JwtDecoder decoder = new SecurityConfig().jwtDecoder(OTRA_SECRET);
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }
}
