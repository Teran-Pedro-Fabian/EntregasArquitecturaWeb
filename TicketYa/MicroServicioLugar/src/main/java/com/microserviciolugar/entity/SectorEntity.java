package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sector")
public class SectorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_lugar")
    private LugarEntity lugar;

    private String nombre;
    private Integer cupos;
    private Double precio;
    private Long idEvento;
}
