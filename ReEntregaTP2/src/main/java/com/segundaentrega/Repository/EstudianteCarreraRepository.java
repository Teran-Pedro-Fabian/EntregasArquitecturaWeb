package com.segundaentrega.Repository;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.segundaentrega.Entitys.EstudianteCarrera;
import com.segundaentrega.Entitys.EstudianteCarreraId;
import com.segundaentrega.Entitys.EstudianteEntity;
import com.segundaentrega.dto.ConteoCarreraAnualDTO;

public class EstudianteCarreraRepository {

    private final EntityManager em;

    public EstudianteCarreraRepository(EntityManager em) {
        this.em = em;
    }

    // Buscar inscripciones por carrera
    public List<EstudianteCarrera> FindByIdCarrera(int idCarrera) {
        TypedQuery<EstudianteCarrera> query = em.createQuery(
                "SELECT ec FROM EstudianteCarrera ec " +
                        "WHERE ec.carrera.id = :idCarrera",
                EstudianteCarrera.class
        );
        query.setParameter("idCarrera", idCarrera);
        return query.getResultList();
    }

    // Recuperar todas las inscripciones
    public List<EstudianteCarrera> FindAll() {
        TypedQuery<EstudianteCarrera> query = em.createQuery(
                "SELECT ec FROM EstudianteCarrera ec",
                EstudianteCarrera.class
        );
        return query.getResultList();
    }

    // Buscar inscripciones de un estudiante
    public List<EstudianteCarrera> findByEstudiante(int idEstudiante) {
        TypedQuery<EstudianteCarrera> query = em.createQuery(
                "SELECT ec FROM EstudianteCarrera ec " +
                        "WHERE ec.estudiante.DNI = :idEstudiante",
                EstudianteCarrera.class
        );
        query.setParameter("idEstudiante", idEstudiante);
        return query.getResultList();
    }

    // Insertar inscripción
    public void Insert(EstudianteCarrera estCarr) {
        try {
            em.getTransaction().begin();
            em.persist(estCarr);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    // Actualizar inscripción existente
    public void Update(EstudianteCarrera estCarr) {
        try {
            em.getTransaction().begin();
            em.merge(estCarr);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    // Eliminar inscripción por clave compuesta
    public void delete(EstudianteCarreraId id) {
        try {
            em.getTransaction().begin();
            EstudianteCarrera ec = em.find(EstudianteCarrera.class, id);
            if (ec != null) {
                em.remove(ec);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    // Recuperar inscripciones ordenadas por DNI
    public List<EstudianteCarrera> FindAllOrderByEstudiantes() {
        TypedQuery<EstudianteCarrera> query = em.createQuery(
                "SELECT ec FROM EstudianteCarrera ec " +
                        "ORDER BY ec.estudiante.DNI",
                EstudianteCarrera.class
        );
        return query.getResultList();
    }

    // Filtrar inscripciones por carrera
    public List<EstudianteCarrera> FindAllEstudianteFilterCarrera(int idCarr) {
        TypedQuery<EstudianteCarrera> query = em.createQuery(
                "SELECT ec FROM EstudianteCarrera ec " +
                        "WHERE ec.carrera.id = :idCarr",
                EstudianteCarrera.class
        );
        query.setParameter("idCarr", idCarr);
        return query.getResultList();
    }
    // Recuperar estudiantes de una carrera y ciudad
    public List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad(
            int idCarrera, String ciudad) {

        TypedQuery<EstudianteEntity> query = em.createQuery(
                "SELECT e FROM EstudianteCarrera ec " +
                        "JOIN ec.estudiante e " +
                        "WHERE ec.carrera.id = :idCarrera " +
                        "AND e.ciudad = :ciudad",
                EstudianteEntity.class
        );
        query.setParameter("idCarrera", idCarrera);
        query.setParameter("ciudad", ciudad);
        return query.getResultList();
    }

    // Contar inscriptos por carrera y año
    public List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio() {
        TypedQuery<ConteoCarreraAnualDTO> query = em.createQuery(
                "SELECT NEW com.segundaentrega.dto.ConteoCarreraAnualDTO(" +
                        "ec.carrera.carrera, ec.inscripcion, COUNT(ec)) " +
                        "FROM EstudianteCarrera ec " +
                        "GROUP BY ec.carrera.carrera, ec.inscripcion " +
                        "ORDER BY ec.carrera.carrera ASC, ec.inscripcion ASC",
                ConteoCarreraAnualDTO.class
        );
        return query.getResultList();
    }

    // Contar egresados por carrera y año
    public List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio() {
        TypedQuery<ConteoCarreraAnualDTO> query = em.createQuery(
                "SELECT NEW com.segundaentrega.dto.ConteoCarreraAnualDTO(" +
                        "ec.carrera.carrera, ec.graduacion, COUNT(ec)) " +
                        "FROM EstudianteCarrera ec " +
                        "WHERE ec.graduacion <> 0 " +
                        "GROUP BY ec.carrera.carrera, ec.graduacion " +
                        "ORDER BY ec.carrera.carrera ASC, ec.graduacion ASC",
                ConteoCarreraAnualDTO.class
        );
        return query.getResultList();
    }
}
