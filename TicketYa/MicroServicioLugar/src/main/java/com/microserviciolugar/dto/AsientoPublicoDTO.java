package com.microserviciolugar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsientoPublicoDTO {
    private Long idAsiento;
    private Long idSector;
    private String fila;
    private Integer columna;
    private String estado;
}
