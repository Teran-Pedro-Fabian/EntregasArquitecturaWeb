package com.microServicioEvento.repository;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoParticipanteRepositoy extends MongoRepository<EventoParticipanteEntity, String> {
}
