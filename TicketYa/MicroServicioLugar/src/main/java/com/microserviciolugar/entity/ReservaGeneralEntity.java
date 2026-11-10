package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "reserva_general")
public class ReservaGeneralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector", nullable = false)
    private SectorEntity sector;

    private String idCliente;
    private Integer cantidad;
    private String estado = "RESERVADA";
    private Instant reservaHasta;
    private Long idCompra;
}
