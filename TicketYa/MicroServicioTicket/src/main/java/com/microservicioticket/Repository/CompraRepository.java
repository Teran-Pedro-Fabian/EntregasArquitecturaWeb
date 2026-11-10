package com.microservicioticket.Repository;

import com.microservicioticket.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<CompraEntity, Long> {
}
