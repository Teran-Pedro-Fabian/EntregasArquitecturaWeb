import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.BloqueoVentaEventoService;
import com.microservicioticket.service.ColaService;
import com.microservicioticket.service.CompraService;
import com.microservicioticket.service.PagoMockService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionCompraTest {
    @Mock private EntityManager entityManager;
    @Mock private Query query;
    @Mock private CompraRepository compraRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PagoMockService pagoMockService;
    @Mock private BloqueoVentaEventoService bloqueoVentaEventoService;
    @Mock private ColaService colaService;
    @InjectMocks private CompraService compraService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(compraService, "maxEntradas", 4);
    }

    private void prepararBloqueo() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("clave"), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);
    }

    @Test
    void confirmacionInciertaGeneralSeRecuperaSinCobrarNiGenerarOtrosTickets() {
        prepararBloqueo();
        CompraEntity compra = new CompraEntity();
        compra.setId(33L);
        compra.setEstado("PENDIENTE_CONFIRMACION");
        compra.setIdCliente("cliente-1");
        compra.setIdEvento("evento-1");
        compra.setIdSector(5L);
        compra.setIdReservaGeneral(10L);
        compra.setCantidadEntradas(1);
        compra.setTotal(10000.0);
        compra.setMedioPago("TARJETA");
        when(compraRepository.findById(33L)).thenReturn(Optional.of(compra));

        TicketEntity ticket = new TicketEntity();
        ticket.setId(150L);
        ticket.setQrCode("QR-original");
        when(ticketRepository.findByIdCompra(33L)).thenReturn(List.of(ticket));
        when(restTemplate.postForEntity(contains("/generales/confirmar"), anyMap(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Lugar sin respuesta"))
                .thenReturn(ResponseEntity.ok("confirmado"));

        CompraResponse primero = compraService.reintentarConfirmacion(33L);
        assertEquals("PENDIENTE_CONFIRMACION", primero.getEstado());
        CompraResponse segundo = compraService.reintentarConfirmacion(33L);
        assertEquals("CONFIRMADA", segundo.getEstado());
        assertEquals("QR-original", segundo.getTickets().get(0).getQrCode());
        verify(restTemplate, times(2)).postForEntity(contains("/generales/confirmar"),
                argThat(body -> body instanceof Map<?, ?> mapa
                        && Long.valueOf(33L).equals(mapa.get("idCompra"))
                        && List.of(150L).equals(mapa.get("idsTickets"))), eq(String.class));
        verifyNoInteractions(pagoMockService);
        verify(ticketRepository, never()).saveAndFlush(any());
        verify(compraRepository, times(1)).saveAndFlush(compra);
    }

    @Test
    void pagoGeneralRechazadoCompensaReservaSinCrearCompra() {
        prepararBloqueo();
        CompraGeneralRequest request = new CompraGeneralRequest();
        request.setIdCliente("cliente-1");
        request.setIdEvento("evento-1");
        request.setIdSector(5L);
        request.setIdReserva(10L);
        request.setCantidad(1);
        request.setMedioPago("TARJETA");
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(contains("/evento/"), eq(Map.class)))
                .thenReturn(Map.of("estado", "PUBLICADO", "demanda", "BAJA"));
        when(restTemplate.getForObject(contains("/precio"), eq(Map.class)))
                .thenReturn(Map.of("precio", 10000.0));
        when(pagoMockService.procesarPago("cliente-1", 10000.0)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> compraService.comprarGeneral(request));
        verify(restTemplate).exchange(contains("/generales/reservas/10"),
                eq(HttpMethod.DELETE), isNull(), eq(String.class));
        verify(compraRepository, never()).saveAndFlush(any());
        verify(ticketRepository, never()).saveAndFlush(any());
    }
}
