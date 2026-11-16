package com.microserviciocliente.repository;

import com.microserviciocliente.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {
    Optional<NotificacionEntity> findByTipoAndReferenciaAndIdEventoAndIdCliente(
            String tipo, String referencia, String idEvento, String idCliente);
    List<NotificacionEntity> findByIdClienteOrderByFechaCreacionDesc(String idCliente);
    List<NotificacionEntity> findTop50ByEstadoEmailInAndIntentosEmailLessThanOrderByFechaCreacionAsc(
            List<String> estados, int intentos);
    List<NotificacionEntity> findByIdClienteAndEstadoEmail(String idCliente, String estadoEmail);
}
