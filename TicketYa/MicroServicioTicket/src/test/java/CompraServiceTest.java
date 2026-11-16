import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.ColaResponse;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.dto.CompraRequest;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {
    @Mock private CompraRepository compraRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private PagoMockService pagoMockService;
    @Mock private BloqueoVentaEventoService bloqueoVentaEventoService;
    @Mock private ColaService colaService;
    @Mock private EntityManager entityManager;
    @InjectMocks private CompraService compraService;

    private Query queryMock;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(compraService, "maxEntradas", 4);
    }

    private CompraRequest numerada(List<Long> asientos) {
        CompraRequest r = new CompraRequest();
        r.setIdCliente("cliente-1");
        r.setIdEvento("evento-1");
        r.setIdSector(5L);
        r.setIdsAsientos(asientos);
        r.setMedioPago("TARJETA");
        return r;
    }

    @Test
    void rechazarAsientosRepetidosEnLaMismaCompra() {
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(numerada(List.of(1L, 1L))));
    }

    @Test
    void rechazarMasDeCuatroEntradas() {
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(numerada(List.of(1L, 2L, 3L, 4L, 5L))));
    }

    @Test
    void rechazarMedioDePagoDesconocido() {
        CompraRequest r = numerada(List.of(1L));
        r.setMedioPago("EFECTIVO");
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(r));
    }

    @Test
    void rechazarGeneralConCantidadMayorAlLimite() {
        CompraGeneralRequest r = new CompraGeneralRequest();
        r.setIdCliente("cliente-1");
        r.setIdEvento("evento-1");
        r.setIdSector(5L);
        r.setIdReserva(10L);
        r.setCantidad(5);
        r.setMedioPago("TARJETA");
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprarGeneral(r));
    }

    @Test
    void rechazarCompraDeEventoAltaDemandaSinFilaVirtual() {
        queryMock = Mockito.mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(0L);
        when(bloqueoVentaEventoService.estaBloqueado(anyString())).thenReturn(false);
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(eq("http://localhost:8082/evento/evento-1"), eq(Map.class)))
                .thenReturn(Map.of("estado", "PUBLICADO", "demanda", "ALTA"));
        when(colaService.consultarPosicion("evento-1", "cliente-1"))
                .thenThrow(new IllegalArgumentException("El cliente no esta en la fila del evento"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(numerada(List.of(1L))));
        assertTrue(error.getMessage().contains("no esta en la fila"));
    }

    @Test
    void rechazarCompraDeEventoAltaDemandaCuandoNoHabilitado() {
        queryMock = Mockito.mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(0L);
        when(bloqueoVentaEventoService.estaBloqueado(anyString())).thenReturn(false);
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(eq("http://localhost:8082/evento/evento-1"), eq(Map.class)))
                .thenReturn(Map.of("estado", "PUBLICADO", "demanda", "ALTA"));
        when(colaService.consultarPosicion("evento-1", "cliente-1"))
                .thenReturn(new ColaResponse("evento-1", "cliente-1", 2, 2, false));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> compraService.comprar(numerada(List.of(1L))));
        assertTrue(error.getMessage().contains("fila virtual"));
    }

    @Test
    void rechazarCompraGeneralDeEventoAltaDemandaSinFilaVirtual() {
        CompraGeneralRequest r = new CompraGeneralRequest();
        r.setIdCliente("cliente-1");
        r.setIdEvento("evento-1");
        r.setIdSector(5L);
        r.setIdReserva(10L);
        r.setCantidad(1);
        r.setMedioPago("TARJETA");
        queryMock = Mockito.mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(0L);
        when(bloqueoVentaEventoService.estaBloqueado(anyString())).thenReturn(false);
        when(compraRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(restTemplate.getForObject(eq("http://localhost:8082/evento/evento-1"), eq(Map.class)))
                .thenReturn(Map.of("estado", "PUBLICADO", "demanda", "ALTA"));
        when(colaService.consultarPosicion("evento-1", "cliente-1"))
                .thenThrow(new IllegalArgumentException("El cliente no esta en la fila del evento"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> compraService.comprarGeneral(r));
        assertTrue(error.getMessage().contains("no esta en la fila"));
    }
}
