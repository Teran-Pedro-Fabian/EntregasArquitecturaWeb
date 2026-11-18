import com.microserviciocliente.controller.ClienteController;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.servise.RecomendacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecomendacionControllerTest {
    @Mock private RecomendacionService recomendacionService;
    @InjectMocks private ClienteController clienteController;

    @Test
    void devuelveRecomendacionesDelCliente() {
        EventoEntityDTO evento = new EventoEntityDTO();
        evento.setId("e1");
        when(recomendacionService.obtenerRecomendaciones("c1")).thenReturn(List.of(evento));

        ResponseEntity<?> respuesta = clienteController.obtenerRecomendaciones("c1");
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(List.of(evento), respuesta.getBody());
    }

    @Test
    void clienteInexistenteDevuelve404() {
        when(recomendacionService.obtenerRecomendaciones("c404"))
                .thenThrow(new NoSuchElementException("Cliente no encontrado"));

        assertEquals(HttpStatus.NOT_FOUND,
                clienteController.obtenerRecomendaciones("c404").getStatusCode());
    }

    @Test
    void fallaServicioExternoDevuelve503() {
        when(recomendacionService.obtenerRecomendaciones("c1"))
                .thenThrow(new ResourceAccessException("Evento no responde"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                clienteController.obtenerRecomendaciones("c1").getStatusCode());
    }
}
