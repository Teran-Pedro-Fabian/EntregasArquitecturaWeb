package com.microServicioEvento.config;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.entity.ParticipanteEntity;
import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.ParticipanteRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Carga un catalogo chico y presentable para la demo del frontend. */
@Component
@ConditionalOnProperty(name = "ticketya.demo.seed", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final EventoParticipanteRepositoy relacionRepository;

    public DemoDataInitializer(EventoRepository eventoRepository,
                               ParticipanteRepository participanteRepository,
                               EventoParticipanteRepositoy relacionRepository) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.relacionRepository = relacionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearParticipantes();
        crearEventos();
        crearRelaciones();
    }

    private void crearParticipantes() {
        participante("participante-rata-blanca", "Rata Blanca");
        participante("participante-raul-lavie", "Raúl Lavié");
        participante("participante-chichilo-viale", "Chichilo Viale");
        participante("participante-los-tapitas", "Los Tapitas");
        participante("participante-santamarina", "Club Santamarina");
        participante("participante-mitre", "Mitre de Santiago del Estero");
        participante("participante-yamila-cafrune", "Yamila Cafrune");
        participante("participante-bruja-salguero", "La Bruja Salguero");
    }

    private void crearEventos() {
        evento("evento-rata-blanca", "Rata Blanca en Tandil",
                "Rata Blanca vuelve a Tandil con un show especial. Datos de fecha, sectores y precios preparados para la demostracion de TicketYa.",
                "RECITAL", "Tandil Producciones", fecha(30, 21, 0), 1001L,
                "PUBLICADO", "ALTA", true);

        evento("evento-chacinar", "Chacinar - Fiesta del Salame y del Cerdo",
                "Festival gastronomico de Tandil con productores regionales, espectaculos y actividades vinculadas al Salame de Tandil.",
                "FESTIVAL", "Clúster Porcino de Tandil", fecha(45, 12, 0), 1002L,
                "PUBLICADO", "ALTA", true);

        evento("evento-santamarina-mitre", "Santamarina vs Mitre de Santiago del Estero",
                "Partido de futbol en Tandil preparado como evento deportivo de demostracion para el flujo de compra general.",
                "DEPORTE", "Club Santamarina", fecha(60, 16, 30), 1003L,
                "PUBLICADO", "NORMAL", true);

        evento("evento-festival-sierra", "Festival de la Sierra",
                "Encuentro tradicional de musica y danza en el Anfiteatro Martin Fierro de Tandil.",
                "FESTIVAL", "Peña El Cielito", fecha(75, 20, 30), 1004L,
                "PUBLICADO", "NORMAL", true);
    }

    private void participante(String id, String nombre) {
        if (participanteRepository.existsById(id)) return;
        ParticipanteEntity participante = new ParticipanteEntity();
        participante.setId(id);
        participante.setNombre(nombre);
        participanteRepository.save(participante);
    }

    private void evento(String id, String nombre, String descripcion, String tipo,
                        String organizador, String fechaHora, Long idLugar,
                        String estado, String demanda, boolean publicacionAvisada) {
        if (eventoRepository.existsById(id)) return;
        EventoEntity evento = new EventoEntity();
        evento.setId(id);
        evento.setNombre(nombre);
        evento.setDescripcion(descripcion);
        evento.setTipo(tipo);
        evento.setOrganizador(organizador);
        evento.setFechaHora(fechaHora);
        evento.setIdLugar(idLugar);
        evento.setEstado(estado);
        evento.setDemanda(demanda);
        evento.setPublicacionAvisada(publicacionAvisada);
        eventoRepository.save(evento);
    }

    private void crearRelaciones() {
        relacion("rel-rata-1", "evento-rata-blanca", "participante-rata-blanca");

        relacion("rel-chacinar-1", "evento-chacinar", "participante-raul-lavie");
        relacion("rel-chacinar-2", "evento-chacinar", "participante-chichilo-viale");
        relacion("rel-chacinar-3", "evento-chacinar", "participante-los-tapitas");

        relacion("rel-santamarina-1", "evento-santamarina-mitre", "participante-santamarina");
        relacion("rel-santamarina-2", "evento-santamarina-mitre", "participante-mitre");

        relacion("rel-sierra-1", "evento-festival-sierra", "participante-yamila-cafrune");
        relacion("rel-sierra-2", "evento-festival-sierra", "participante-bruja-salguero");
    }

    private void relacion(String id, String idEvento, String idParticipante) {
        if (relacionRepository.existsById(id)) return;
        EventoParticipanteEntity relacion = new EventoParticipanteEntity();
        relacion.setId(id);
        relacion.setIdEvento(idEvento);
        relacion.setIdParticipante(idParticipante);
        relacionRepository.save(relacion);
    }

    private String fecha(int dias, int hora, int minuto) {
        LocalDate base = LocalDate.now().plusDays(dias);
        LocalDateTime fecha = base.atTime(hora, minuto, 0);
        return fecha.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
