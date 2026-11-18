import com.microservicioticket.dto.ColaResponse;
import com.microservicioticket.service.ColaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColaServiceTest {
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ZSetOperations<String, String> zSetOperations;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private ColaService colaService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(colaService, "tandaSize", 2);
        ReflectionTestUtils.setField(colaService, "ttlMinutos", 60L);
    }

    private void eventoPublicado(String demanda) {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("estado", "PUBLICADO", "demanda", demanda));
    }

    @Test
    void entrarAsignaTurnoYGuardaEnRedis() {
        eventoPublicado("ALTA");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(1L);

        ColaResponse respuesta = colaService.entrar("evento-1", "cliente-1");

        assertEquals(1, respuesta.getPosicion());
        assertEquals(1, respuesta.getTotal());
        assertTrue(respuesta.getHabilitado());
        verify(zSetOperations).addIfAbsent("fila:evento:evento-1", "cliente-1", 1.0);
    }

    @Test
    void noPermiteFilaEnEventoDeDemandaBaja() {
        eventoPublicado("BAJA");
        assertThrows(IllegalStateException.class,
                () -> colaService.entrar("evento-1", "cliente-1"));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void noPermiteFilaEnEventoSinPublicar() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("estado", "BORRADOR", "demanda", "ALTA"));
        assertThrows(IllegalStateException.class,
                () -> colaService.entrar("evento-1", "cliente-1"));
    }

    @Test
    void consultarPosicionDeClienteFueraDeFilaFalla() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> colaService.consultarPosicion("evento-1", "cliente-1"));
    }

    @Test
    void clienteFueraDeLaTandaNoPuedeComprar() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(2L);
        assertThrows(IllegalStateException.class,
                () -> colaService.validarTurno("evento-1", "cliente-3", "ALTA"));
    }

    @Test
    void clienteSinFilaNoPuedeComprar() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> colaService.validarTurno("evento-1", "cliente-3", "ALTA"));
    }

    @Test
    void clienteHabilitadoPuedeComprar() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(1L);
        assertDoesNotThrow(() -> colaService.validarTurno("evento-1", "cliente-2", "ALTA"));
    }

    @Test
    void eventoDeDemandaBajaNoRequiereFila() {
        assertDoesNotThrow(() -> colaService.validarTurno("evento-1", "cliente-1", "BAJA"));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void compraConfirmadaLiberaTurno() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        colaService.finalizarCompra("evento-1", "cliente-1");
        verify(zSetOperations).remove("fila:evento:evento-1", "cliente-1");
    }
}
