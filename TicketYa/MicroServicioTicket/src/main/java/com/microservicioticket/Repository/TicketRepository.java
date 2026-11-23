package com.microservicioticket.Repository;

import com.microservicioticket.entity.TicketEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByIdCompra(Long idCompra);

    // entradas del cliente ordenadas desde la mas nueva
    List<TicketEntity> findByIdClienteOrderByIdDesc(String idCliente);
    List<TicketEntity> findByEstadoDevolucionOrderByIdDesc(String estadoDevolucion);
    List<TicketEntity> findByEstadoDevolucionNotOrderByIdDesc(String estadoDevolucion);

    // Recuperar todos los tickets de un evento para cancelacion masiva
    List<TicketEntity> findByIdEventoOrderByIdAsc(String idEvento);

    /**
     * Contar tickets activos de un cliente en un evento
     * Las devoluciones completadas no ocupan el limite
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t " +
            "WHERE t.idCliente = :idCliente AND t.idEvento = :idEvento " +
            "AND (t.estadoDevolucion IS NULL OR t.estadoDevolucion <> 'DEVUELTA')")
    long contarActivos(@Param("idCliente") String idCliente,
                       @Param("idEvento") String idEvento);

    /**
     * Buscar un ticket y bloquearlo mientras se revisa
     *
     * @param id ID del ticket
     * @return ticket encontrado
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketEntity t WHERE t.id = :id")
    Optional<TicketEntity> findByIdParaRevision(@Param("id") Long id);

    // bloquear el ticket por QR para impedir ingresos simultaneos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketEntity t WHERE t.qrCode = :qrCode")
    Optional<TicketEntity> findByQrCodeParaRevision(@Param("qrCode") String qrCode);

}