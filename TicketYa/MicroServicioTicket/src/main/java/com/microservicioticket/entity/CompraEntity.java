package com.microservicioticket.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * Registrar el detalle de una compra aprobada
 * Un pedido puede generar varios tickets
 */
@Data
@Entity
@Table(name = "compra")
public class CompraEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idCliente;
    private String idEvento;
    private Long idSector;
    @Column(name = "id_reserva_general")
    private Long idReservaGeneral;
    private Integer cantidadEntradas;
    private Double total;
    private String medioPago;
    private String estado;
    private Instant fechaCompra;
}
