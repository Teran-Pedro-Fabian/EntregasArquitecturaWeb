import com.microServicioEvento.entity.CambioAvisoEntity;
import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.repository.CambioAvisoRepository;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import com.microServicioEvento.service.NotificacionEventoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificacionEventoServiceTest {
    @Mock EventoRepository eventoRepository;
    @Mock CambioAvisoRepository cambioAvisoRepository;
    @Mock EventoParticipanteRepositoy participanteRepository;
    @Mock RestTemplate restTemplate;
    @InjectMocks NotificacionEventoService service;

    @Test void publicacionRecuperaCiudadYMarcaAvisada() {
        ReflectionTestUtils.setField(service, "lugarUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(service, "clienteUrl", "http://localhost:8081");
        EventoEntity e = new EventoEntity();
        e.setId("evento-1"); e.setNombre("Recital"); e.setIdLugar(1L);
        EventoParticipanteEntity p = new EventoParticipanteEntity();
        p.setIdParticipante("artista-1");
        when(eventoRepository.findTop50ByEstadoAndPublicacionAvisadaFalseOrderByIdAsc("PUBLICADO"))
                .thenReturn(List.of(e));
        when(participanteRepository.findByIdEvento("evento-1")).thenReturn(List.of(p));
        when(restTemplate.getForObject("http://localhost:8083/lugares", Map[].class))
                .thenReturn(new Map[]{Map.of("id", 1, "ciudad", "Tandil")});
        service.avisarPendientes();
        assertTrue(e.isPublicacionAvisada());
        verify(eventoRepository).save(e);
        verify(restTemplate).postForEntity(contains("/notificaciones/publicacion"), anyMap(), eq(Void.class));
    }

    @Test void cambioConservaLaReferenciaSiSeReintenta() {
        ReflectionTestUtils.setField(service, "ticketUrl", "http://localhost:8084");
        CambioAvisoEntity aviso = new CambioAvisoEntity("evento-1", "Nueva fecha");
        when(cambioAvisoRepository.findTop50ByEntregadoFalseOrderByFechaCreacionAsc())
                .thenReturn(List.of(aviso));
        service.avisarPendientes();
        assertTrue(aviso.isEntregado());
        verify(cambioAvisoRepository).save(aviso);
        verify(restTemplate).postForEntity(contains("/notificar-cambio"), anyMap(), eq(Void.class));
    }
}
