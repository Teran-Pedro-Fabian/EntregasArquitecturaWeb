import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class    EscaneoQrServiceTest {
    private static final String QR = "TICKETYA-QR-PRUEBA";
    private static final String EVENTO = "evento-1";
    private static final String URL = "http://localhost:8082/evento/" + EVENTO;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private TicketService ticketService;

    private TicketEntity ticket() {
        TicketEntity t = new TicketEntity();
        t.setId(24L);
        t.setQrCode(QR);
        t.setIdEvento(EVENTO);
        t.setEstadoDevolucion("SIN_SOLICITUD");
        t.setUsado(false);
        return t;
    }

    @Test
    void qrValidoMarcaUsado() {
        TicketEntity t = ticket();
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));
        when(restTemplate.getForObject(URL, Map.class))
                .thenReturn(Map.of("id", EVENTO, "estado", "PUBLICADO"));
        when(ticketRepository.saveAndFlush(t)).thenReturn(t);

        TicketEntity resultado = ticketService.marcarEntradaUtilizada(QR, EVENTO);

        assertTrue(resultado.isUsado());
        verify(ticketRepository).saveAndFlush(t);
    }

    @Test
    void qrDeEventoAgotadoPuedeIngresar() {
        TicketEntity t = ticket();
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));
        when(restTemplate.getForObject(URL, Map.class))
                .thenReturn(Map.of("id", EVENTO, "estado", "AGOTADO"));
        when(ticketRepository.saveAndFlush(t)).thenReturn(t);

        assertTrue(ticketService.marcarEntradaUtilizada(QR, EVENTO).isUsado());
    }

    @Test
    void noPermiteReutilizarElQr() {
        TicketEntity t = ticket();
        t.setUsado(true);
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        assertEquals("La entrada ya fue utilizada", error.getMessage());
        verify(ticketRepository, never()).saveAndFlush(any());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void noPermiteQrDeOtroEvento() {
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(ticket()));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, "evento-2"));
        assertEquals("El QR no pertenece a este evento", error.getMessage());
        verifyNoInteractions(restTemplate);
        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    void noPermiteEntradaDevuelta() {
        TicketEntity t = ticket();
        t.setEstadoDevolucion("DEVUELTA");
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        verifyNoInteractions(restTemplate);
        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    void noPermiteDevolucionPendiente() {
        TicketEntity t = ticket();
        t.setEstadoDevolucion("PENDIENTE");
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void noPermiteEventoCancelado() {
        TicketEntity t = ticket();
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(t));
        when(restTemplate.getForObject(URL, Map.class))
                .thenReturn(Map.of("id", EVENTO, "estado", "CANCELADO"));

        assertThrows(IllegalStateException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    void noPermiteQrInexistente() {
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void validaParametrosVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.marcarEntradaUtilizada("  ", EVENTO));
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, ""));
        verifyNoInteractions(ticketRepository, restTemplate);
    }

    @Test
    void siEventoNoRespondeNoMarcaUsado() {
        when(ticketRepository.findByQrCodeParaRevision(QR)).thenReturn(Optional.of(ticket()));
        when(restTemplate.getForObject(URL, Map.class))
                .thenThrow(new ResourceAccessException("Servicio caido"));
        assertThrows(ResourceAccessException.class,
                () -> ticketService.marcarEntradaUtilizada(QR, EVENTO));
        verify(ticketRepository, never()).saveAndFlush(any());
    }
}
