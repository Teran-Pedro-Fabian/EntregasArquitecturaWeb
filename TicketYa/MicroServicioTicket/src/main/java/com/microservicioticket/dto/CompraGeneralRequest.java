package com.microservicioticket.dto;

import lombok.Data;

@Data
public class CompraGeneralRequest {
    private String idCliente;
    private String idEvento;
    private Long idSector;
    private Long idReserva;
    private Integer cantidad;
    private String medioPago;
}
