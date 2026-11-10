import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.BloqueoVentaEventoService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraReglasTest {
    @Mock private EntityManager entityManager;
    @Mock private Query query;
    @Mock private CompraRepository compraRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PagoMockService pagoMockService;
    @Mock private BloqueoVentaEventoService bloqueoVentaEventoService;
    @InjectMocks private CompraService service;

    @BeforeEach void preparar(){ReflectionTestUtils.setField(service,"maxEntradas",4);}

    private CompraRequest compra(){
        CompraRequest r=new CompraRequest();
        r.setIdCliente("cliente-1");r.setIdEvento("evento-1");r.setIdSector(5L);
        r.setIdsAsientos(List.of(1L));r.setMedioPago("TARJETA");
        return r;
    }
    private void bloqueoCliente(){
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("clave"),any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);
    }

    @Test
    void quintoTicketParaUnClienteEsRechazadoAntesDeCobrar(){
        bloqueoCliente();
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(contains("8082"),eq(Map.class))).thenReturn(Map.of("estado","PUBLICADO"));
        when(ticketRepository.contarActivos("cliente-1","evento-1")).thenReturn(4L);
        assertThrows(IllegalStateException.class, () -> service.comprar(compra()));
        verifyNoInteractions(pagoMockService);
    }

    @Test
    void pagoRechazadoLiberaReservaNumerada(){
        bloqueoCliente();
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(contains("8082"),eq(Map.class))).thenReturn(Map.of("estado","PUBLICADO"));
        when(restTemplate.getForObject(contains("/precio"),eq(Map.class))).thenReturn(Map.of("precio",15000.0));
        // El mock no devuelve una respuesta de validacion pero no produce error.
        when(pagoMockService.procesarPago("cliente-1",15000.0)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> service.comprar(compra()));
        verify(restTemplate).exchange(contains("/lugares/reservas/1"),eq(HttpMethod.DELETE),isNull(),eq(String.class));
        verify(compraRepository,never()).saveAndFlush(any());
        verify(ticketRepository,never()).saveAndFlush(any());
    }

    @Test
    void eventoCanceladoNoPermitePagar(){
        bloqueoCliente();
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(bloqueoVentaEventoService.estaBloqueado("evento-1")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.comprar(compra()));
        verifyNoInteractions(pagoMockService);
    }

    @Test
    void reintentoDeCompraConfirmadaNoCobraOtraVez(){
        bloqueoCliente();
        CompraEntity c=new CompraEntity();
        c.setId(33L);c.setIdEvento("evento-1");c.setIdCliente("cliente-1");
        c.setEstado("CONFIRMADA");c.setMedioPago("TARJETA");c.setTotal(15000.0);
        when(compraRepository.findById(33L)).thenReturn(Optional.of(c));
        TicketEntity t=new TicketEntity();t.setId(100L);t.setQrCode("QR-original");
        when(ticketRepository.findByIdCompra(33L)).thenReturn(List.of(t));
        CompraResponse respuesta=service.reintentarConfirmacion(33L);
        assertEquals("CONFIRMADA",respuesta.getEstado());
        assertEquals("QR-original",respuesta.getTickets().get(0).getQrCode());
        verifyNoInteractions(pagoMockService,restTemplate);
        verify(ticketRepository,never()).saveAndFlush(any());
    }

    @Test
    void siLugarNoRespondeLaCompraQuedaPendienteSinNuevoPago(){
        bloqueoCliente();
        CompraEntity c=new CompraEntity();
        c.setId(33L);c.setIdEvento("evento-1");c.setIdCliente("cliente-1");
        c.setEstado("PENDIENTE_CONFIRMACION");c.setCantidadEntradas(1);
        c.setMedioPago("TARJETA");c.setTotal(15000.0);
        when(compraRepository.findById(33L)).thenReturn(Optional.of(c));
        TicketEntity t=new TicketEntity();t.setId(100L);t.setIdAsiento(1L);t.setQrCode("QR-original");
        when(ticketRepository.findByIdCompra(33L)).thenReturn(List.of(t));
        when(restTemplate.postForEntity(contains("/asiento/confirmar"),anyMap(),eq(String.class)))
                .thenThrow(new ResourceAccessException("Conexion interrumpida"));
        CompraResponse respuesta=service.reintentarConfirmacion(33L);
        assertEquals("PENDIENTE_CONFIRMACION",respuesta.getEstado());
        assertEquals("QR-original",respuesta.getTickets().get(0).getQrCode());
        verifyNoInteractions(pagoMockService);
        verify(ticketRepository,never()).saveAndFlush(any());
    }
}
