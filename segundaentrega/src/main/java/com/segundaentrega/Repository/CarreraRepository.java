package com.segundaentrega.Repository;

import com.segundaentrega.Entitys.CarreraEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CarreraRepository extends JpaRepository<CarreraEntity, Integer> {

    @Modifying
    @Transactional
    @Query("""
            UPDATE CarreraEntity c
            SET c.carrera = :#{#carrera.carrera},
                c.duracion = :#{#carrera.duracion}
            WHERE c.id = :#{#carrera.id}
            """)
    public int UpdateCarrera(@Param("carrera") CarreraEntity carrera);


    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO carrera (id_carrera, carrera, duracion)
            VALUES (:#{#carrera.id}, :#{#carrera.carrera}, :#{#carrera.duracion})
            """, nativeQuery = true)
    public int InsertCarrera(@Param("carrera") CarreraEntity carrera);
}