import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.PreferenciasRepository;
import com.microserviciocliente.servise.PreferenciasService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenciasServiceTest {
    @Mock private PreferenciasRepository preferenciasRepository;
    @InjectMocks private PreferenciasService preferenciasService;

    @Test
    void guardarPreferencia() {
        PreferenciasEntity pref = new PreferenciasEntity();
        pref.setIdCliente("cliente-1");
        pref.setIdEvento("evento-1");
        when(preferenciasRepository.save(pref)).thenReturn(pref);
        assertSame(pref, preferenciasService.savePreferencia(pref));
        verify(preferenciasRepository).save(pref);
    }

    @Test
    void consultarPreferenciasDelCliente() {
        PreferenciasEntity pref = new PreferenciasEntity();
        pref.setIdCliente("cliente-1");
        when(preferenciasRepository.findByidCliente("cliente-1")).thenReturn(List.of(pref));
        assertEquals(1, preferenciasService.findByidCliente("cliente-1").size());
    }
}
