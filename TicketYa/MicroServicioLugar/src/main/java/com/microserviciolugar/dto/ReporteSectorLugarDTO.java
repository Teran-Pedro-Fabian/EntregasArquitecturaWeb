package com.microserviciolugar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteSectorLugarDTO {
    private Long idSector;
    private String nombre;
    private String tipo;
    private long capacidad;
}
