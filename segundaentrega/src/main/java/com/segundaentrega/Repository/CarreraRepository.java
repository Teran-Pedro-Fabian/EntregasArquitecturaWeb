package com.segundaentrega.Repository;

import com.segundaentrega.Entitys.CarreraEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * Recupera las carreras que tienen estudiantes inscriptos
     * agrupa por carrera
     * cuenta la cantidad de inscriptos
     * ordena de mayor a menor
     */
    @Query("""
        SELECT c
        FROM CarreraEntity c
        JOIN c.estudiantes ec
        GROUP BY c
        ORDER BY COUNT(ec) DESC
        """)
    public List<CarreraEntity> FindCarrerasOrderByInscriptos();
}