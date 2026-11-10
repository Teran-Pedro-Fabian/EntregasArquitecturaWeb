package com.microservicioticket.Repository;

import com.microservicioticket.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<CompraEntity, Long> {
    Optional<CompraEntity> findByClaveIdempotencia(String claveIdempotencia);
}
