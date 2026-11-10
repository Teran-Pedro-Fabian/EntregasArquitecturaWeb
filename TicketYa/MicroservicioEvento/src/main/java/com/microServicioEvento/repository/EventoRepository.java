package com.microServicioEvento.repository;

import com.microServicioEvento.entity.EventoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface EventoRepository extends MongoRepository<EventoEntity, String> {
    List<EventoEntity> findByEstado(String estado);
    List<EventoEntity> findByEstadoIn(Collection<String> estados);
}
