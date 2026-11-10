package com.microserviciocliente.repository;

import com.microserviciocliente.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {
    Optional<NotificacionEntity> findByIdEventoAndIdCliente(String idEvento, String idCliente);
    List<NotificacionEntity> findByIdClienteOrderByFechaCreacionDesc(String idCliente);
}
