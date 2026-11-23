import com.microserviciocliente.controller.ClienteController;
import com.microserviciocliente.dto.LoginRequest;
import com.microserviciocliente.servise.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Prueba el login REAL, el controlador nunca recibe el rol del usuario
class ClienteControllerLoginSeguroTest {
    private LoginService loginService;
    private ClienteController controller;

    @BeforeEach
    void configurar() {
        loginService = mock(LoginService.class);
        controller = new ClienteController();
        ReflectionTestUtils.setField(controller, "loginService", loginService);
    }

    @Test
    void credencialesValidasDevuelvenElTokenDelServicio() {
        when(loginService.ingresar("cliente-1", "clave-correcta"))
                .thenReturn(Map.of("token", "jwt-de-prueba", "idUsuario", "cliente-1", "rol", "CLIENTE"));

        ResponseEntity<?> respuesta = controller.login(new LoginRequest("cliente-1", "clave-correcta"));

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals("CLIENTE", ((Map<?, ?>) respuesta.getBody()).get("rol"));
        assertEquals("jwt-de-prueba", ((Map<?, ?>) respuesta.getBody()).get("token"));
        verify(loginService).ingresar("cliente-1", "clave-correcta");
    }

    @Test
    void credencialesIncorrectasDevuelven401() {
        when(loginService.ingresar("cliente-1", "incorrecta"))
                .thenThrow(new IllegalArgumentException("Usuario o contraseña incorrectos"));

        ResponseEntity<?> respuesta = controller.login(new LoginRequest("cliente-1", "incorrecta"));

        assertEquals(401, respuesta.getStatusCode().value());
        verify(loginService).ingresar("cliente-1", "incorrecta");
    }

    @Test
    void claveVaciaDevuelve401() {
        when(loginService.ingresar("cliente-1", ""))
                .thenThrow(new IllegalArgumentException("Usuario o contraseña incorrectos"));

        ResponseEntity<?> respuesta = controller.login(new LoginRequest("cliente-1", ""));

        assertEquals(401, respuesta.getStatusCode().value());
    }
}
