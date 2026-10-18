package com.segundaentrega.Repository;

import com.segundaentrega.Entitys.CarreraEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class CarreraRepository {
    private final EntityManager em;
    public CarreraRepository(EntityManager em) {
        this.em = em;
    }

    // Actualizar una carrera existente
    public int UpdateCarrera(CarreraEntity carrera) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CarreraEntity existente = em.find(CarreraEntity.class, carrera.getId());
            if (existente == null) {
                tx.commit();
                return 0;
            }
            existente.setCarrera(carrera.getCarrera());
            existente.setDuracion(carrera.getDuracion());
            tx.commit();
            return 1;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // Insertar una carrera
    public int InsertCarrera(CarreraEntity carrera) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(carrera);
            tx.commit();
            return 1;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Recupera las carreras que tienen estudiantes inscriptos
     * agrupa por carrera
     * cuenta la cantidad de inscriptos
     * ordena de mayor a menor
     */
    public List<CarreraEntity> FindCarrerasOrderByInscriptos() {
        TypedQuery<CarreraEntity> query = em.createQuery(
                "SELECT c " +
                        "FROM CarreraEntity c " +
                        "JOIN c.estudiantes ec " +
                        "GROUP BY c " +
                        "ORDER BY COUNT(ec) DESC",
                CarreraEntity.class
        );
        return query.getResultList();
    }
}