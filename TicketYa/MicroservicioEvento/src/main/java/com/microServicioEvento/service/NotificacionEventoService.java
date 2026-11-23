package com.microServicioEvento.service;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.CambioAvisoEntity;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.CambioAvisoRepository;
import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reintenta las publicaciones y cambios que todavia no se pudieron notificar */
@Service
public class NotificacionEventoService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionEventoService.class);
    @Autowired private EventoRepository eventoRepository;
    @Autowired private CambioAvisoRepository cambioAvisoRepository;
    @Autowired private EventoParticipanteRepositoy eventoParticipanteRepository;
    @Autowired private RestTemplate restTemplate;
    @Value("${ticketya.cliente.url:http://localhost:8081}") private String clienteUrl;
    @Value("${ticketya.ticket.url:http://localhost:8084}") private String ticketUrl;
    @Value("${ticketya.lugar.url:http://localhost:8083}") private String lugarUrl;

    @Scheduled(fixedDelayString = "${ticketya.notificaciones.intervalo-ms:10000}")
    public void avisarPendientes() {
        for (EventoEntity evento : eventoRepository
                .findTop50ByEstadoAndPublicacionAvisadaFalseOrderByIdAsc("PUBLICADO")) {
            try {
                publicarAviso(evento);
                evento.setPublicacionAvisada(true);
                eventoRepository.save(evento);
            } catch (RestClientException | IllegalStateException ex) {
                log.warn("Publicacion {} pendiente de aviso: {}", evento.getId(), ex.getMessage());
            }
        }
        for (CambioAvisoEntity aviso : cambioAvisoRepository.findTop50ByEntregadoFalseOrderByFechaCreacionAsc()) {
            try {
                restTemplate.postForEntity(ticketUrl + "/ticket/interno/eventos/" +
                        aviso.getIdEvento() + "/notificar-cambio",
                        Map.of("referencia", aviso.getId(), "mensaje", aviso.getMensaje()), Void.class);
                aviso.setEntregado(true);
                cambioAvisoRepository.save(aviso);
            } catch (RestClientException ex) {
                log.warn("Cambio {} pendiente de aviso: {}", aviso.getId(), ex.getMessage());
            }
        }
    }

    private void publicarAviso(EventoEntity evento) {
        // Lugar tiene la ciudad, no se confia en un campo enviado por el organizador
        Map[] lugares = restTemplate.getForObject(lugarUrl + "/lugares", Map[].class);
        if (lugares == null) throw new IllegalStateException("No se pudieron consultar los lugares");
        String ciudad = "";
        for (Map lugar : lugares) {
            if (lugar.get("id") != null && evento.getIdLugar() != null &&
                    evento.getIdLugar().toString().equals(lugar.get("id").toString())) {
                if (lugar.get("ciudad") != null) ciudad = lugar.get("ciudad").toString();
                break;
            }
        }
        List<String> participantes = new ArrayList<>();
        for (EventoParticipanteEntity relacion : eventoParticipanteRepository.findByIdEvento(evento.getId())) {
            participantes.add(relacion.getIdParticipante());
        }
        restTemplate.postForEntity(clienteUrl + "/clientes/interno/notificaciones/publicacion",
                Map.of("idEvento", evento.getId(), "nombreEvento", evento.getNombre(),
                       "ciudad", ciudad, "idLugar", String.valueOf(evento.getIdLugar()),
                       "idsParticipantes", participantes), Void.class);
    }
}
