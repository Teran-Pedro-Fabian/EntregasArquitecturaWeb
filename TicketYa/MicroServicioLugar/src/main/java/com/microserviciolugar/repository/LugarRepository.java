package com.microserviciolugar.repository;

import com.microserviciolugar.entity.LugarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LugarRepository extends JpaRepository<LugarEntity, Long> {
}
