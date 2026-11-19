import com.microserviciocliente.controller.ClienteController;
import com.microserviciocliente.dto.LoginRequest;
import com.microserviciocliente.dto.TokenResponse;
import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerLoginTest {

    @Mock private ClienteService clienteService;
    @Mock private JwtService jwtService;
    @InjectMocks private ClienteController clienteController;

    private LoginRequest buildRequest(String idCliente, String rol) {
        LoginRequest datos = new LoginRequest();
        datos.setIdCliente(idCliente);
        datos.setRol(rol);
        return datos;
    }

    @Test
    void login_conRolCliente_retornaOkConToken() {
        LoginRequest datos = buildRequest("cliente-01", "CLIENTE");
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId("cliente-01");
        cliente.setNombre("Juan Perez");
        when(clienteService.findById("cliente-01")).thenReturn(cliente);
        when(jwtService.generarTokenJwt("cliente-01", "CLIENTE")).thenReturn("token-cliente-01");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(200, response.getStatusCode().value());
        TokenResponse body = (TokenResponse) response.getBody();
        assertNotNull(body);
        assertEquals("token-cliente-01", body.getToken());
        assertEquals("cliente-01", body.getIdCliente());
        assertEquals("CLIENTE", body.getRoles());
        verify(jwtService).generarTokenJwt("cliente-01", "CLIENTE");
    }

    @Test
    void login_conRolOrganizador_retornaOkConToken() {
        LoginRequest datos = buildRequest("organizador-01", "ORGANIZADOR");
        when(jwtService.generarTokenJwt("organizador-01", "ORGANIZADOR")).thenReturn("token-organizador-01");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(200, response.getStatusCode().value());
        TokenResponse body = (TokenResponse) response.getBody();
        assertEquals("token-organizador-01", body.getToken());
        assertEquals("ORGANIZADOR", body.getRoles());
        verify(jwtService).generarTokenJwt("organizador-01", "ORGANIZADOR");
    }

    @Test
    void login_conRolAdministrador_retornaOkConToken() {
        LoginRequest datos = buildRequest("administrador-01", "ADMINISTRADOR");
        when(jwtService.generarTokenJwt("administrador-01", "ADMINISTRADOR")).thenReturn("token-administrador-01");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(200, response.getStatusCode().value());
        TokenResponse body = (TokenResponse) response.getBody();
        assertEquals("token-administrador-01", body.getToken());
        assertEquals("ADMINISTRADOR", body.getRoles());
        verify(jwtService).generarTokenJwt("administrador-01", "ADMINISTRADOR");
    }

    @Test
    void login_conRolPersonalAcceso_retornaOkConToken() {
        LoginRequest datos = buildRequest("personal-01", "PERSONAL_ACCESO");
        when(jwtService.generarTokenJwt("personal-01", "PERSONAL_ACCESO")).thenReturn("token-personal-01");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(200, response.getStatusCode().value());
        TokenResponse body = (TokenResponse) response.getBody();
        assertEquals("token-personal-01", body.getToken());
        assertEquals("PERSONAL_ACCESO", body.getRoles());
        verify(jwtService).generarTokenJwt("personal-01", "PERSONAL_ACCESO");
    }

    @Test
    void login_conRolInvalido_retornaBadRequest() {
        LoginRequest datos = buildRequest("cliente-01", "ROLOFICIO");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(400, response.getStatusCode().value());
        verify(jwtService, never()).generarTokenJwt(anyString(), anyString());
    }

    @Test
    void login_sinIdCliente_retornaBadRequest() {
        LoginRequest datos = buildRequest(null, "CLIENTE");

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(400, response.getStatusCode().value());
        verify(jwtService, never()).generarTokenJwt(anyString(), anyString());
    }

    @Test
    void login_clienteNoExisteConRolCliente_retornaNotFound() {
        LoginRequest datos = buildRequest("ausente", "CLIENTE");
        when(clienteService.findById("ausente")).thenReturn(null);

        ResponseEntity<?> response = clienteController.login(datos);

        assertEquals(404, response.getStatusCode().value());
        verify(jwtService, never()).generarTokenJwt(anyString(), anyString());
    }
}
