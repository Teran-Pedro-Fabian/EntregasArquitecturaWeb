package com.segundaentrega.Repository;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.segundaentrega.Entitys.EstudianteEntity;

public class EstudianteRepository {
       private final EntityManager em;
       public EstudianteRepository(EntityManager em) {
              this.em = em;
       }

       // Recuperar estudiantes ordenados por DNI
       public List<EstudianteEntity> FindAllOrderByDNI() {
              TypedQuery<EstudianteEntity> query = em.createQuery(
                      "SELECT e FROM EstudianteEntity e " +
                              "ORDER BY e.DNI",
                      EstudianteEntity.class
              );
              return query.getResultList();
       }

       // Buscar estudiante por numero de libreta
       public EstudianteEntity FindByNumeroDeLibreta(int libreta) {
              TypedQuery<EstudianteEntity> query = em.createQuery(
                      "SELECT e FROM EstudianteEntity e " +
                              "WHERE e.LU = :libreta",
                      EstudianteEntity.class
              );
              query.setParameter("libreta", libreta);
              List<EstudianteEntity> estudiantes = query.getResultList();
              if (estudiantes.isEmpty()) {
                     return null;
              }
              return estudiantes.get(0);
       }

       // Recuperar estudiantes por genero
       public List<EstudianteEntity> FindByGenero(String genero) {
              TypedQuery<EstudianteEntity> query = em.createQuery(
                      "SELECT e FROM EstudianteEntity e " +
                              "WHERE e.genero = :genero",
                      EstudianteEntity.class
              );
              query.setParameter("genero", genero);
              return query.getResultList();
       }

       // Recuperar estudiantes ordenados por ciudad
       public List<EstudianteEntity> findAllEstudianteOrderByResidencia() {
              TypedQuery<EstudianteEntity> query = em.createQuery(
                      "SELECT e FROM EstudianteEntity e " +
                              "ORDER BY e.ciudad",
                      EstudianteEntity.class
              );
              return query.getResultList();
       }
}
