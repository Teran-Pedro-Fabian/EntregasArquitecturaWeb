import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.repository.NotificacionRepository;
import com.microserviciocliente.servise.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {
    @Mock NotificacionRepository repo;
    @InjectMocks NotificacionService service;

    private NotificacionCancelacionRequest datos() {
        NotificacionCancelacionRequest d = new NotificacionCancelacionRequest();
        d.setIdEvento("evento-1");
        d.setIdCliente("cliente-1");
        d.setMensaje("El evento fue cancelado");
        return d;
    }

    @Test
    void guardarNotificacionNueva() {
        when(repo.findByIdEventoAndIdCliente("evento-1", "cliente-1"))
                .thenReturn(Optional.empty());
        service.recibirCancelacion(datos());
        verify(repo).saveAndFlush(argThat(n ->
                "evento-1".equals(n.getIdEvento()) &&
                "cliente-1".equals(n.getIdCliente()) &&
                n.getFechaCreacion() != null));
    }

    @Test
    void noDuplicarCuandoTicketReintenta() {
        NotificacionEntity existente = new NotificacionEntity();
        existente.setIdEvento("evento-1");
        existente.setIdCliente("cliente-1");
        when(repo.findByIdEventoAndIdCliente("evento-1", "cliente-1"))
                .thenReturn(Optional.of(existente));
        assertSame(existente, service.recibirCancelacion(datos()));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void rechazarDatosInvalidos() {
        NotificacionCancelacionRequest d = datos();
        d.setIdCliente(" ");
        assertThrows(IllegalArgumentException.class, () -> service.recibirCancelacion(d));
        verifyNoInteractions(repo);
    }
}
