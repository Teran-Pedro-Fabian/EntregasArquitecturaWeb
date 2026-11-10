package com.Repositorys;

import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteCarreraId;
import com.Entitys.EstudianteEntity;
import com.dtos.ConteoCarreraAnualDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EstudianteCarreraRepository
        extends JpaRepository<EstudianteCarrera, EstudianteCarreraId> {

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.carrera.id = :idCarrera
            """)
    List<EstudianteCarrera> findByIdCarrera(@Param("idCarrera") int idCarrera);

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            """)
    List<EstudianteCarrera> FindAll();

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.estudiante.DNI = :idEstudiante
            """)
    List<EstudianteCarrera> findByEstudiante(@Param("idEstudiante") int idEstudiante);

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            ORDER BY ec.estudiante.DNI
            """)
    List<EstudianteCarrera> FindAllOrderByEstudiantes();

    @Query("""
            SELECT ec
            FROM EstudianteCarrera ec
            WHERE ec.carrera.id = :idCarr
            """)
    List<EstudianteCarrera> FindAllEstudianteFilterCarrera(@Param("idCarr") int idCarr);

    // Recuperar estudiantes por carrera y ciudad
    @Query("""
            SELECT e
            FROM EstudianteCarrera ec
            JOIN ec.estudiante e
            JOIN ec.carrera c
            WHERE c.id = :idCarrera
            AND e.ciudad = :ciudad
            """)
    List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad(@Param("idCarrera") int idCarrera, @Param("ciudad") String ciudad);

    // Contar inscriptos por carrera y anio
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
    List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio();

    // Contar egresados por carrera y anio
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
    List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio();

    // Verificar si existe una inscripcion
    @Query("""
            SELECT CASE WHEN COUNT(ec) > 0 THEN true ELSE false END
            FROM EstudianteCarrera ec
            WHERE ec.estudiante.DNI = :dni
            AND ec.carrera.id = :idCarrera
            """)
    boolean existeInscripcion(@Param("dni") int dni, @Param("idCarrera") int idCarrera);
}
