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
import org.springframework.web.client.ResourceAccessException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EscaneoQrControllerTest {
    @Mock
    private TicketService ticketService;
    @InjectMocks
    private TicketController controller;

    @Test
    void ingresoCorrecto() {
        TicketEntity ticket = new TicketEntity();
        ticket.setUsado(true);
        when(ticketService.marcarEntradaUtilizada("qr", "evento")).thenReturn(ticket);

        ResponseEntity<?> resultado = controller.escanearQrcode("qr", "evento");
        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(ticket, resultado.getBody());
    }

    @Test
    void entradaReutilizadaDevuelve409() {
        when(ticketService.marcarEntradaUtilizada("qr", "evento"))
                .thenThrow(new IllegalStateException("La entrada ya fue utilizada"));
        ResponseEntity<?> resultado = controller.escanearQrcode("qr", "evento");
        assertEquals(HttpStatus.CONFLICT, resultado.getStatusCode());
    }

    @Test
    void qrInexistenteDevuelve404() {
        when(ticketService.marcarEntradaUtilizada("qr", "evento"))
                .thenThrow(new NoSuchElementException("Ticket no encontrado"));
        assertEquals(HttpStatus.NOT_FOUND,
                controller.escanearQrcode("qr", "evento").getStatusCode());
    }

    @Test
    void datosInvalidosDevuelve400() {
        when(ticketService.marcarEntradaUtilizada("", "evento"))
                .thenThrow(new IllegalArgumentException("Debe indicar el QR y el evento"));
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.escanearQrcode("", "evento").getStatusCode());
    }

    @Test
    void sinEventoDevuelve503() {
        when(ticketService.marcarEntradaUtilizada("qr", "evento"))
                .thenThrow(new ResourceAccessException("sin conexion"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                controller.escanearQrcode("qr", "evento").getStatusCode());
    }
}
