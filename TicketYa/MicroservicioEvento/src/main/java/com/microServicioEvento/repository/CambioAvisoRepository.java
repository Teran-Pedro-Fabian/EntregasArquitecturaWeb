package com.microServicioEvento.repository;

import com.microServicioEvento.entity.CambioAvisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CambioAvisoRepository extends JpaRepository<CambioAvisoEntity, String> {
    List<CambioAvisoEntity> findTop50ByEntregadoFalseOrderByFechaCreacionAsc();
}
