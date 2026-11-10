package com.microserviciolugar.repository;

import com.microserviciolugar.entity.LiberacionAsientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiberacionAsientoRepository extends JpaRepository<LiberacionAsientoEntity, Long> {
}
