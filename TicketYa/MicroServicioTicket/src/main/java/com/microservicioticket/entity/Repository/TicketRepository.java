package com.microservicioticket.repository;

import com.microservicioticket.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
}
