import com.microservicioticket.Repository.AvisoCancelacionRepository;
import com.microservicioticket.entity.AvisoCancelacionEntity;
import com.microservicioticket.service.AvisoCancelacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvisoCancelacionServiceTest {
    @Mock private AvisoCancelacionRepository repository;
    @InjectMocks private AvisoCancelacionService service;

    @Test
    void noCrearAvisoDuplicado() {
        when(repository.existsByIdEventoAndIdCliente("evento-1","cliente-1")).thenReturn(true);
        service.registrar("evento-1","cliente-1");
        verify(repository,never()).saveAndFlush(any());
    }

    @Test
    void guardarAvisoCuandoNoExiste() {
        when(repository.existsByIdEventoAndIdCliente("evento-1","cliente-1")).thenReturn(false);
        service.registrar("evento-1","cliente-1");
        verify(repository).saveAndFlush(argThat(a ->
            "evento-1".equals(a.getIdEvento()) && "cliente-1".equals(a.getIdCliente())
            && a.getFechaCreacion()!=null));
    }

    @Test
    void rechazarIdentificadorVacio() {
        assertThrows(IllegalArgumentException.class,()->service.registrar("evento-1"," "));
        verifyNoInteractions(repository);
    }
}
