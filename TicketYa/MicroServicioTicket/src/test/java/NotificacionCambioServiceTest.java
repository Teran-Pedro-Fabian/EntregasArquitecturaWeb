import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.NotificacionCambioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificacionCambioServiceTest {
    @Mock TicketRepository ticketRepository;
    @Mock CompraRepository compraRepository;
    @Mock RestTemplate restTemplate;
    @InjectMocks NotificacionCambioService service;

    @Test void avisarUnaVezPorCompradorConfirmado() {
        ReflectionTestUtils.setField(service, "clienteUrl", "http://localhost:8081");
        TicketEntity t1 = new TicketEntity(); t1.setIdCompra(1L); t1.setIdCliente("c1");
        TicketEntity t2 = new TicketEntity(); t2.setIdCompra(1L); t2.setIdCliente("c1");
        CompraEntity c = new CompraEntity(); c.setEstado("CONFIRMADA");
        when(ticketRepository.findByIdEventoOrderByIdAsc("e1")).thenReturn(List.of(t1,t2));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(c));
        assertEquals(1, service.notificarCambio("e1", "cambio-1", "Nueva fecha"));
        verify(restTemplate, times(1)).postForEntity(contains("/notificaciones/cambio"),
                anyMap(), eq(Void.class));
    }
}
