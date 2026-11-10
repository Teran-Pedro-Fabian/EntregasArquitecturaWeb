package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

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

    // Cliente que tiene reservado el asiento
    @Column(name = "cliente_reserva")
    private String idClienteReserva;

    // Fecha y hora de vencimiento de la reserva
    @Column(name = "reserva_hasta")
    private Instant reservaHasta;

    // Ticket que tiene vendido el asiento
    @Column(name = "id_ticket_vendido")
    private Long idTicketVendido;
}
