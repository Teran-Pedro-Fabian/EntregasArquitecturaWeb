import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.NotificacionRepository;
import com.microserviciocliente.servise.CorreoNotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CorreoNotificacionServiceTest {
    @Mock NotificacionRepository repo;
    @Mock ClienteRepository clienteRepository;
    @Mock JavaMailSender mailSender;
    @InjectMocks CorreoNotificacionService service;

    @BeforeEach void configurar() {
        ReflectionTestUtils.setField(service, "habilitado", true);
        ReflectionTestUtils.setField(service, "remitente", "notificaciones@ticketya.local");
    }

    private NotificacionEntity aviso() {
        NotificacionEntity n = new NotificacionEntity();
        n.setIdCliente("cliente-1"); n.setMensaje("Compra confirmada");
        n.setTipo("COMPRA"); return n;
    }
    @Test void enviarMarcaEstadoCuandoSmtpResponde() {
        NotificacionEntity n = aviso();
        ClienteEntity c = new ClienteEntity(); c.setEmail("cliente@prueba.com");
        when(repo.findTop50ByEstadoEmailInAndIntentosEmailLessThanOrderByFechaCreacionAsc(anyList(), eq(5)))
                .thenReturn(List.of(n));
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(c));
        service.enviarPendientes();
        verify(mailSender).send(any(SimpleMailMessage.class));
        assertEquals("ENVIADO", n.getEstadoEmail());
    }
    @Test void clienteSinEmailConservaAvisoEnBandeja() {
        NotificacionEntity n = aviso();
        when(repo.findTop50ByEstadoEmailInAndIntentosEmailLessThanOrderByFechaCreacionAsc(anyList(), eq(5)))
                .thenReturn(List.of(n));
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(new ClienteEntity()));
        service.enviarPendientes();
        assertEquals("SIN_EMAIL", n.getEstadoEmail());
        verifyNoInteractions(mailSender);
    }
    @Test void falloSmtpDejaPendienteParaReintento() {
        NotificacionEntity n = aviso();
        ClienteEntity c = new ClienteEntity(); c.setEmail("cliente@prueba.com");
        when(repo.findTop50ByEstadoEmailInAndIntentosEmailLessThanOrderByFechaCreacionAsc(anyList(), eq(5)))
                .thenReturn(List.of(n));
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(c));
        doThrow(new IllegalStateException("SMTP caido")).when(mailSender).send(any(SimpleMailMessage.class));
        service.enviarPendientes();
        assertEquals("ERROR", n.getEstadoEmail());
        assertEquals(1, n.getIntentosEmail());
    }
}
