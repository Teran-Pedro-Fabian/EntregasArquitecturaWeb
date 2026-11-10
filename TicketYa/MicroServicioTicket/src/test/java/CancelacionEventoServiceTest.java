import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.AvisoCancelacionService;
import com.microservicioticket.service.EntregaAvisoService;
import com.microservicioticket.service.CancelacionEventoService;
import com.microservicioticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelacionEventoServiceTest {
    @Mock private TicketRepository ticketRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private TicketService ticketService;
    @Mock private AvisoCancelacionService avisoCancelacionService;
    @Mock private EntregaAvisoService entregaAvisoService;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private CancelacionEventoService service;

    private TicketEntity ticket(Long id, String cliente, String estado){
        TicketEntity t=new TicketEntity();t.setId(id);t.setIdCliente(cliente);t.setEstadoDevolucion(estado);
        return t;
    }

    @Test
    void noReintegrarSiEventoNoEstaCancelado(){
        when(restTemplate.getForObject(contains("/evento/"),eq(Map.class)))
                .thenReturn(Map.of("estado","PUBLICADO"));
        assertThrows(IllegalStateException.class, () -> service.procesar("evento-1","admin"));
        verifyNoInteractions(ticketRepository,ticketService);
    }

    @Test
    void consultarResumenDistinguePendientesDeDevueltos(){
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1"))
                .thenReturn(List.of(ticket(1L,"cliente-1","DEVUELTA"),ticket(2L,"cliente-2","SIN_SOLICITUD")));
        when(avisoCancelacionService.contarPorEvento("evento-1")).thenReturn(2L);
        Map<String,Object> r=service.consultar("evento-1");
        assertEquals(2,r.get("totalTickets"));
        assertEquals(1,r.get("devueltos"));
        assertEquals(1,r.get("pendientes"));
        assertEquals(2L,r.get("avisosRegistrados"));
    }

    @Test
    void cadaCompradorRecibeSoloUnAvisoPorEvento(){
        when(restTemplate.getForObject(contains("/evento/"),eq(Map.class)))
                .thenReturn(Map.of("estado","CANCELADO"));
        // Las entradas ya estan devueltas, solo hay que conservar el aviso unico.
        List<TicketEntity> tickets=List.of(ticket(1L,"cliente-1","DEVUELTA"),ticket(2L,"cliente-1","DEVUELTA"));
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1")).thenReturn(tickets);
        service.procesar("evento-1","admin");
        verify(avisoCancelacionService,times(1)).registrar("evento-1","cliente-1");
        verify(entregaAvisoService,times(1)).entregar("evento-1","cliente-1");
        verifyNoInteractions(ticketService);
    }

    @Test
    void cancelacionProcesaCadaTicketVendido(){
        when(restTemplate.getForObject(contains("/evento/"),eq(Map.class)))
                .thenReturn(Map.of("estado","CANCELADO"));
        when(ticketRepository.findByIdEventoOrderByIdAsc("evento-1"))
                .thenReturn(List.of(ticket(10L,"cliente-1","SIN_SOLICITUD")));
        service.procesar("evento-1","admin");
        verify(ticketService).marcarDevolucionPorCancelacion(10L,"admin");
        verify(ticketService).procesarReintegro(10L);
        verify(ticketService).finalizarDevolucion(10L);
        verify(avisoCancelacionService).registrar("evento-1","cliente-1");
        verify(entregaAvisoService).entregar("evento-1","cliente-1");
    }
}
