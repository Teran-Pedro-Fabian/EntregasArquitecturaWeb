import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.PreferenciasRepository;
import com.microserviciocliente.servise.PreferenciasService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenciasServiceTest {
    @Mock private PreferenciasRepository preferenciasRepository;
    @InjectMocks private PreferenciasService preferenciasService;

    @Test
    void guardarPreferenciaGeneraUuid() {
        PreferenciasEntity pref = new PreferenciasEntity();
        pref.setIdCliente("cliente-1");
        pref.setIdEvento("evento-1");
        when(preferenciasRepository.save(pref)).thenReturn(pref);

        assertSame(pref, preferenciasService.savePreferencia(pref));
        assertNotNull(pref.getId());
        assertDoesNotThrow(() -> UUID.fromString(pref.getId()));
        verify(preferenciasRepository).save(pref);
    }

    @Test
    void guardarPreferenciaConservaIdExistente() {
        PreferenciasEntity pref = new PreferenciasEntity();
        pref.setId("preferencia-123");
        when(preferenciasRepository.save(pref)).thenReturn(pref);

        preferenciasService.savePreferencia(pref);

        assertEquals("preferencia-123", pref.getId());
        verify(preferenciasRepository).save(pref);
    }

    @Test
    void consultarPreferenciasDelCliente() {
        PreferenciasEntity pref = new PreferenciasEntity();
        pref.setIdCliente("cliente-1");
        when(preferenciasRepository.findByIdCliente("cliente-1")).thenReturn(List.of(pref));
        assertEquals(1, preferenciasService.findByidCliente("cliente-1").size());
        verify(preferenciasRepository).findByIdCliente("cliente-1");
    }
}
