
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

// IMPORTACIONES CLAVE QUE TE FALTABAN:
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TicketRepository ticketRepository; // <--- AGREGAR ESTO: El repo es quien busca

    @InjectMocks
    private TicketService ticketService;

    @Test
    public void testDevolucion_RechazadaPorTiempo() {
        // 1. Setup: Crear la Entidad (los datos)
        TicketEntity ticket = new TicketEntity();
        ticket.setId(1L);
        ticket.setIdEvento(100L);

        // 2. CORRECCIÓN: Llamamos al REPOSITORIO, no al ticket
        // "Cuando el REPOSITORIO busque el id 1, devuelve este ticket"
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // 3. Setup de la respuesta del MicroServicioEvento
        Map<String, Object> eventoMap = new HashMap<>();
        eventoMap.put("fechaHora", LocalDateTime.now().plusDays(1).toString());
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(eventoMap);

        // 4. Verificación
        assertThrows(RuntimeException.class, () -> {
            ticketService.devolverTicket(1L);
        });
    }
}
