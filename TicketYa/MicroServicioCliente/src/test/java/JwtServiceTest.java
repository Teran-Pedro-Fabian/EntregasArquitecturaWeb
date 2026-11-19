import com.microserviciocliente.servise.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "ticketya_gateway_jwt_secret_phase1_2026_abcdefg";
    private final JwtService jwtService = new JwtService();

    private SecretKey key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generarTokenJwt_conRolCliente() {
        String token = jwtService.generarTokenJwt("cliente-01", "CLIENTE");
        Claims claims = Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
        assertEquals("cliente-01", claims.getSubject());
        assertEquals("CLIENTE", claims.get("roles"));
    }

    @Test
    void generarTokenJwt_conRolOrganizador() {
        String token = jwtService.generarTokenJwt("organizador-01", "ORGANIZADOR");
        Claims claims = Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
        assertEquals("organizador-01", claims.getSubject());
        assertEquals("ORGANIZADOR", claims.get("roles"));
    }

    @Test
    void generarTokenJwt_conRolAdministrador() {
        String token = jwtService.generarTokenJwt("administrador-01", "ADMINISTRADOR");
        Claims claims = Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
        assertEquals("administrador-01", claims.getSubject());
        assertEquals("ADMINISTRADOR", claims.get("roles"));
    }

    @Test
    void generarTokenJwt_conRolPersonalAcceso() {
        String token = jwtService.generarTokenJwt("personal-01", "PERSONAL_ACCESO");
        Claims claims = Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
        assertEquals("personal-01", claims.getSubject());
        assertEquals("PERSONAL_ACCESO", claims.get("roles"));
    }

    @Test
    void generarTokenJwt_conRolNuloUsaClientePorDefecto() {
        String token = jwtService.generarTokenJwt("cliente-02", null);
        Claims claims = Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
        assertEquals("cliente-02", claims.getSubject());
        assertEquals("CLIENTE", claims.get("roles"));
    }
}
