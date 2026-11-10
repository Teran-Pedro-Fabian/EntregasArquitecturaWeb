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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @Test
    public void testEmitir_Exitoso() {
        TicketEntity ticket = new TicketEntity();
        ticket.setId(10L);
        when(ticketService.emitirTicket(ticket)).thenReturn(ticket);
        ResponseEntity<?> respuesta = ticketController.emitir(ticket);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(ticket, respuesta.getBody());
    }

    @Test
    public void testEmitir_DatosInvalidos() {
        when(ticketService.emitirTicket(any(TicketEntity.class)))
                .thenThrow(new IllegalArgumentException("Datos del ticket incompletos"));
        ResponseEntity<?> respuesta = ticketController.emitir(new TicketEntity());
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    public void testEmitir_PagoRechazado() {
        when(ticketService.emitirTicket(any(TicketEntity.class)))
                .thenThrow(new IllegalStateException("El pago fue rechazado"));
        ResponseEntity<?> respuesta = ticketController.emitir(new TicketEntity());
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    public void testEmitir_ReservaRechazada() {
        when(ticketService.emitirTicket(any(TicketEntity.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Reserva vencida"));
        ResponseEntity<?> respuesta = ticketController.emitir(new TicketEntity());
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    public void testEmitir_LugarNoDisponible() {
        when(ticketService.emitirTicket(any(TicketEntity.class)))
                .thenThrow(new ResourceAccessException("Lugar no responde"));
        ResponseEntity<?> respuesta = ticketController.emitir(new TicketEntity());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
    }

    @Test
    public void testObtener_TicketExistente() {
        TicketEntity ticket = new TicketEntity();
        ticket.setId(10L);
        when(ticketService.obtenerTicket(10L)).thenReturn(ticket);
        ResponseEntity<TicketEntity> respuesta = ticketController.obtener(10L);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(ticket, respuesta.getBody());
    }

    @Test
    public void testObtener_TicketInexistente() {
        ResponseEntity<TicketEntity> respuesta = ticketController.obtener(10L);
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    public void testDevolucion_Rechazada() {
        when(ticketService.solicitarDevolucion(10L, "cliente1"))
                .thenThrow(new RuntimeException("Fuera de plazo"));
        ResponseEntity<?> respuesta = ticketController.solicitarDevolucion(10L, "cliente1");
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    public void testRevision_Aprobada() {
        TicketEntity ticket = new TicketEntity();
        ticket.setEstadoDevolucion("APROBADA_PENDIENTE_REINTEGRO");
        when(ticketService.revisarDevolucion(10L, "admin1", "APROBAR"))
                .thenReturn(ticket);
        ResponseEntity<?> respuesta = ticketController.revisarDevolucion(10L, "admin1", "APROBAR");
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(ticket, respuesta.getBody());
    }
}
