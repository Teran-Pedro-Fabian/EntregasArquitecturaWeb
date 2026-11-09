import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.service.LugarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LugarServiceTest {
    @Mock
    private AsientoRepository asientoRepository;
    @Mock
    private StringRedisTemplate redisTemplate;

    // 1. Creamos un mock para las operaciones de valor de Redis
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LugarService lugarService;

    @BeforeEach
    void setup() {
        // 2. Configuramos que redisTemplate.opsForValue() devuelva nuestro mock de valueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testReservarAsiento_Exitoso() {
        // Setup: Asiento disponible
        AsientoEntity asiento = new AsientoEntity();
        asiento.setEstado("DISPONIBLE");

        when(asientoRepository.findByIdWithLugar(1L)).thenReturn(Optional.of(asiento));

        // Ejecutamos
        AsientoEntity resultado = lugarService.reservarAsiento(1L, "cliente1");

        // Verificamos
        org.junit.jupiter.api.Assertions.assertEquals("RESERVADO", resultado.getEstado());
    }
}
