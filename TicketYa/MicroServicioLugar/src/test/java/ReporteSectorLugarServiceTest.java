import com.microserviciolugar.dto.ReporteSectorLugarDTO;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.service.ReporteSectorLugarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteSectorLugarServiceTest {
    @Mock private SectorRepository sectorRepository;
    @Mock private AsientoRepository asientoRepository;

    @Test
    void capacidadNumeradaCuentaAsientosYGeneralUsaCupos() {
        SectorEntity numerado = new SectorEntity();
        numerado.setId(1L);
        numerado.setNombre("Platea");
        numerado.setTipo("NUMERADO");
        numerado.setCupos(50);

        SectorEntity general = new SectorEntity();
        general.setId(2L);
        general.setNombre("Campo");
        general.setTipo("NO_NUMERADO");
        general.setCupos(200);

        when(sectorRepository.findByIdEvento("evento-1")).thenReturn(
                new java.util.ArrayList<>(List.of(general, numerado)));
        when(asientoRepository.countBySector_Id(1L)).thenReturn(8L);

        ReporteSectorLugarService service = new ReporteSectorLugarService(
                sectorRepository, asientoRepository);
        List<ReporteSectorLugarDTO> resultado = service.obtenerSectores("evento-1");

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdSector());
        assertEquals(8L, resultado.get(0).getCapacidad());
        assertEquals(200L, resultado.get(1).getCapacidad());
        verify(asientoRepository, times(1)).countBySector_Id(1L);
        verify(asientoRepository, never()).countBySector_Id(2L);
    }

    @Test
    void eventoSinSectoresDevuelveListaVacia() {
        when(sectorRepository.findByIdEvento("evento-vacio")).thenReturn(new java.util.ArrayList<>());
        ReporteSectorLugarService service = new ReporteSectorLugarService(
                sectorRepository, asientoRepository);
        assertTrue(service.obtenerSectores("evento-vacio").isEmpty());
    }
}
