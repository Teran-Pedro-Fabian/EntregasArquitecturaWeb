package com.segundaentrega.Repository;

import com.segundaentrega.Entitys.CarreraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarreraRepository extends JpaRepository<CarreraEntity, Integer> {


    @Query("""
            UPDATE CarreraEntity c
            SET c.nombre = :#{#carrera.nombre},
                c.cantidadMaterias = :#{#carrera.cantidadMaterias},
                c.cantidadAnios = :#{#carrera.cantidadAnios}
            WHERE c.id = :#{#carrera.id}
            """)
    public int UpdateCarrera(@Param("carrera") CarreraEntity carrera);


    @Query ("""
            INSERT INTO CarreraEntity (nombre, cantidadMaterias, cantidadAnios)
            VALUES (:#{#carrera.nombre}, :#{#carrera.cantidadMaterias}, :#{#carrera.cantidadAnios})
            """)
    public int InsertCarrera(@Param("carrera") CarreraEntity carrera);
}
