import com.microserviciolugar.LugarServiceApplication;
import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.service.LugarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // <--- IMPORTANTE
import org.springframework.data.redis.core.StringRedisTemplate; // <--- IMPORTANTE
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LugarServiceApplication.class)
public class LugarConcurrencyTest {

    @org.junit.jupiter.api.BeforeAll
    static void setupTimeZone() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
        System.out.println("🕒 TimeZone forzado a UTC para el Test");
    }

    @Autowired
    private LugarService lugarService;
    @Autowired
    private AsientoRepository asientoRepository;
    @Autowired
    private SectorRepository sectorRepository;


    @MockBean
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setupRedisMock() {
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testNoDobleVenta() throws InterruptedException {
        SectorEntity sector = new SectorEntity();
        sector.setNombre("VIP");
        sector = sectorRepository.save(sector);

        AsientoEntity asiento = new AsientoEntity();
        asiento.setSector(sector);
        asiento.setEstado("DISPONIBLE");
        // Guardamos el asiento y obtenemos su ID generado por la DB
        asiento = asientoRepository.save(asiento);
        Long idAsiento = asiento.getId();

        // 2. Configurar la concurrencia
        int hilos = 15;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallos = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            String clienteId = "cli-" + i;
            executor.execute(() -> {
                try {
                    latch.await(); // Sincroniza el inicio de todos los hilos
                    lugarService.reservarAsiento(idAsiento, clienteId);
                    exitos.incrementAndGet();
                } catch (Exception e) {
                    // Imprimimos la excepción solo para debuggear si vuelve a fallar
                    // System.out.println("Error en hilo: " + e.getMessage());
                    fallos.incrementAndGet();
                }
            });
        }

        // 3. Ejecución y Cierre
        latch.countDown(); // Dispara todos los hilos al mismo tiempo
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 4. Verificación Final
        System.out.println("--- RESULTADO CONCURRENCIA ---");
        System.out.println("Exitos: " + exitos.get() + " | Fallos: " + fallos.get());

        assertEquals(1, exitos.get(), "Solo un cliente debe lograr la reserva");
        assertEquals(hilos - 1, fallos.get(), "El resto debe haber fallado por bloqueo de DB");
    }
}