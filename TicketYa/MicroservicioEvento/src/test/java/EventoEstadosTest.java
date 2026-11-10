import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.service.EventoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoEstadosTest {
    @Mock private EventoRepository eventoRepository;
    @InjectMocks private EventoService eventoService;

    private EventoEntity evento(String estado) {
        EventoEntity e = new EventoEntity();
        e.setId("evento-1");
        e.setNombre("Recital");
        e.setOrganizador("organizador-1");
        e.setEstado(estado);
        e.setFechaHora(LocalDateTime.now().plusDays(5).toString());
        return e;
    }

    @Test
    void noSuspenderBorrador() {
        when(eventoRepository.findById("evento-1")).thenReturn(Optional.of(evento("BORRADOR")));
        assertThrows(IllegalStateException.class, () -> eventoService.suspenderEvento("evento-1", "admin-1"));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void noPublicarPorOtroOrganizador() {
        when(eventoRepository.findById("evento-1")).thenReturn(Optional.of(evento("BORRADOR")));
        assertThrows(RuntimeException.class, () -> eventoService.publicarEvento("evento-1", "otro"));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void noFinalizarEventoFuturo() {
        when(eventoRepository.findById("evento-1")).thenReturn(Optional.of(evento("PUBLICADO")));
        assertThrows(IllegalStateException.class, () -> eventoService.finalizarEvento("evento-1", "admin-1"));
        verify(eventoRepository, never()).save(any());
    }
}
