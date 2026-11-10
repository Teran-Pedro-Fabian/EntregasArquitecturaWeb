package com.microservicioticket.Repository;

import com.microservicioticket.entity.TicketEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    /**
     * Buscar un ticket y bloquearlo mientras se revisa
     *
     * @param id ID del ticket
     * @return ticket encontrado
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketEntity t WHERE t.id = :id")
    Optional<TicketEntity> findByIdParaRevision(@Param("id") Long id);
}