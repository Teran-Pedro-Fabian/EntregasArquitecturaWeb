package com.segundaentrega.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.segundaentrega.Entitys.EstudianteCarrera;

public interface EstudianteCarreraRepository extends JpaRepository<EstudianteCarrera, Integer> {

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.carrera.id = :idCarrera
            """)
    public EstudianteCarrera FindByIdCarrera(@Param("idCarrera") int idCarrera);


    @Query("""
            SELECT ec.carrera
            FROM EstudianteCarrera ec
            """)
    public List<EstudianteCarrera> FindAll();


    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.estudiante.DNI = :idEstudiante
            """)
    public EstudianteCarrera findByEstudiante(@Param("idEstudiante") int idEstudiante);


    @Query("""
            INSERT INTO EstudianteCarrera(id, estudiante, carrera, inscripcion, graduacion, antiguedad)
            VALUES (:#{#estCarr.id}, :#{#estCarr.estudiante}, :#{#estCarr.carrera},
                    :#{#estCarr.inscripcion}, :#{#estCarr.graduacion}, :#{#estCarr.antiguedad})
            """)
    public void Insert(@Param("estCarr") EstudianteCarrera estCarr);


    @Query("""
            UPDATE EstudianteCarrera ec
            SET ec.estudiante = :#{#estCarr.estudiante},
                ec.carrera = :#{#estCarr.carrera},
                ec.inscripcion = :#{#estCarr.inscripcion},
                ec.graduacion = :#{#estCarr.graduacion},
                ec.antiguedad = :#{#estCarr.antiguedad}
            WHERE ec.id = :#{#estCarr.id}
            """)
    public void Update(@Param("estCarr") EstudianteCarrera estCarr);


    @Query("""
            DELETE
            FROM EstudianteCarrera ec
            WHERE ec.id = :id
            """)
    public void delete(@Param("id") int id);


    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            ORDER BY ec.estudiante.DNI
            """)
    public List<EstudianteCarrera> FindAllOrderByEstudiantes();


    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.carrera.id = :idCarr
            """)
    public List<EstudianteCarrera> FindAllEstudianteFilterCarrera(@Param("idCarr") int idCarr);
}
