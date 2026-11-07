package com.microserviciolugar.repository;

import com.microserviciolugar.entity.AsientoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsientoRepository extends JpaRepository<AsientoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asiento a WHERE a.idLugar = :id")
    Optional<AsientoEntity> findByIdWithLugar(Long id);
}
