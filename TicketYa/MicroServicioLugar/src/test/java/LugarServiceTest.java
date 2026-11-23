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

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LugarServiceTest {

    @Mock private AsientoRepository asientoRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private LugarService lugarService;

    @Test
    void testReservarAsientoExitoso() {
        AsientoEntity asiento = new AsientoEntity();
        asiento.setEstado("DISPONIBLE");
        when(asientoRepository.findByIdWithLugar(1L)).thenReturn(Optional.of(asiento));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AsientoEntity resultado = lugarService.reservarAsiento(1L, "cliente1");
        assertEquals("RESERVADO", resultado.getEstado());
        assertEquals("cliente1", resultado.getIdClienteReserva());
        assertNotNull(resultado.getReservaHasta());
    }

    @Test
    void testNoPermitirAsientoFisicoDuplicado() {
        SectorEntity sector = new SectorEntity();
        sector.setTipo("NUMERADO");
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector));
        when(asientoRepository.contarMismaUbicacion(5L, "A", 10)).thenReturn(1L);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> lugarService.crearAsiento(5L, " a ", 10));
        assertTrue(error.getMessage().contains("Ya existe"));
        verify(asientoRepository, never()).save(any());
    }

    @Test
    void testNormalizarFilaAntesDeGuardar() {
        SectorEntity sector = new SectorEntity();
        sector.setTipo("NUMERADO");
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector));
        when(asientoRepository.contarMismaUbicacion(5L, "A", 10)).thenReturn(0L);
        when(asientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AsientoEntity asiento = lugarService.crearAsiento(5L, " a ", 10);
        assertEquals("A", asiento.getFila());
    }
    @Test
    void testListarAsientosPublicosDeSectorNumerado() {
        SectorEntity sector = new SectorEntity();
        sector.setId(5L);
        sector.setTipo("NUMERADO");

        AsientoEntity asiento = new AsientoEntity();
        asiento.setId(20L);
        asiento.setSector(sector);
        asiento.setFila("A");
        asiento.setColumna(5);
        asiento.setEstado("DISPONIBLE");

        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector));
        when(asientoRepository.findBySector_IdOrderByFilaAscColumnaAsc(5L))
                .thenReturn(List.of(asiento));

        var resultado = lugarService.listarAsientosPublicos(5L);

        assertEquals(1, resultado.size());
        assertEquals(20L, resultado.get(0).getIdAsiento());
        assertEquals("A", resultado.get(0).getFila());
        assertEquals(5, resultado.get(0).getColumna());
        assertEquals("DISPONIBLE", resultado.get(0).getEstado());
    }

}
