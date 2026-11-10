package com.microserviciolugar.repository;

import com.microserviciolugar.entity.AsientoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.Instant;

@Repository
public interface AsientoRepository extends JpaRepository<AsientoEntity, Long> {

    List<AsientoEntity> findTop100ByEstadoAndReservaHastaBeforeOrderByReservaHastaAsc(
            String estado, Instant fecha);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AsientoEntity a WHERE a.id = :id")
    Optional<AsientoEntity> findByIdWithLugar(@Param("id") Long id);
}