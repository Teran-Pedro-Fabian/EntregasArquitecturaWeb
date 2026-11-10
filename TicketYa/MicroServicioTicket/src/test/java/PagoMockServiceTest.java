import com.microservicioticket.service.PagoMockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class PagoMockServiceTest {

    private PagoMockService pagoMockService;

    @BeforeEach
    public void preparar() {
        pagoMockService = new PagoMockService();
        // Simular las propiedades configurables de Spring
        ReflectionTestUtils.setField(pagoMockService, "pagoAprobado", true);
        ReflectionTestUtils.setField(pagoMockService, "reintegroAprobado", true);
    }

    @Test
    public void testPago_Aprobado() {
        assertTrue(pagoMockService.procesarPago("cliente1", 15000.0));
    }

    @Test
    public void testPago_Rechazado() {
        ReflectionTestUtils.setField(pagoMockService, "pagoAprobado", false);
        assertFalse(pagoMockService.procesarPago("cliente1", 15000.0));
    }

    @Test
    public void testPago_DatosInvalidos() {
        assertThrows(RuntimeException.class,
                () -> pagoMockService.procesarPago(null, 15000.0));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.procesarPago(" ", 15000.0));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.procesarPago("cliente1", null));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.procesarPago("cliente1", 0.0));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.procesarPago("cliente1", -1.0));
    }

    @Test
    public void testReintegro_Aprobado() {
        assertTrue(pagoMockService.solicitarReintegro(10L, 15000.0));
    }

    @Test
    public void testReintegro_Rechazado() {
        ReflectionTestUtils.setField(pagoMockService, "reintegroAprobado", false);
        assertFalse(pagoMockService.solicitarReintegro(10L, 15000.0));
    }

    @Test
    public void testReintegro_DatosInvalidos() {
        assertThrows(RuntimeException.class,
                () -> pagoMockService.solicitarReintegro(null, 15000.0));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.solicitarReintegro(10L, null));
        assertThrows(RuntimeException.class,
                () -> pagoMockService.solicitarReintegro(10L, -1.0));
    }
}
