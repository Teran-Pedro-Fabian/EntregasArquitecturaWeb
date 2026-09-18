package com.segundaentrega.Repository;

import com.segundaentrega.Entitys.EstudianteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
    public EstudianteEntity FindByNumeroDeLibreta(@Param(libreta) int libreta);




}
