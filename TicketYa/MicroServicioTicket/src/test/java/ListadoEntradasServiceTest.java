import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.PagoMockService;
import com.microservicioticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListadoEntradasServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PagoMockService pagoMockService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    public void testListarEntradasCliente() {
        TicketEntity primero = new TicketEntity();
        primero.setId(20L);
        primero.setIdCliente("cliente1");

        TicketEntity segundo = new TicketEntity();
        segundo.setId(10L);
        segundo.setIdCliente("cliente1");

        when(ticketRepository.findByIdClienteOrderByIdDesc("cliente1"))
                .thenReturn(List.of(primero, segundo));

        List<TicketEntity> resultado = ticketService.obtenerTicketsCliente("cliente1");

        assertEquals(2, resultado.size());
        assertEquals(20L, resultado.get(0).getId());
        assertEquals(10L, resultado.get(1).getId());
    }
}
