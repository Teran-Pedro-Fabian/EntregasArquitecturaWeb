package com.microServicioEvento.repository;

import com.microServicioEvento.entity.ParticipanteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ParticipanteRepository extends MongoRepository<ParticipanteEntity, String> {
    java.util.List<ParticipanteEntity> findByNombreContainingIgnoreCase(String nombre);
}

