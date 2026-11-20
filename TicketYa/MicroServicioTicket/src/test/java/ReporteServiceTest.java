import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.ReporteEventoDTO;
import com.microservicioticket.dto.ReporteSectorDTO;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.ReporteService;
import com.microservicioticket.service.ReporteService.ReporteSectorLugar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
    @Mock private TicketRepository ticketRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private RestTemplate restTemplate;

    private ReporteService service;
    private static final String EVENTO_URL = "http://localhost:8082/evento/evento-1";
    private static final String LUGAR_URL =
            "http://localhost:8083/lugares/interno/reportes/eventos/evento-1/sectores";

    @BeforeEach
    void preparar() {
        service = new ReporteService(ticketRepository, compraRepository, restTemplate,
                "http://localhost:8082", "http://localhost:8083");
    }

    private void configurarServicios() {
        when(restTemplate.getForObject(EVENTO_URL, Map.class)).thenReturn(
                Map.of("id", "evento-1", "nombre", "Festival", "organizador", "organizador-1"));
        when(restTemplate.getForObject(LUGAR_URL, ReporteSectorLugar[].class)).thenReturn(
                new ReporteSectorLugar[]{
                        new ReporteSectorLugar(1L, "Platea", "NUMERADO", 5),
                        new ReporteSectorLugar(2L, "Campo", "NO_NUMERADO", 100)
                });
    }

    private TicketEntity ticket(long id, long compra, String devolucion, double precio) {
        TicketEntity t = new TicketEntity();
        t.setId(id);
        t.setIdCompra(compra);
        t.setIdEvento("evento-1");
        t.setIdSector(1L);
        t.setEstadoDevolucion(devolucion);
        t.setPrecio(precio);
        return t;
    }

    private CompraEntity compra(long id, String estado) {
        CompraEntity c = new CompraEntity();
        c.setId(id);
        c.setEstado(estado);
        return c;
    }

    @Test
    void calculaVentasDevolucionesOcupacionYRecaudacion() {
        configurarServicios();
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1")).thenReturn(List.of(
                ticket(1, 10, "SIN_SOLICITUD", 100),
                ticket(2, 10, "DEVUELTA", 100),
                ticket(3, 10, "REINTEGRADA_PENDIENTE_LIBERACION", 100),
                ticket(4, 20, "SIN_SOLICITUD", 300)));
        when(compraRepository.findAllById(any())).thenReturn(List.of(
                compra(10, "CONFIRMADA"), compra(20, "PENDIENTE_CONFIRMACION")));

        ReporteEventoDTO reporte = service.reportePorEvento("evento-1", "organizador-1");
        ReporteSectorDTO platea = reporte.getSectores().get(0);
        assertEquals(3, platea.getEntradasVendidas());
        assertEquals(1, platea.getEntradasDevueltas());
        assertEquals(2, platea.getEntradasActivas());
        assertEquals(new BigDecimal("40.00"), platea.getOcupacionPorcentaje());
        assertEquals(new BigDecimal("300.00"), platea.getRecaudacionBruta());
        assertEquals(new BigDecimal("200.00"), platea.getReintegrosRealizados());
        assertEquals(new BigDecimal("100.00"), platea.getRecaudacionNeta());
        assertEquals(105, reporte.getCapacidadTotal());
        assertEquals(new BigDecimal("1.90"), reporte.getOcupacionPorcentaje());
        assertEquals(new BigDecimal("100.00"), reporte.getRecaudacionNeta());
    }

    @Test
    void organizadorAjenoNoPuedeVerElReporte() {
        when(restTemplate.getForObject(EVENTO_URL, Map.class)).thenReturn(
                Map.of("nombre", "Festival", "organizador", "organizador-1"));
        assertThrows(SecurityException.class, () -> service.reportePorEvento("evento-1", "otro"));
        verifyNoInteractions(ticketRepository, compraRepository);
        verify(restTemplate, never()).getForObject(eq(LUGAR_URL), eq(ReporteSectorLugar[].class));
    }

    @Test
    void eventoSinVentasDevuelveValoresEnCero() {
        configurarServicios();
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1")).thenReturn(List.of());
        ReporteEventoDTO reporte = service.reportePorEvento("evento-1", "organizador-1");
        assertEquals(0, reporte.getEntradasVendidas());
        assertEquals(0, reporte.getEntradasDevueltas());
        assertEquals(0, reporte.getEntradasActivas());
        assertEquals(new BigDecimal("0.00"), reporte.getRecaudacionNeta());
        verifyNoInteractions(compraRepository);
    }

    @Test
    void rechazaLosTicketsConSectorInexistente() {
        configurarServicios();
        TicketEntity sinSector = ticket(5, 10, "SIN_SOLICITUD", 100);
        sinSector.setIdSector(99L);
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1")).thenReturn(List.of(sinSector));
        when(compraRepository.findAllById(any())).thenReturn(List.of(compra(10, "CONFIRMADA")));
        assertThrows(IllegalStateException.class,
                () -> service.reportePorEvento("evento-1", "organizador-1"));
    }
}
