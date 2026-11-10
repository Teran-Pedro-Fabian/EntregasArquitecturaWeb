package com.microServicioEvento.dto;

import lombok.Data;

/** Parametros opcionales de GET /evento/buscar. */
@Data
public class BusquedaEventoFiltro {
    private String tipo;
    private String organizador;
    private String participante;
    private String ciudad;
    private String fechaDesde; // yyyy-MM-dd, inclusive
    private String fechaHasta; // yyyy-MM-dd, inclusive
    private String texto; // Busca en titulo, descripcion, tipo, organizador y participantes
    private String q; // Alias opcional de texto
    private Double lat;
    private Double lon;
    private String ordenarPor = "fecha"; // fecha, precio, relevancia
}
