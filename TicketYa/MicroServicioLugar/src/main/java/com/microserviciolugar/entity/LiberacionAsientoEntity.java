package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "liberacion_asiento")
public class LiberacionAsientoEntity {

    // Ticket que genero la liberacion
    @Id
    @Column(name = "id_ticket")
    private Long idTicket;

    // Asiento que fue liberado
    @Column(name = "id_asiento", nullable = false)
    private Long idAsiento;

    // Fecha en la que se libero el asiento
    @Column(name = "fecha_liberacion", nullable = false)
    private Instant fechaLiberacion;
}
