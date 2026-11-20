import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.servise.JwtService;
import com.microserviciocliente.servise.LoginService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// El rol nunca se recibe del usuario; se decide segun sus credenciales
class AutenticacionSeguraRolesTest {
    private static final String SECRET = "ticketya-jwt-test-secret-clave-de-mas-de-32-bytes";
    private ClienteRepository clientes;
    private BCryptPasswordEncoder encoder;
    private LoginService login;

    @BeforeEach
    void configurar() {
        clientes = mock(ClienteRepository.class);
        encoder = new BCryptPasswordEncoder();
        login = new LoginService(clientes, encoder, new JwtService(SECRET));
        ReflectionTestUtils.setField(login, "idAdmin", "admin-test");
        ReflectionTestUtils.setField(login, "claveAdmin", "clave-admin-correcta");
        ReflectionTestUtils.setField(login, "idOrganizador", "organizador-test");
        ReflectionTestUtils.setField(login, "claveOrganizador", "clave-organizador-correcta");
        ReflectionTestUtils.setField(login, "idAcceso", "personal-test");
        ReflectionTestUtils.setField(login, "claveAcceso", "clave-personal-correcta");
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseClaimsJws(token).getBody();
    }

    @Test
    void administradorConClaveCorrectaRecibeRolYEmisorValidos() {
        Map<String, String> respuesta = login.ingresar("admin-test", "clave-admin-correcta");
        Claims jwt = claims(respuesta.get("token"));
        assertEquals("ADMINISTRADOR", respuesta.get("rol"));
        assertEquals("admin-test", jwt.getSubject());
        assertEquals("ADMINISTRADOR", jwt.get("roles", String.class));
        assertEquals("ticketya", jwt.getIssuer());
        assertNotNull(jwt.getExpiration());
    }

    @Test
    void passwordIncorrectoNoPermiteHacersePasarPorAdministrador() {
        when(clientes.findById("admin-test")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> login.ingresar("admin-test", "clave-invalida"));
    }

    @Test
    void clienteNormalNoObtieneRolAdministrador() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId("cliente-test");
        cliente.setPasswordHash(encoder.encode("clave-cliente-correcta"));
        when(clientes.findById("cliente-test")).thenReturn(Optional.of(cliente));
        Map<String, String> respuesta = login.ingresar("cliente-test", "clave-cliente-correcta");
        assertEquals("CLIENTE", respuesta.get("rol"));
        assertEquals("CLIENTE", claims(respuesta.get("token")).get("roles", String.class));
    }

    @Test
    void organizadorYPersonalRecibenSoloSuRolConfigurado() {
        assertEquals("ORGANIZADOR", login.ingresar("organizador-test", "clave-organizador-correcta").get("rol"));
        assertEquals("PERSONAL_ACCESO", login.ingresar("personal-test", "clave-personal-correcta").get("rol"));
    }

    @Test
    void usuarioSinPasswordNoPuedeIngresar() {
        assertThrows(IllegalArgumentException.class, () -> login.ingresar("admin-test", null));
        assertThrows(IllegalArgumentException.class, () -> login.ingresar("", "algo"));
    }
}
