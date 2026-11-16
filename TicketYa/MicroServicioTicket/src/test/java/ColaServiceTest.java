import com.microservicioticket.dto.ColaResponse;
import com.microservicioticket.service.ColaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ColaServiceTest {
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ZSetOperations<String, String> zSetOperations;
    @InjectMocks private ColaService colaService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(colaService, "tandaSize", 10);
    }

    @Test
    void clienteNuevoIngresaALaFilaComoPrimerLugar() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(null);
        when(zSetOperations.add(anyString(), (Set<TypedTuple<String>>) anyMap())).thenReturn(1L);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(1L);

        ColaResponse respuesta = colaService.entrar("evento-1", "cliente-1");
        assertEquals("evento-1", respuesta.getIdEvento());
        assertEquals("cliente-1", respuesta.getIdCliente());
        assertEquals(1, respuesta.getPosicion());
        assertEquals(1, respuesta.getTotal());
        assertTrue(respuesta.getHabilitado());
        verify(zSetOperations).add(eq("fila:evento:evento-1"), (Set<TypedTuple<String>>) anyMap());
    }

    @Test
    void clienteYaEnFilaNoSeVuelveANuevoLugar() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(1700000000000.0);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(1L);

        ColaResponse respuesta = colaService.entrar("evento-1", "cliente-1");
        assertEquals(1, respuesta.getPosicion());
        assertEquals(1, respuesta.getTotal());
        assertTrue(respuesta.getHabilitado());
        verify(zSetOperations, never()).add(anyString(), (Set<TypedTuple<String>>) anyMap());
    }

    @Test
    void consultarPosicionDevuelvePosicionDelCliente() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(1700000000000.0);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(1L);
        when(zSetOperations.zCard(anyString())).thenReturn(2L);

        ColaResponse respuesta = colaService.consultarPosicion("evento-1", "cliente-1");
        assertEquals(2, respuesta.getPosicion());
        assertEquals(2, respuesta.getTotal());
        assertTrue(respuesta.getHabilitado());
    }

    @Test
    void clienteFueraDeLaTandaNoHabilitado() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(1700000000000.0);
        when(zSetOperations.rank(anyString(), anyString())).thenReturn(10L);
        when(zSetOperations.zCard(anyString())).thenReturn(11L);

        ColaResponse respuesta = colaService.consultarPosicion("evento-1", "cliente-1");
        assertEquals(11, respuesta.getPosicion());
        assertEquals(11, respuesta.getTotal());
        assertFalse(respuesta.getHabilitado());
    }

    @Test
    void consultarPosicionDeClienteFueraDeLaFilaFalla() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(anyString(), anyString())).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> colaService.consultarPosicion("evento-1", "cliente-1"));
        assertTrue(error.getMessage().contains("no esta en la fila"));
    }
}
