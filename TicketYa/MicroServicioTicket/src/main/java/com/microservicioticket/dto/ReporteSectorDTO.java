package com.microservicioticket.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReporteSectorDTO {
    private Long idSector;
    private String nombre;
    private String tipo;
    private long capacidad;
    private long entradasVendidas;
    private long entradasDevueltas;
    private long entradasActivas;
    private BigDecimal ocupacionPorcentaje = BigDecimal.ZERO;
    private BigDecimal recaudacionBruta = BigDecimal.ZERO;
    private BigDecimal reintegrosRealizados = BigDecimal.ZERO;
    private BigDecimal recaudacionNeta = BigDecimal.ZERO;
}
