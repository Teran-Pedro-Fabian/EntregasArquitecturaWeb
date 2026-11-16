package com.microservicioticket.dto;

import lombok.Data;

/**
 * Solicitud para ingresar a la fila virtual de un evento
 * El cliente indica el evento al que quiere comprar
 */
@Data
public class ColaRequest {
    private String idCliente;
}
