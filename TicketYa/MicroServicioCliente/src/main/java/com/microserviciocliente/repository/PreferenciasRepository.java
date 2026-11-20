package com.microserviciocliente.repository;

import com.microserviciocliente.entity.PreferenciasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferenciasRepository extends JpaRepository<PreferenciasEntity, String> {

    List<PreferenciasEntity> findByIdCliente(String idCliente);

    List<PreferenciasEntity> findByIdParticipanteIn(List<String> idsParticipantes);
}
