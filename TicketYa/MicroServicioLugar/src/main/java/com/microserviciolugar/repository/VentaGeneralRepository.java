package com.microserviciolugar.repository;

import com.microserviciolugar.entity.VentaGeneralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VentaGeneralRepository extends JpaRepository<VentaGeneralEntity, Long> {
    List<VentaGeneralEntity> findByIdReservaGeneral(Long idReservaGeneral);
}
