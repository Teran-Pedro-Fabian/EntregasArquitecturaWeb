package com.microservicioticket.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Aviso de cancelacion disponible para consultar por cliente
 * Un aviso por cliente y evento para no repetir notificaciones
 */
@Entity
@Table(name = "aviso_cancelacion", uniqueConstraints = @UniqueConstraint(name = "uq_aviso_evento_cliente", columnNames = {
        "id_evento", "id_cliente" }))
public class AvisoCancelacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_evento", nullable = false)
    private String idEvento;

    @Column(name = "id_cliente", nullable = false)
    private String idCliente;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private Instant fechaCreacion;

    // Nulo hasta que Cliente guarda la notificacion
    @Column(name = "fecha_entrega")
    private Instant fechaEntrega;

    public Long getId() {
        return id;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
}
