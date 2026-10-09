package com.Repositorys;

import java.util.List;

import com.dtos.ConteoCarreraAnualDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteEntity;

public interface EstudianteCarreraRepository extends JpaRepository<EstudianteCarrera, Integer> {

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.carrera.id = :idCarrera
            """)
    public List<EstudianteCarrera> findByIdCarrera(@Param("idCarrera") int idCarrera);


    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            """)
    public List<EstudianteCarrera> FindAll();


    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.estudiante.DNI = :idEstudiante
            """)
    public List<EstudianteCarrera> findByEstudiante(@Param("idEstudiante") int idEstudiante);


    @Modifying
    @Transactional
    @Query("""
            INSERT INTO EstudianteCarrera(id, estudiante, carrera, inscripcion, graduacion, antiguedad)
            VALUES (:#{#estCarr.id}, :#{#estCarr.estudiante}, :#{#estCarr.carrera},
                    :#{#estCarr.inscripcion}, :#{#estCarr.graduacion}, :#{#estCarr.antiguedad})
            """)
    public void Insert(@Param("estCarr") EstudianteCarrera estCarr);


    @Modifying
    @Transactional
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


    @Modifying
    @Transactional
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

    /**
     * Recupera estudiantes de una carrera determinada
     * filtra los estudiantes por ciudad de residencia
     */
    @Query("""
        SELECT e
        FROM EstudianteCarrera ec
        JOIN ec.estudiante e
        JOIN ec.carrera c
        WHERE c.id = :idCarrera
        AND e.ciudad = :ciudad
        """)
    public List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad(
            @Param("idCarrera") int idCarrera,
            @Param("ciudad") String ciudad
    );

    /**
     * Punto 3
     * Cuenta inscriptos agrupados por carrera y por año de inscripción.
     * Devuelve resultados ordenados por nombre de carrera ASC y año ASC.
     */
    @Query("""
        SELECT NEW com.dtos.ConteoCarreraAnualDTO(
            ec.carrera.carrera,
            ec.inscripcion,
            COUNT(ec)
        )
        FROM EstudianteCarrera ec
        GROUP BY ec.carrera.carrera, ec.inscripcion
        ORDER BY ec.carrera.carrera ASC, ec.inscripcion ASC
        """)
    public List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio();

    /**
     * Punto 3
     * Cuenta egresados agrupados por carrera y por año de graduación.
     * Excluye los registros con graduacion = 0 (no egresados).
     * Devuelve resultados ordenados por nombre de carrera ASC y año ASC.
     */
    @Query("""
        SELECT NEW com.dtos.ConteoCarreraAnualDTO(
            ec.carrera.carrera,
            ec.graduacion,
            COUNT(ec)
        )
        FROM EstudianteCarrera ec
        WHERE ec.graduacion <> 0
        GROUP BY ec.carrera.carrera, ec.graduacion
        ORDER BY ec.carrera.carrera ASC, ec.graduacion ASC
        """)
    public List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio();

    /**
     * Verificar si un estudiante ya esta inscripto en una carrera
     * utilizando el DNI y el ID de la carrera
     */
    @Query("""
    SELECT CASE WHEN COUNT(ec) > 0 THEN true ELSE false END
    FROM EstudianteCarrera ec
    WHERE ec.estudiante.DNI = :dni
    AND ec.carrera.id = :idCarrera
    """)
    public boolean existeInscripcion(@Param("dni") int dni, @Param("idCarrera") int idCarrera);
}
