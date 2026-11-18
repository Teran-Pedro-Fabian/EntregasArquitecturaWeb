package com.microserviciocliente.dto;

import lombok.Data;

@Data
public class LugarEntityDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private Double longitud;
    private Double latitud;
}
