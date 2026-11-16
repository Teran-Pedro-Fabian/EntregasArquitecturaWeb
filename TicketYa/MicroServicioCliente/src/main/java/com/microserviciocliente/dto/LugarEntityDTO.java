package com.microserviciocliente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LugarEntityDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Double longitud;
    private Double latitud;
    // Ciudad para el filtro del catalogo. Los lugares antiguos pueden tenerla vacia.
    private String ciudad;
     }