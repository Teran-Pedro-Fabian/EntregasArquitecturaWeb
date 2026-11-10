package com.microserviciolugar.repository;

import com.microserviciolugar.entity.LugarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LugarRepository extends JpaRepository<LugarEntity, Long> {

    @Query("SELECT l FROM LugarEntity l WHERE l.latitud = :lat AND l.longitud = :lon")
    public List<LugarEntity> findByLatitudAndLongitud(@Param("lat") Double latitud, @Param("lon") Double longitud);

}