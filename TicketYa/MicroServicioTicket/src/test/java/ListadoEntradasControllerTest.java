import com.microservicioticket.controller.TicketController;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListadoEntradasControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @Test
    public void testListarEntradasCliente() {
        TicketEntity ticket = new TicketEntity();
        ticket.setId(10L);
        ticket.setIdCliente("cliente1");

        when(ticketService.obtenerTicketsCliente("cliente1"))
                .thenReturn(List.of(ticket));

        ResponseEntity<?> respuesta = ticketController.obtenerPorCliente("cliente1");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
    }
}
