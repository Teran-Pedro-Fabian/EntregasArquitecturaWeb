import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.service.NotificacionCompraService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionCompraServiceTest {
    @Mock CompraRepository repo;
    @Mock RestTemplate restTemplate;
    @InjectMocks NotificacionCompraService service;
    private CompraEntity compra() {
        CompraEntity c = new CompraEntity(); c.setId(15L);
        c.setIdCliente("cliente-1"); c.setIdEvento("evento-1");
        c.setTotal(12000.0); c.setEstado("CONFIRMADA"); return c;
    }
    @Test void marcarSoloCuandoClienteConfirmo() {
        ReflectionTestUtils.setField(service, "clienteUrl", "http://localhost:8081");
        CompraEntity c = compra();
        when(repo.findTop50ByEstadoAndNotificacionEnviadaFalseOrderByIdAsc("CONFIRMADA"))
                .thenReturn(List.of(c));
        service.avisarComprasConfirmadas();
        assertTrue(c.isNotificacionEnviada());
        verify(repo).save(c);
        verify(restTemplate).postForEntity(contains("/notificaciones/compra"), anyMap(), eq(Void.class));
    }
    @Test void falloEnClientePermiteReintento() {
        ReflectionTestUtils.setField(service, "clienteUrl", "http://localhost:8081");
        CompraEntity c = compra();
        when(repo.findTop50ByEstadoAndNotificacionEnviadaFalseOrderByIdAsc("CONFIRMADA"))
                .thenReturn(List.of(c));
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Cliente caido"));
        service.avisarComprasConfirmadas();
        assertFalse(c.isNotificacionEnviada());
        verify(repo, never()).save(any());
    }
}
