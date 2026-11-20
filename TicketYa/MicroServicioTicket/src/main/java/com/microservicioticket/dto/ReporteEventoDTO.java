package com.microservicioticket.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReporteEventoDTO {
    private String idEvento;
    private String nombreEvento;
    private String organizador;
    private List<ReporteSectorDTO> sectores = new ArrayList<>();
    private long capacidadTotal;
    private long entradasVendidas;
    private long entradasDevueltas;
    private long entradasActivas;
    private BigDecimal ocupacionPorcentaje = BigDecimal.ZERO;
    private BigDecimal recaudacionBruta = BigDecimal.ZERO;
    private BigDecimal reintegrosRealizados = BigDecimal.ZERO;
    private BigDecimal recaudacionNeta = BigDecimal.ZERO;
}
