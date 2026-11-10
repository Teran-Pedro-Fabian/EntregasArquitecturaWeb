package com.microServicioEvento.repository;

import com.microServicioEvento.entity.ParticipanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipanteRepository extends JpaRepository<ParticipanteEntity, String> {
    List<ParticipanteEntity> findByNombreContainingIgnoreCase(String nombre);
}
