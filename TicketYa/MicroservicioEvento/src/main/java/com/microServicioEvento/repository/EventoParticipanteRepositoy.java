package com.microServicioEvento.repository;

import com.microServicioEvento.entity.EventoParticipanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// Se conserva el nombre Repositoy para no romper las referencias existentes
public interface EventoParticipanteRepositoy extends JpaRepository<EventoParticipanteEntity, String> {
    List<EventoParticipanteEntity> findByIdEvento(String idEvento);
    List<EventoParticipanteEntity> findByIdParticipanteIn(List<String> idsParticipantes);
    List<EventoParticipanteEntity> findByIdEventoIn(List<String> idsEventos);
    Optional<EventoParticipanteEntity> findByIdEventoAndIdParticipante(String idEvento, String idParticipante);
}
