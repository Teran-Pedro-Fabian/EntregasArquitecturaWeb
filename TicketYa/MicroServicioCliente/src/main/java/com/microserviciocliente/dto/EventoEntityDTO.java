package com.microserviciocliente.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class EventoEntityDTO {
    
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String organizador;
    private String fechaHora;
    private Long idLugar;
    private String estado;
    private String demanda;
    private Double precioDesde;

}
