import com.microservicioticket.Repository.AvisoCancelacionRepository;
import com.microservicioticket.entity.AvisoCancelacionEntity;
import com.microservicioticket.service.EntregaAvisoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntregaAvisoServiceTest {
    @Mock AvisoCancelacionRepository repo;
    @Mock RestTemplate restTemplate;
    @InjectMocks EntregaAvisoService service;

    private AvisoCancelacionEntity aviso() {
        AvisoCancelacionEntity a = new AvisoCancelacionEntity();
        a.setIdEvento("evento-1");
        a.setIdCliente("cliente-1");
        a.setMensaje("Evento cancelado");
        a.setFechaCreacion(Instant.now());
        return a;
    }

    @Test
    void entregarGuardaFechaSoloCuandoClienteResponde() {
        AvisoCancelacionEntity a = aviso();
        when(repo.findByIdEventoAndIdCliente("evento-1", "cliente-1"))
                .thenReturn(Optional.of(a));
        service.entregar("evento-1", "cliente-1");
        verify(restTemplate).postForEntity(contains("/notificaciones/cancelacion"), anyMap(), eq(Void.class));
        verify(repo).saveAndFlush(a);
        assertNotNull(a.getFechaEntrega());
    }

    @Test
    void noVolverAEnviarSiYaFueEntregado() {
        AvisoCancelacionEntity a = aviso();
        a.setFechaEntrega(Instant.now());
        when(repo.findByIdEventoAndIdCliente("evento-1", "cliente-1"))
                .thenReturn(Optional.of(a));
        service.entregar("evento-1", "cliente-1");
        verifyNoInteractions(restTemplate);
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void falloDeClienteDejaAvisoPendiente() {
        AvisoCancelacionEntity a = aviso();
        when(repo.findByIdEventoAndIdCliente("evento-1", "cliente-1"))
                .thenReturn(Optional.of(a));
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Void.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Sin conexion"));
        assertThrows(org.springframework.web.client.ResourceAccessException.class,
                () -> service.entregar("evento-1", "cliente-1"));
        verify(repo, never()).saveAndFlush(any());
        assertNull(a.getFechaEntrega());
    }
}
