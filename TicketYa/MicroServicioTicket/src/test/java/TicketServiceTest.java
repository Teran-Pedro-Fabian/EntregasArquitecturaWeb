import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.PagoMockService;
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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private static final String URL_VALIDAR =
            "http://localhost:8083/lugares/interno/reservas/validar";
    private static final String URL_CONFIRMAR =
            "http://localhost:8083/lugares/asiento/confirmar";
    private static final String URL_EVENTO =
            "http://localhost:8082/evento/evento-prueba";
    private static final String URL_LIBERAR =
            "http://localhost:8083/lugares/interno/asientos/1/liberar-venta?idTicket=10";

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PagoMockService pagoMockService;

    @InjectMocks
    private TicketService ticketService;

    // Crear un ticket valido para reutilizar en las pruebas
    private TicketEntity ticketDePrueba() {
        TicketEntity ticket = new TicketEntity();
        ticket.setId(10L);
        ticket.setIdAsiento(1L);
        ticket.setIdSector(2L);
        ticket.setIdCliente("cliente1");
        ticket.setIdEvento("evento-prueba");
        ticket.setPrecio(15000.0);
        ticket.setEstadoDevolucion("SIN_SOLICITUD");
        return ticket;
    }

    // Simular un evento en el futuro
    private void simularEvento(LocalDateTime fecha) {
        when(restTemplate.getForObject(eq(URL_EVENTO), eq(Map.class)))
                .thenReturn(Map.of("fechaHora", fecha.toString()));
    }

    // Simular la generacion del ID que hace PostgreSQL
    private void simularGuardadoDeEmision() {
        when(ticketRepository.saveAndFlush(any(TicketEntity.class)))
                .thenAnswer(invocacion -> {
                    TicketEntity guardado = invocacion.getArgument(0);
                    guardado.setId(10L);
                    return guardado;
                });
    }

    @Test
    public void testEmision_DatosIncompletos() {
        // 1) Verificar que no se acepten datos obligatorios vacios
        TicketEntity ticket = ticketDePrueba();
        ticket.setIdAsiento(null);
        assertThrows(IllegalArgumentException.class, () -> ticketService.emitirTicket(ticket));
        ticket.setIdAsiento(1L);
        ticket.setIdCliente(" ");
        assertThrows(IllegalArgumentException.class, () -> ticketService.emitirTicket(ticket));
        ticket.setIdCliente("cliente1");
        ticket.setIdEvento(null);
        assertThrows(IllegalArgumentException.class, () -> ticketService.emitirTicket(ticket));
        ticket.setIdEvento("evento-prueba");
        ticket.setPrecio(0.0);
        assertThrows(IllegalArgumentException.class, () -> ticketService.emitirTicket(ticket));
        verifyNoInteractions(ticketRepository, restTemplate, pagoMockService);
    }

    @Test
    public void testEmision_ReservaRechazada() {
        // 1) Lugar informa que la reserva no corresponde
        when(restTemplate.postForEntity(eq(URL_VALIDAR), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Reserva invalida"));
        assertThrows(HttpClientErrorException.class,
                () -> ticketService.emitirTicket(ticketDePrueba()));
        // 2) No cobrar ni guardar
        verifyNoInteractions(pagoMockService, ticketRepository);
    }

    @Test
    public void testEmision_LugarNoDisponible() {
        when(restTemplate.postForEntity(eq(URL_VALIDAR), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Lugar no disponible"));
        assertThrows(ResourceAccessException.class,
                () -> ticketService.emitirTicket(ticketDePrueba()));
        verifyNoInteractions(pagoMockService, ticketRepository);
    }

    @Test
    public void testEmision_PagoRechazado() {
        when(pagoMockService.procesarPago("cliente1", 15000.0)).thenReturn(false);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ticketService.emitirTicket(ticketDePrueba()));
        assertEquals("El pago fue rechazado", error.getMessage());
        verify(ticketRepository, never()).saveAndFlush(any(TicketEntity.class));
        verify(restTemplate, never()).postForEntity(eq(URL_CONFIRMAR), any(), eq(String.class));
    }

    @Test
    public void testEmision_ErrorEnPago() {
        when(pagoMockService.procesarPago("cliente1", 15000.0))
                .thenThrow(new RuntimeException("Error pasarela"));
        assertThrows(RuntimeException.class,
                () -> ticketService.emitirTicket(ticketDePrueba()));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    public void testEmision_PagoAprobado() {
        when(pagoMockService.procesarPago("cliente1", 15000.0)).thenReturn(true);
        simularGuardadoDeEmision();

        TicketEntity resultado = ticketService.emitirTicket(ticketDePrueba());

        assertEquals(10L, resultado.getId());
        assertTrue(resultado.getQrCode().startsWith("TICKETYA-"));
        assertEquals("SIN_SOLICITUD", resultado.getEstadoDevolucion());
        assertFalse(resultado.isUsado());
        verify(ticketRepository).saveAndFlush(any(TicketEntity.class));
        verify(restTemplate).postForEntity(eq(URL_VALIDAR), any(), eq(String.class));
        verify(restTemplate).postForEntity(eq(URL_CONFIRMAR), any(), eq(String.class));
    }

    @Test
    public void testEmision_ErrorAlGuardar() {
        when(pagoMockService.procesarPago("cliente1", 15000.0)).thenReturn(true);
        when(ticketRepository.saveAndFlush(any(TicketEntity.class)))
                .thenThrow(new RuntimeException("Error PostgreSQL"));

        assertThrows(RuntimeException.class,
                () -> ticketService.emitirTicket(ticketDePrueba()));
        verify(restTemplate, never()).postForEntity(eq(URL_CONFIRMAR), any(), eq(String.class));
    }


    @Test
    public void testEmision_ConfirmacionRechazada() {
        // 1) Simular el pago aprobado
        when(pagoMockService.procesarPago("cliente1", 15000.0))
                .thenReturn(true);
        // 2) Simular el guardado del ticket
        simularGuardadoDeEmision();
        // 3) Simular una reserva valida
        doReturn(ResponseEntity.ok("Reserva valida"))
                .when(restTemplate)
                .postForEntity(eq(URL_VALIDAR), any(), eq(String.class));
        // 4) Simular que Lugar rechaza la confirmacion
        doThrow(new HttpClientErrorException(
                HttpStatus.CONFLICT, "Reserva vencida"))
                .when(restTemplate)
                .postForEntity(eq(URL_CONFIRMAR), any(), eq(String.class));
        // 5) Verificar que se informe el error
        assertThrows(
                HttpClientErrorException.class,
                () -> ticketService.emitirTicket(ticketDePrueba())
        );
        // 6) Verificar que se intento guardar y confirmar
        verify(ticketRepository)
                .saveAndFlush(any(TicketEntity.class));
        verify(restTemplate).postForEntity(
                eq(URL_CONFIRMAR), any(), eq(String.class)
        );
    }

    @Test
    public void testObtenerTicket_Encontrado() {
        TicketEntity ticket = ticketDePrueba();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        assertSame(ticket, ticketService.obtenerTicket(10L));
    }

    @Test
    public void testObtenerTicket_NoEncontrado() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());
        assertNull(ticketService.obtenerTicket(10L));
    }

    @Test
    public void testDevolucion_TicketNoEncontrado() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> ticketService.solicitarDevolucion(10L, "cliente1"));
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testDevolucion_ClienteIncorrecto() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticketDePrueba()));
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> ticketService.solicitarDevolucion(10L, "otroCliente"));
        assertEquals("El ticket no pertenece al cliente", error.getMessage());
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testDevolucion_SolicitudDuplicada() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("PENDIENTE");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        assertThrows(RuntimeException.class,
                () -> ticketService.solicitarDevolucion(10L, "cliente1"));
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testDevolucion_EventoSinFecha() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticketDePrueba()));
        when(restTemplate.getForObject(eq(URL_EVENTO), eq(Map.class)))
                .thenReturn(Map.of());
        assertThrows(RuntimeException.class,
                () -> ticketService.solicitarDevolucion(10L, "cliente1"));
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testDevolucion_RechazadaPorTiempo() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticketDePrueba()));
        simularEvento(LocalDateTime.now().plusHours(24));
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> ticketService.solicitarDevolucion(10L, "cliente1"));
        assertEquals("No se puede solicitar la devolucion: faltan menos de 72 horas",
                error.getMessage());
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testDevolucion_Valida() {
        TicketEntity ticket = ticketDePrueba();
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        simularEvento(LocalDateTime.now().plusDays(10));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resultado = ticketService.solicitarDevolucion(10L, "cliente1");
        assertEquals("PENDIENTE", resultado.getEstadoDevolucion());
        verify(ticketRepository).save(ticket);
    }

    @Test
    public void testRevision_AdministradorInvalido() {
        assertThrows(RuntimeException.class,
                () -> ticketService.revisarDevolucion(10L, " ", "APROBAR"));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    public void testRevision_DecisionInvalida() {
        assertThrows(RuntimeException.class,
                () -> ticketService.revisarDevolucion(10L, "admin1", "OTRA"));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    public void testRevision_SinSolicitudPendiente() {
        when(ticketRepository.findByIdParaRevision(10L))
                .thenReturn(Optional.of(ticketDePrueba()));
        assertThrows(RuntimeException.class,
                () -> ticketService.revisarDevolucion(10L, "admin1", "APROBAR"));
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testRevision_Aprobar() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("PENDIENTE");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resultado = ticketService.revisarDevolucion(10L, "admin1", "APROBAR");
        assertEquals("APROBADA_PENDIENTE_REINTEGRO", resultado.getEstadoDevolucion());
        assertEquals("admin1", resultado.getIdAdministradorRevision());
        assertNotNull(resultado.getFechaRevision());
    }

    @Test
    public void testRevision_Rechazar() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("PENDIENTE");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resultado = ticketService.revisarDevolucion(10L, "admin1", "RECHAZAR");
        assertEquals("RECHAZADA", resultado.getEstadoDevolucion());
        assertEquals("admin1", resultado.getIdAdministradorRevision());
    }

    @Test
    public void testReintegro_SinAprobacion() {
        when(ticketRepository.findByIdParaRevision(10L))
                .thenReturn(Optional.of(ticketDePrueba()));
        assertThrows(RuntimeException.class, () -> ticketService.procesarReintegro(10L));
        verifyNoInteractions(pagoMockService);
    }

    @Test
    public void testReintegro_YaProcesado() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("REINTEGRADA_PENDIENTE_LIBERACION");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        assertSame(ticket, ticketService.procesarReintegro(10L));
        verifyNoInteractions(pagoMockService);
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testReintegro_RechazadoPorPasarela() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("APROBADA_PENDIENTE_REINTEGRO");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(pagoMockService.solicitarReintegro(10L, 15000.0)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> ticketService.procesarReintegro(10L));
        assertEquals("APROBADA_PENDIENTE_REINTEGRO", ticket.getEstadoDevolucion());
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testReintegro_Aprobado() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("APROBADA_PENDIENTE_REINTEGRO");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(pagoMockService.solicitarReintegro(10L, 15000.0)).thenReturn(true);
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resultado = ticketService.procesarReintegro(10L);
        assertEquals("REINTEGRADA_PENDIENTE_LIBERACION", resultado.getEstadoDevolucion());
        verify(ticketRepository).save(ticket);
    }

    @Test
    public void testFinalizacion_YaDevuelta() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("DEVUELTA");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        assertSame(ticket, ticketService.finalizarDevolucion(10L));
        verifyNoInteractions(restTemplate);
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testFinalizacion_SinReintegro() {
        when(ticketRepository.findByIdParaRevision(10L))
                .thenReturn(Optional.of(ticketDePrueba()));
        assertThrows(RuntimeException.class, () -> ticketService.finalizarDevolucion(10L));
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testFinalizacion_SinAsiento() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("REINTEGRADA_PENDIENTE_LIBERACION");
        ticket.setIdAsiento(null);
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        assertThrows(RuntimeException.class, () -> ticketService.finalizarDevolucion(10L));
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testFinalizacion_LiberacionFallida() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("REINTEGRADA_PENDIENTE_LIBERACION");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(restTemplate.postForEntity(eq(URL_LIBERAR), isNull(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Asiento no liberado"));

        assertThrows(HttpClientErrorException.class,
                () -> ticketService.finalizarDevolucion(10L));
        assertEquals("REINTEGRADA_PENDIENTE_LIBERACION", ticket.getEstadoDevolucion());
        verify(ticketRepository, never()).save(any(TicketEntity.class));
    }

    @Test
    public void testFinalizacion_Correcta() {
        TicketEntity ticket = ticketDePrueba();
        ticket.setEstadoDevolucion("REINTEGRADA_PENDIENTE_LIBERACION");
        when(ticketRepository.findByIdParaRevision(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketEntity resultado = ticketService.finalizarDevolucion(10L);
        assertEquals("DEVUELTA", resultado.getEstadoDevolucion());
        verify(restTemplate).postForEntity(eq(URL_LIBERAR), isNull(), eq(String.class));
        verify(ticketRepository).save(ticket);
    }
}
