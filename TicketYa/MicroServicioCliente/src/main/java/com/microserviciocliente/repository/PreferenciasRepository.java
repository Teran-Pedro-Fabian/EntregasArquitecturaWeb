package com.microserviciocliente.repository;

import com.microserviciocliente.entity.PreferenciasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PreferenciasRepository extends JpaRepository<PreferenciasEntity, String> {
    
    @Query ("SELECT p FROM PreferenciasEntity p WHERE p.cliente.id = :idCliente")
    List<PreferenciasEntity> findByidCliente(String idCliente);
}
