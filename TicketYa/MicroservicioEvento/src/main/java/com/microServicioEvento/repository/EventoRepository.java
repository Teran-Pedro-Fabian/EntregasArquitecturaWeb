package com.microServicioEvento.repository;

import com.microServicioEvento.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Collection;
import java.util.List;

public interface EventoRepository extends JpaRepository<EventoEntity, String>, JpaSpecificationExecutor<EventoEntity> {
    List<EventoEntity> findByEstado(String estado);
    List<EventoEntity> findByEstadoIn(Collection<String> estados);
}
