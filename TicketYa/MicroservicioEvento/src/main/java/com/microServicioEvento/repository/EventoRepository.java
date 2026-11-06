package com.microServicioEvento.repository;

import com.microServicioEvento.entity.EventoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EventoRepository extends MongoRepository<EventoEntity, String> {


    List<EventoEntity> findByEstado(String publicado);
}

