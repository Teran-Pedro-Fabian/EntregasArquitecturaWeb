import com.microserviciolugar.entity.ReservaGeneralEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.entity.VentaGeneralEntity;
import com.microserviciolugar.repository.ReservaGeneralRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.repository.VentaGeneralRepository;
import com.microserviciolugar.service.ReservaGeneralService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaGeneralServiceTest {
    @Mock private SectorRepository sectorRepository;
    @Mock private ReservaGeneralRepository reservaRepository;
    @Mock private VentaGeneralRepository ventaRepository;
    @Mock private EntityManager entityManager;
    @InjectMocks private ReservaGeneralService service;

    private SectorEntity sector(int capacidad, int reservados, int vendidos) {
        SectorEntity s = new SectorEntity();
        s.setId(5L);
        s.setIdEvento("evento-1");
        s.setTipo("NO_NUMERADO");
        s.setCupos(capacidad);
        s.setCuposReservados(reservados);
        s.setCuposVendidos(vendidos);
        return s;
    }

    private void prepararBloqueo(SectorEntity s) {
        when(sectorRepository.findByIdParaActualizar(5L)).thenReturn(Optional.of(s));
    }
    private void sinReservasVencidas() {
        when(reservaRepository.findBySectorIdAndEstadoAndReservaHastaLessThanEqual(eq(5L), eq("RESERVADA"),any(Instant.class)))
                .thenReturn(List.of());
    }

    @Test
    void reservarDosDeTresDejaUnCupo() {
        SectorEntity s=sector(3,0,0);
        prepararBloqueo(s);
        sinReservasVencidas();
        when(reservaRepository.save(any(ReservaGeneralEntity.class))).thenAnswer(inv->inv.getArgument(0));
        ReservaGeneralEntity reserva=service.reservar(5L,"cliente-1",2);
        assertEquals(2,reserva.getCantidad());
        assertEquals("RESERVADA",reserva.getEstado());
        assertEquals(2,s.getCuposReservados());
        assertEquals(1,s.getCupos()-s.getCuposReservados()-s.getCuposVendidos());
        verify(sectorRepository, times(2)).save(s);
    }

    @Test
    void noPermitirSobreReservaDelUltimoCupo() {
        SectorEntity s=sector(3,2,0);
        prepararBloqueo(s);
        sinReservasVencidas();
        assertThrows(IllegalStateException.class, () -> service.reservar(5L,"cliente-2",2));
        verify(reservaRepository,never()).save(any());
    }

    @Test
    void noReservarCantidadNoValida() {
        assertThrows(IllegalArgumentException.class, () -> service.reservar(5L,"cliente-1",0));
        assertThrows(IllegalArgumentException.class, () -> service.reservar(5L,"cliente-1",-1));
        verifyNoInteractions(sectorRepository,reservaRepository);
    }

    @Test
    void cancelarReservaDevuelveCupos() {
        SectorEntity s=sector(3,2,0);
        prepararBloqueo(s);
        ReservaGeneralEntity r=new ReservaGeneralEntity();
        r.setId(10L);r.setSector(s);r.setIdCliente("cliente-1");r.setCantidad(2);
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(r));
        service.cancelar(5L,10L,"cliente-1");
        assertEquals("CANCELADA",r.getEstado());
        assertEquals(0,s.getCuposReservados());
        verify(reservaRepository).save(r);
    }

    @Test
    void confirmacionRepetidaDeLaMismaReservaNoDuplicaCupos() {
        SectorEntity s=sector(3,0,2);
        prepararBloqueo(s);
        sinReservasVencidas();
        ReservaGeneralEntity r=new ReservaGeneralEntity();
        r.setId(10L);r.setSector(s);r.setEstado("VENDIDA");r.setIdCompra(30L);r.setIdCliente("cliente-1");r.setCantidad(2);
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(r));
        VentaGeneralEntity v1=new VentaGeneralEntity();v1.setIdTicket(100L);
        VentaGeneralEntity v2=new VentaGeneralEntity();v2.setIdTicket(101L);
        when(ventaRepository.findByIdReservaGeneral(10L)).thenReturn(List.of(v1,v2));
        assertDoesNotThrow(() -> service.confirmar(5L,10L,"cliente-1",30L,List.of(100L,101L)));
        assertEquals(2,s.getCuposVendidos());
        verify(ventaRepository,never()).save(any());
    }

    @Test
    void liberarVentaGeneralDevuelveUnCupo() {
        SectorEntity s=sector(3,0,2);
        prepararBloqueo(s);
        VentaGeneralEntity venta=new VentaGeneralEntity();
        venta.setIdTicket(100L);venta.setIdSector(5L);
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        service.liberarVenta(5L,100L);
        assertEquals("DEVUELTA",venta.getEstado());
        assertEquals(1,s.getCuposVendidos());
        verify(ventaRepository).save(venta);
        verify(sectorRepository).save(s);
    }
}
