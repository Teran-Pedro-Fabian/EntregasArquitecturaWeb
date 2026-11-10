package com.microserviciolugar.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "venta_general")
public class VentaGeneralEntity {

    // Ticket unico de la venta. Evita registrar el mismo ticket dos veces
    @Id
    private Long idTicket;

    @Column(name = "id_sector", nullable = false)
    private Long idSector;

    @Column(name = "id_reserva_general", nullable = false)
    private Long idReservaGeneral;

    private String estado = "VENDIDO";
}
