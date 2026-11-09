package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lugar")
public class LugarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private Double longitud;
    private Double latitud;

    public LugarEntity(String estadioArena, String s, double v, double v1) {
        this.nombre = estadioArena;
        this.direccion = s;
        this.longitud = v;
        this.latitud = v1;
    }
}