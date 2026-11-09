
import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.service.EventoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.web.client.RestTemplate;

import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EventoService eventoService;

    @Test
    public void testBuscarPorTipo_RetornaCorrectamente() {

        EventoEntity evento = new EventoEntity();
        evento.setNombre("Rock Fest");
        evento.setTipo("Recital");

        when(mongoTemplate.find(any(), eq(EventoEntity.class)))
                .thenReturn(List.of(evento));


        List<EventoEntity> resultados = eventoService.buscarEventos("Recital", null, null, null, null, "fecha");


        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());
        assertEquals("Recital", resultados.get(0).getTipo());
    }
}
