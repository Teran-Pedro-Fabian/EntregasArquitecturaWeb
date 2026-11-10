package com.microservicioticket.Repository;

import com.microservicioticket.entity.AvisoCancelacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvisoCancelacionRepository extends JpaRepository<AvisoCancelacionEntity, Long> {
    boolean existsByIdEventoAndIdCliente(String idEvento, String idCliente);
    List<AvisoCancelacionEntity> findByIdClienteOrderByFechaCreacionDesc(String idCliente);
    long countByIdEvento(String idEvento);
}
