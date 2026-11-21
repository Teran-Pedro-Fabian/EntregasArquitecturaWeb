package com.microserviciolugar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectorPublicoDTO {
    private Long idSector;
    private Long idLugar;
    private String idEvento;
    private String nombre;
    private String tipo;
    private Double precio;
    private long capacidad;
}
