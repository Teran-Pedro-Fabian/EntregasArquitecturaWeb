package com.microServicioEvento.service;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private MongoTemplate mongoTemplate; // Usamos MongoTemplate para búsquedas dinámicas
    @Autowired
    private RestTemplate restTemplate; // Para consultar la ciudad en el MicroServicioLugar

    public EventoEntity crearEvento(EventoEntity evento) {
        evento.setEstado("BORRADOR"); // Todo evento nace como borrador
        return eventoRepository.save(evento);
    }

    public EventoEntity publicarEvento(String id) {
        EventoEntity evento = eventoRepository.findById(id).orElseThrow();
        evento.setEstado("PUBLICADO");
        return eventoRepository.save(evento);
    }

    public List<EventoEntity> buscarEventosPublicados() {
        return eventoRepository.findByEstado("PUBLICADO");
    }

    public EventoEntity obtenerEvento(String id) {
        return eventoRepository.findById(id).orElse(null);
    }

    public List<EventoEntity> buscarEventos(String tipo, String organizador, String participante, Double lat, Double lon, String ordenarPor) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 1. Filtro por Tipo (Coincidencia exacta)
        if (tipo != null && !tipo.isEmpty()) {
            criteria.and("tipo").is(tipo);
        }

        // 2. Filtro por Organizador (Búsqueda parcial, no importa mayúsculas)
        if (organizador != null && !organizador.isEmpty()) {
            criteria.and("organizador").regex(organizador, "i");
        }

        // 3. Filtro por Participante
        // Nota: Para simplificar, buscamos que el nombre del participante esté en la descripción
        if (participante != null && !participante.isEmpty()) {
            criteria.and("descripcion").regex(participante, "i");
        }

        // 4) Filtro geografico corrección
        if (lat != null && lon != null) {
            // Consultar los lugares que coinciden con la ubicacion
            String urlLugar = "http://localhost:8083/lugares/buscarPorUbicacion?lat=" + lat + "&lon=" + lon;

            // Obtener los ID de los lugares encontrados
            Long[] idsLugares = restTemplate.getForObject(urlLugar, Long[].class);

            // Filtrar los eventos que pertenecen a esos lugares
            if (idsLugares != null && idsLugares.length > 0) {
                criteria.and("idLugar").in(Arrays.asList(idsLugares));
            } else {
                // devolver una lista vacia
                return Collections.emptyList();
            }
        }


        query.addCriteria(criteria);

        // 5. ORDENAMIENTO
        if ("fecha".equalsIgnoreCase(ordenarPor)) {
            query.with(Sort.by(Sort.Direction.ASC, "fechaHora"));
        } else if ("precio".equalsIgnoreCase(ordenarPor)) {
            // El precio está en el Sector (otro MS), aquí ordenamos por nombre como fallback
            query.with(Sort.by(Sort.Direction.ASC, "nombre"));
        } else {
            query.with(Sort.by(Sort.Direction.DESC, "nombre")); // Relevancia por defecto
        }

        return mongoTemplate.find(query, EventoEntity.class);
    }
}

