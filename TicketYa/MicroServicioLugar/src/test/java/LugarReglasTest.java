import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.service.LugarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LugarReglasTest {
    @Mock private AsientoRepository asientoRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String,String> valueOperations;
    @InjectMocks private LugarService lugarService;

    private AsientoEntity asiento(String estado) {
        AsientoEntity a = new AsientoEntity();
        a.setId(11L);
        a.setEstado(estado);
        return a;
    }

    @Test
    void reservarAsientoPoneDiezMinutosDeTtl() {
        AsientoEntity a = asiento("DISPONIBLE");
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Instant antes = Instant.now();
        lugarService.reservarAsiento(11L,"cliente-1");
        assertEquals("RESERVADO",a.getEstado());
        assertFalse(a.getReservaHasta().isBefore(antes.plusSeconds(600)));
        verify(valueOperations).set("reserva:asiento:11","cliente-1",10,TimeUnit.MINUTES);
    }

    @Test
    void noPermitirReservarUnAsientoYaVendido() {
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(asiento("VENDIDO")));
        assertThrows(RuntimeException.class, () -> lugarService.reservarAsiento(11L,"cliente-1"));
        verify(asientoRepository, never()).save(any());
    }

    @Test
    void confirmarMismoTicketDosVecesEsIdempotente() {
        AsientoEntity a = asiento("VENDIDO");
        a.setIdTicketVendido(90L);
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        assertDoesNotThrow(() -> lugarService.confirmarVenta(11L,"cliente-1",90L));
        verify(asientoRepository, never()).save(any());
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void noConfirmarOtroTicketParaElMismoAsiento() {
        AsientoEntity a = asiento("VENDIDO");
        a.setIdTicketVendido(90L);
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        assertThrows(IllegalStateException.class, () -> lugarService.confirmarVenta(11L,"cliente-1",91L));
        verify(asientoRepository, never()).save(any());
    }

    @Test
    void noConfirmarReservaVencida() {
        AsientoEntity a = asiento("RESERVADO");
        a.setIdClienteReserva("cliente-1");
        a.setReservaHasta(Instant.now().minusSeconds(1));
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        assertThrows(RuntimeException.class, () -> lugarService.confirmarVenta(11L,"cliente-1",91L));
        verify(asientoRepository, never()).save(any());
    }

    @Test
    void confirmaVentaYQuitaReservaRedis() {
        AsientoEntity a = asiento("RESERVADO");
        a.setIdClienteReserva("cliente-1");
        a.setReservaHasta(Instant.now().plusSeconds(600));
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        lugarService.confirmarVenta(11L,"cliente-1",91L);
        assertEquals("VENDIDO",a.getEstado());
        assertEquals(91L,a.getIdTicketVendido());
        assertNull(a.getReservaHasta());
        verify(redisTemplate).delete("reserva:asiento:11");
        verify(asientoRepository).save(a);
    }

    @Test
    void devuelvePrecioOficialDelSector() {
        SectorEntity s = new SectorEntity();
        s.setIdEvento("evento-1");
        s.setPrecio(12000.0);
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(s));
        assertEquals(12000.0,lugarService.obtenerPrecioSector(5L,"evento-1"));
    }

    @Test
    void noUsarPrecioDeOtroEvento() {
        SectorEntity s = new SectorEntity();
        s.setIdEvento("evento-1");
        s.setPrecio(12000.0);
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(s));
        assertThrows(IllegalArgumentException.class, () -> lugarService.obtenerPrecioSector(5L,"evento-2"));
    }

    @Test
    void liberarReservaVencidaEnLimpieza() {
        AsientoEntity a = asiento("RESERVADO");
        a.setIdClienteReserva("cliente-1");
        a.setReservaHasta(Instant.now().minusSeconds(10));
        when(asientoRepository.findTop100ByEstadoAndReservaHastaBeforeOrderByReservaHastaAsc(eq("RESERVADO"), any(Instant.class)))
                .thenReturn(List.of(a));
        when(asientoRepository.findByIdWithLugar(11L)).thenReturn(Optional.of(a));
        lugarService.liberarReservasVencidas();
        assertEquals("DISPONIBLE",a.getEstado());
        assertNull(a.getIdClienteReserva());
        verify(asientoRepository).save(a);
        verify(redisTemplate).delete("reserva:asiento:11");
    }
}
