import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.dto.NotificacionPublicacionRequest;
import com.microserviciocliente.dto.NotificacionRequest;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.NotificacionRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import com.microserviciocliente.servise.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {
    @Mock NotificacionRepository repo;
    @Mock PreferenciasRepository preferenciasRepository;
    @InjectMocks NotificacionService service;

    private NotificacionCancelacionRequest datos() {
        NotificacionCancelacionRequest d = new NotificacionCancelacionRequest();
        d.setIdEvento("evento-1");
        d.setIdCliente("cliente-1");
        d.setMensaje("El evento fue cancelado");
        return d;
    }

    @Test
    void guardarNotificacionNueva() {
        service.recibirCancelacion(datos());
        verify(repo).saveAndFlush(argThat(n ->
                "CANCELACION".equals(n.getTipo()) && "evento".equals(n.getReferencia()) &&
                "evento-1".equals(n.getIdEvento()) && "cliente-1".equals(n.getIdCliente()) &&
                n.getFechaCreacion() != null));
    }

    @Test
    void noDuplicarCuandoTicketReintenta() {
        NotificacionEntity existente = new NotificacionEntity();
        when(repo.findByTipoAndReferenciaAndIdEventoAndIdCliente(
                "CANCELACION", "evento", "evento-1", "cliente-1"))
                .thenReturn(Optional.of(existente));
        assertSame(existente, service.recibirCancelacion(datos()));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void rechazarDatosInvalidos() {
        NotificacionCancelacionRequest d = datos();
        d.setIdCliente(" ");
        assertThrows(IllegalArgumentException.class, () -> service.recibirCancelacion(d));
        verifyNoInteractions(repo);
    }

    @Test
    void compraUsaIdCompraComoReferencia() {
        NotificacionRequest d = new NotificacionRequest();
        d.setIdCliente("cliente-1"); d.setIdEvento("evento-1");
        d.setReferencia("compra-45"); d.setMensaje("Compra confirmada");
        service.recibirCompra(d);
        verify(repo).saveAndFlush(argThat(n ->
                "COMPRA".equals(n.getTipo()) && "compra-45".equals(n.getReferencia())));
    }

    @Test
    void cambioPermiteOtraNotificacionEnElMismoEvento() {
        NotificacionRequest d = new NotificacionRequest();
        d.setIdCliente("cliente-1"); d.setIdEvento("evento-1");
        d.setReferencia("cambio-uuid-1"); d.setMensaje("Se cambio la fecha");
        service.recibirCambio(d);
        verify(repo).saveAndFlush(argThat(n ->
                "CAMBIO".equals(n.getTipo()) && "cambio-uuid-1".equals(n.getReferencia())));
    }

    @Test
    void artistaFavoritoSoloCoincideEnLaCiudadYSinDuplicados() {
        PreferenciasEntity a = new PreferenciasEntity();
        a.setIdCliente("cliente-1"); a.setIdParticipante("banda-1"); a.setCiudad("Tandil");
        PreferenciasEntity repetida = new PreferenciasEntity();
        repetida.setIdCliente("cliente-1"); repetida.setIdParticipante("banda-1"); repetida.setCiudad("tandil");
        PreferenciasEntity otraCiudad = new PreferenciasEntity();
        otraCiudad.setIdCliente("cliente-2"); otraCiudad.setIdParticipante("banda-1"); otraCiudad.setCiudad("Azul");
        when(preferenciasRepository.findByIdParticipanteIn(List.of("banda-1")))
                .thenReturn(List.of(a, repetida, otraCiudad));
        NotificacionPublicacionRequest datos = new NotificacionPublicacionRequest();
        datos.setIdEvento("evento-1"); datos.setNombreEvento("Recital");
        datos.setCiudad("Tandil"); datos.setIdLugar("5");
        datos.setIdsParticipantes(List.of("banda-1"));
        assertEquals(1, service.recibirPublicacion(datos));
        verify(repo, times(1)).saveAndFlush(argThat(n ->
                "ARTISTA".equals(n.getTipo()) && "cliente-1".equals(n.getIdCliente())));
    }
}
