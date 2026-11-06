package com.microServicioEvento.service;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

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
}

