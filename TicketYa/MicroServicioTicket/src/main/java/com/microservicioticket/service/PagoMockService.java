package com.microservicioticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PagoMockService {

    @Value("${ticketya.pagos.mock.reintegro-aprobado:true}")
    private boolean reintegroAprobado;

    /**
     * Simular el reintegro de una entrada
     * Devolver si la pasarela aprobo o rechazo
     *
     * @param idTicket ID del ticket
     * @param monto monto a reintegrar
     * @return true si se aprobo el reintegro
     */
    public boolean solicitarReintegro(Long idTicket, Double monto) {
        // 1) Verificar los datos del reintegro
        if (idTicket == null || monto == null || monto < 0) {
            throw new RuntimeException("Datos del reintegro no validos");
        }
        // 2) Simular la respuesta de la pasarela de pagos
        return reintegroAprobado;
    }
}