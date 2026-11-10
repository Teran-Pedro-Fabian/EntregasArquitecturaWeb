import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {
    @InjectMocks private CompraService compraService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(compraService, "maxEntradas", 4);
    }

    private CompraRequest numerada(List<Long> asientos) {
        CompraRequest r = new CompraRequest();
        r.setIdCliente("cliente-1");
        r.setIdEvento("evento-1");
        r.setIdSector(5L);
        r.setIdsAsientos(asientos);
        r.setMedioPago("TARJETA");
        return r;
    }

    @Test
    void rechazarAsientosRepetidosEnLaMismaCompra() {
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(numerada(List.of(1L, 1L))));
    }

    @Test
    void rechazarMasDeCuatroEntradas() {
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(numerada(List.of(1L, 2L, 3L, 4L, 5L))));
    }

    @Test
    void rechazarMedioDePagoDesconocido() {
        CompraRequest r = numerada(List.of(1L));
        r.setMedioPago("EFECTIVO");
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprar(r));
    }

    @Test
    void rechazarGeneralConCantidadMayorAlLimite() {
        CompraGeneralRequest r = new CompraGeneralRequest();
        r.setIdCliente("cliente-1");
        r.setIdEvento("evento-1");
        r.setIdSector(5L);
        r.setIdReserva(10L);
        r.setCantidad(5);
        r.setMedioPago("TARJETA");
        assertThrows(IllegalArgumentException.class,
                () -> compraService.comprarGeneral(r));
    }
}
