import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.servise.JwtService;
import com.microserviciocliente.servise.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginServiceTest {
    ClienteRepository repo;
    LoginService service;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        repo = mock(ClienteRepository.class);
        service = new LoginService(repo, encoder,
                new JwtService("ticketya-clave-de-prueba-de-mas-de-32-caracteres"));
    }

    @Test
    void contraseñaIncorrectaEsRechazada() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setPasswordHash(encoder.encode("clave-correcta"));
        when(repo.findById("c1")).thenReturn(Optional.of(cliente));
        assertThrows(IllegalArgumentException.class, () -> service.ingresar("c1", "incorrecta"));
    }

    @Test
    void clienteConContraseñaCorrectaRecibeSoloRolCliente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setPasswordHash(encoder.encode("clave-correcta"));
        when(repo.findById("c1")).thenReturn(Optional.of(cliente));
        var token = service.ingresar("c1", "clave-correcta");
        assertEquals("CLIENTE", token.get("rol"));
        assertFalse(token.get("token").isBlank());
    }

    @Test
    void cuentaViejaSinContraseñaNoPuedeIngresar() {
        when(repo.findById("c1")).thenReturn(Optional.of(new ClienteEntity()));
        assertThrows(IllegalArgumentException.class, () -> service.ingresar("c1", "cualquier-clave"));
    }
}
