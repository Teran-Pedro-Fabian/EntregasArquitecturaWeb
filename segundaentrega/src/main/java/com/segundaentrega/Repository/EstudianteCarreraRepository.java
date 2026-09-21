package com.segundaentrega.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.segundaentrega.Entitys.EstudainteCarrera;

public interface EstudianteCarreraRepository extends JpaRepository<EstudainteCarrera, Integer> {

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.id_carrera =:idCarrera
            """)
    public EstudainteCarrera FindByIdCarrera(@Param("idCarrera") int idCarrera);


    @Query ("""
            SELECT ec.id_carrera
            FROM EstudianteCarrera ec
            """)
    public List<EstudainteCarrera> FindAll();

    @Query ("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.id_estudiante =:idEstudiante
            """)
    public EstudainteCarrera findByEstudiante(@Param("idEstudiante") int idEstudiante);


    @Query ("""
            INSERT INTO EstidainteCarrera(id, id_estudiante, id_carrera, inscripcion, graduacion, antiguedad)
            VALUES (:#{#estCarr.id}, :#{#estCarr.id_estudiante}, :#{#estCarr.id_carrera}, :#{#inscripcion}, 
            :#{#estCarr.graduacion}, :#{#estCarr.antiguedad})
            """)
    public void Insert(@Param("estCarr") EstudainteCarrera estCarr);


    @Query("""
            UPDATE EstudainteCarrera ec 
            SET ec.id_estudiante = :#{#estCarr.id_estudiante}, 
                ec.id_carrera = :#{#estCarr.id_carrera},
                ec.inscripcion = :#{#estCarr.inscripcion},
                ec.graduacion = :#{#estCarr.graduacion},
                ec.antiguedad = :#{#estCarr.antiguedad}
            WHERE ec.id = :#{#estCarr.id}
            """)
    public void Update(@Param("estCarr") EstudainteCarrera estCarr);

    @Query("""
            DELETE 
            FROM EstudainteCarrera ec
            WHERE ec.id =: id 
            """)
    public void delete(@Param("id") int id);


    @Query("""
            SELECT ec , 
            FROM EstudainteCarrera ec
            ORDER BY ec.id_estudiante
            """)
    public List<EstudainteCarrera> FindAllOrderByEstudiantes();


    public List<Estudiante> FindAllEstudianteFilter(carrera C)
}
