import com.microserviciocliente.client.ApiService;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LugarEntityDTO;
import com.microserviciocliente.dto.ParticipanteEntityDTO;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import com.microserviciocliente.servise.RecomendacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecomendacionServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private PreferenciasRepository preferenciasRepository;
    @Mock private ApiService apiService;
    @InjectMocks private RecomendacionService recomendacionService;

    private EventoEntityDTO evento(String id, String tipo, Long idLugar) {
        EventoEntityDTO e = new EventoEntityDTO();
        e.setId(id);
        e.setNombre("Evento " + id);
        e.setTipo(tipo);
        e.setIdLugar(idLugar);
        e.setEstado("PUBLICADO");
        e.setFechaHora("2099-05-20T20:00:00");
        return e;
    }

    private PreferenciasEntity pref(String cliente) {
        PreferenciasEntity p = new PreferenciasEntity();
        p.setIdCliente(cliente);
        return p;
    }

    private void preparar(String cliente, List<PreferenciasEntity> propias, List<EventoEntityDTO> eventos) {
        when(clienteRepository.existsById(cliente)).thenReturn(true);
        when(preferenciasRepository.findByIdCliente(cliente)).thenReturn(propias);
        if (!propias.isEmpty()) {
            when(apiService.buscarEventos()).thenReturn(eventos);
        }
    }

    @Test
    void recomiendaPorTipoGuardadoEnMongo() {
        PreferenciasEntity p = pref("c1");
        p.setTipo("RECITAL");
        EventoEntityDTO recital = evento("e1", "RECITAL", 2L);
        preparar("c1", List.of(p), List.of(evento("e2", "TEATRO", 2L), recital));

        assertEquals(List.of("e1"), recomendacionService.obtenerRecomendaciones("c1")
                .stream().map(EventoEntityDTO::getId).toList());
    }

    @Test
    void recomiendaPorCiudadYLugar() {
        PreferenciasEntity p = pref("c1");
        p.setCiudad(" tandil ");
        p.setIdLugar("2");
        preparar("c1", List.of(p), List.of(evento("e1", "TEATRO", 2L), evento("e2", "TEATRO", 3L)));
        LugarEntityDTO tandil = new LugarEntityDTO();
        tandil.setId(2L);
        tandil.setCiudad("Tandil");
        LugarEntityDTO azul = new LugarEntityDTO();
        azul.setId(3L);
        azul.setCiudad("Azul");
        when(apiService.listarLugares()).thenReturn(List.of(tandil, azul));

        List<EventoEntityDTO> resultado = recomendacionService.obtenerRecomendaciones("c1");
        assertEquals("e1", resultado.get(0).getId());
        assertEquals(1, resultado.size());
    }

    @Test
    void recomiendaPorArtistaFavorito() {
        PreferenciasEntity p = pref("c1");
        p.setIdParticipante("artista-1");
        EventoEntityDTO recital = evento("e1", "RECITAL", 2L);
        preparar("c1", List.of(p), List.of(recital, evento("e2", "RECITAL", 3L)));
        ParticipanteEntityDTO artista = new ParticipanteEntityDTO();
        artista.setId("artista-1");
        artista.setNombre("Artista P11");
        when(apiService.listarParticipantes()).thenReturn(List.of(artista));
        when(apiService.buscarPorParticipante("Artista P11")).thenReturn(List.of(recital));

        assertEquals(List.of("e1"), recomendacionService.obtenerRecomendaciones("c1")
                .stream().map(EventoEntityDTO::getId).toList());
    }

    @Test
    void recomiendaEventoFavoritoDeOtroClienteConGustosSimilares() {
        PreferenciasEntity propia = pref("c1");
        propia.setCiudad("Tandil");
        PreferenciasEntity otraSimilar = pref("c2");
        otraSimilar.setCiudad("Tandil");
        PreferenciasEntity favoritaAjena = pref("c2");
        favoritaAjena.setIdEvento("e2");
        EventoEntityDTO e1 = evento("e1", "TEATRO", 3L);
        EventoEntityDTO e2 = evento("e2", "RECITAL", 3L);
        preparar("c1", List.of(propia), List.of(e1, e2));
        when(preferenciasRepository.findAll()).thenReturn(List.of(propia, otraSimilar, favoritaAjena));

        assertEquals(List.of("e2"), recomendacionService.obtenerRecomendaciones("c1")
                .stream().map(EventoEntityDTO::getId).toList());
    }

    @Test
    void noRecomiendaFavoritosDeClientesSinAfinidad() {
        PreferenciasEntity propia = pref("c1");
        propia.setCiudad("Tandil");
        PreferenciasEntity otra = pref("c2");
        otra.setCiudad("Azul");
        PreferenciasEntity otroEvento = pref("c2");
        otroEvento.setIdEvento("e2");
        preparar("c1", List.of(propia), List.of(evento("e2", "RECITAL", 3L)));
        when(preferenciasRepository.findAll()).thenReturn(List.of(otra, otroEvento));

        assertTrue(recomendacionService.obtenerRecomendaciones("c1").isEmpty());
    }

    @Test
    void noRecomiendaEventosCanceladosAgotadosNiPasados() {
        PreferenciasEntity pref = pref("c1");
        pref.setTipo("RECITAL");
        EventoEntityDTO cancelado = evento("e1", "RECITAL", 2L);
        cancelado.setEstado("CANCELADO");
        EventoEntityDTO agotado = evento("e2", "RECITAL", 2L);
        agotado.setEstado("AGOTADO");
        EventoEntityDTO pasado = evento("e3", "RECITAL", 2L);
        pasado.setFechaHora("2020-05-20T20:00:00");
        preparar("c1", List.of(pref), List.of(cancelado, agotado, pasado));

        assertTrue(recomendacionService.obtenerRecomendaciones("c1").isEmpty());
    }

    @Test
    void sinPreferenciasDevuelveListaVaciaSinConsultarEventos() {
        when(clienteRepository.existsById("c1")).thenReturn(true);
        when(preferenciasRepository.findByIdCliente("c1")).thenReturn(List.of());

        assertTrue(recomendacionService.obtenerRecomendaciones("c1").isEmpty());
        verifyNoInteractions(apiService);
    }

    @Test
    void clienteInexistenteDevuelveError() {
        assertThrows(NoSuchElementException.class,
                () -> recomendacionService.obtenerRecomendaciones("c404"));
        verifyNoInteractions(apiService);
    }

    @Test
    void evitaDuplicadosYLimitaVeinteRecomendaciones() {
        PreferenciasEntity pref = pref("c1");
        pref.setTipo("RECITAL");
        List<EventoEntityDTO> eventos = new ArrayList<>();
        for (int i = 1; i <= 23; i++) {
            eventos.add(evento("e" + i, "RECITAL", 2L));
        }
        eventos.add(evento("e1", "RECITAL", 2L));
        preparar("c1", List.of(pref), eventos);

        List<EventoEntityDTO> resultado = recomendacionService.obtenerRecomendaciones("c1");
        assertEquals(20, resultado.size());
        assertEquals(20, resultado.stream().map(EventoEntityDTO::getId).distinct().count());
    }
}
