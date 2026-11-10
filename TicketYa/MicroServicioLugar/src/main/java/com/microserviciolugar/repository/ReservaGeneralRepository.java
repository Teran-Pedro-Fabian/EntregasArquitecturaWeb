package com.microserviciolugar.repository;

import com.microserviciolugar.entity.ReservaGeneralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface ReservaGeneralRepository extends JpaRepository<ReservaGeneralEntity, Long> {

    List<ReservaGeneralEntity> findBySectorIdAndEstadoAndReservaHastaLessThanEqual(
            Long idSector, String estado, Instant fecha);

    List<ReservaGeneralEntity> findTop100ByEstadoAndReservaHastaBeforeOrderByReservaHastaAsc(
            String estado, Instant fecha);
}
