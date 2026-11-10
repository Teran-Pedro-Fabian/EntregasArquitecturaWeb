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

    // NUMERADO: cada asiento tiene su propia reserva
    // NO_NUMERADO: se administra el stock del sector
    private String tipo = "NUMERADO";

    @Column(name = "cupos_reservados")
    private Integer cuposReservados = 0;

    @Column(name = "cupos_vendidos")
    private Integer cuposVendidos = 0;
    @Column(name = "id_evento", length = 64)
    private String idEvento;
}
