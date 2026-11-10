package com.microserviciolugar.service;

import com.microserviciolugar.entity.ReservaGeneralEntity;
import com.microserviciolugar.repository.ReservaGeneralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
public class LimpiezaReservaGeneral {

    @Autowired private ReservaGeneralRepository reservaRepository;
    @Autowired private ReservaGeneralService reservaGeneralService;

    // Un lote cada 30 segundos. Cada reserva usa una transaccion independiente
    @Scheduled(fixedDelayString = "${ticketya.reservas.limpieza-ms:30000}",
            initialDelayString = "${ticketya.reservas.limpieza-ms:30000}")
    public void liberarVencidas() {
        List<ReservaGeneralEntity> vencidas = reservaRepository
                .findTop100ByEstadoAndReservaHastaBeforeOrderByReservaHastaAsc("RESERVADA", Instant.now());
        for (ReservaGeneralEntity reserva : vencidas) {
            reservaGeneralService.expirarReserva(reserva.getId());
        }
    }
}
