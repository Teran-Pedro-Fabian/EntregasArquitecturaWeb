package com.microserviciocliente.repository;

import com.microserviciocliente.entity.PreferenciasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PreferenciasRepository extends JpaRepository<PreferenciasEntity, String> {
    // Se conserva el nombre usado por PreferenciasService
    List<PreferenciasEntity> findByidCliente(String idCliente);
}
