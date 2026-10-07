package com.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReporteCarreraAnualDTO {

    private String carrera;
    private int anio;
    private long inscriptos;
    private long egresados;

    @Override
    public String toString() {
        return String.format("%-40s | %-6d | inscriptos: %-6d | egresados: %-6d",
                carrera, anio, inscriptos, egresados);
    }
}
