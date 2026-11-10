import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.service.EventoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {
    @Mock private EventoRepository eventoRepository;
    @InjectMocks private EventoService eventoService;

    @Test
    void crearSiempreEnBorrador() {
        EventoEntity evento = new EventoEntity();
        evento.setId("id-no-debe-conservarse");
        evento.setNombre("Recital de prueba");
        evento.setDescripcion("Descripcion de prueba");
        evento.setTipo("RECITAL");
        evento.setOrganizador("organizador-test");
        evento.setFechaHora(LocalDateTime.now().plusDays(30).toString());
        evento.setIdLugar(1L);
        evento.setEstado("PUBLICADO");
        when(eventoRepository.save(any(EventoEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        EventoEntity resultado = eventoService.crearEvento(evento);
        assertEquals("BORRADOR", resultado.getEstado());
        assertNull(resultado.getId());
        verify(eventoRepository).save(any(EventoEntity.class));
    }

    @Test
    void listarSoloPublicadosYAgotados() {
        EventoEntity e = new EventoEntity();
        e.setEstado("PUBLICADO");
        when(eventoRepository.findByEstadoIn(List.of("PUBLICADO", "AGOTADO")))
                .thenReturn(List.of(e));
        assertEquals(1, eventoService.buscarEventosPublicados().size());
    }

    @Test
    void noCancelarEventoFinalizado() {
        EventoEntity e = new EventoEntity();
        e.setId("e1");
        e.setEstado("FINALIZADO");
        when(eventoRepository.findById("e1")).thenReturn(Optional.of(e));
        assertThrows(IllegalStateException.class,
                () -> eventoService.cancelarEvento("e1", "admin"));
        verify(eventoRepository, never()).save(any());
    }
}
