package com.segundaentrega.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.segundaentrega.Entitys.EstudianteEntity;

public interface EstudianteRepository
       extends JpaRepository<EstudianteEntity, Integer> {

       @Query("""
              SELECT e
              FROM EstudianteEntity e
              ORDER BY e.DNI
              """)
       public List<EstudianteEntity> FindAllOrderByDNI();

       @Query ("""
              SELECT e
              FROM EstudianteEntity e
              WHERE e.LU = :libreta
              """)
       public EstudianteEntity FindByNumeroDeLibreta(@Param("libreta") int libreta);


       @Query("""
              SELECT e 
              FROM EstudianteEntity e
              WHERE e.genero = :genero
              """)
       public List<EstudianteEntity> FindByGerero(@Param("genero") String genero);
       

       @Query ("""
                     SELECT e
                     FROM EstudianteEntity e
                     ORDER BY e.ciudad
                     """)
       public List<EstudianteEntity> findAllEstudianteOrderByResidencia();




}
