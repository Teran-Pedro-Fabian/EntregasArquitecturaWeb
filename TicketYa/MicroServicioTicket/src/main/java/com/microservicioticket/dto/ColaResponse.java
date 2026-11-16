package com.microservicioticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Posicion de un cliente dentro de la fila virtual de un evento
 * posicion 1 = primero en la fila, habilitado indica si le toca comprar ahora
 */
@Data
@AllArgsConstructor
public class ColaResponse {
    private String idEvento;
    private String idCliente;
    private Integer posicion;
    private Integer total;
    private Boolean habilitado;
}
