package com.microservicioticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColaResponse {
    private String idEvento;
    private String idCliente;
    private Integer posicion;
    private Integer total;
    private Boolean habilitado;
}
