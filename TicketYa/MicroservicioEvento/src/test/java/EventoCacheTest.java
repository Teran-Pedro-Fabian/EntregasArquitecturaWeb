import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.dto.BusquedaEventoFiltro;
import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.CambioAvisoRepository;
import com.microServicioEvento.repository.ParticipanteRepository;
import com.microServicioEvento.service.EventoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = EventoCacheTest.Config.class)
public class EventoCacheTest {
    @Autowired private EventoService service;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private CacheManager cacheManager;

    @BeforeEach
    void preparar() {
        reset(eventoRepository);
        cacheManager.getCache("catalogo-eventos").clear();
        cacheManager.getCache("busqueda-eventos").clear();
    }

    @Test
    void segundaConsultaDeCatalogoUsaCache() {
        when(eventoRepository.findByEstadoIn(List.of("PUBLICADO", "AGOTADO"))).thenReturn(List.of());

        service.buscarEventosPublicados();
        service.buscarEventosPublicados();

        verify(eventoRepository, times(1)).findByEstadoIn(List.of("PUBLICADO", "AGOTADO"));
    }

    @Test
    void segundaBusquedaConLosMismosFiltrosUsaCache() {
        when(eventoRepository.findAll(any(Specification.class))).thenReturn(List.of());
        BusquedaEventoFiltro filtro1 = new BusquedaEventoFiltro();
        filtro1.setTipo("RECITAL");
        BusquedaEventoFiltro filtro2 = new BusquedaEventoFiltro();
        filtro2.setTipo("RECITAL");

        service.buscarEventos(filtro1);
        service.buscarEventos(filtro2);

        verify(eventoRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void crearEventoInvalidaLaCacheDelCatalogo() {
        when(eventoRepository.findByEstadoIn(List.of("PUBLICADO", "AGOTADO"))).thenReturn(List.of());
        EventoEntity evento = new EventoEntity();
        evento.setNombre("Prueba");
        evento.setDescripcion("Prueba");
        evento.setTipo("RECITAL");
        evento.setOrganizador("organizador");
        evento.setFechaHora(LocalDateTime.now().plusDays(15).toString());
        evento.setIdLugar(1L);
        when(eventoRepository.save(any(EventoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.buscarEventosPublicados();
        service.crearEvento(evento);
        service.buscarEventosPublicados();

        verify(eventoRepository, times(2)).findByEstadoIn(List.of("PUBLICADO", "AGOTADO"));
    }

    @Test
    void detalleSinCacheParaEvitarEstadosDesactualizadosEnCompras() {
        EventoEntity evento = new EventoEntity();
        when(eventoRepository.findById("e1")).thenReturn(Optional.of(evento));

        service.obtenerEvento("e1");
        service.obtenerEvento("e1");

        verify(eventoRepository, times(2)).findById("e1");
    }

    @Configuration
    @EnableCaching
    static class Config {
        @Bean EventoService service() { return new EventoService(); }
        @Bean EventoRepository eventoRepository() { return mock(EventoRepository.class); }
        @Bean CambioAvisoRepository cambioAvisoRepository() { return mock(CambioAvisoRepository.class); }
        @Bean EventoParticipanteRepositoy eventoParticipanteRepositoy() { return mock(EventoParticipanteRepositoy.class); }
        @Bean ParticipanteRepository participanteRepository() { return mock(ParticipanteRepository.class); }
        @Bean RestTemplate restTemplate() { return mock(RestTemplate.class); }
        @Bean CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("catalogo-eventos", "busqueda-eventos");
        }
    }
}
