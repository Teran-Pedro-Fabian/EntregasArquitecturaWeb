package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "asiento")
public class AsientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_sector")
    private SectorEntity sector;

    private String fila;
    private Integer columna;


    private String estado = "DISPONIBLE";
}