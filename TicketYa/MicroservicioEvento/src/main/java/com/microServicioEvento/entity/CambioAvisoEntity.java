package com.microServicioEvento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Un cambio de fecha o descripcion debe avisarse aunque haya cambios posteriores */
@Entity
@Table(name = "aviso_cambio_evento")
public class CambioAvisoEntity {
    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "id_evento", nullable = false, length = 64)
    private String idEvento;
    @Column(nullable = false, length = 500)
    private String mensaje;
    @Column(nullable = false)
    private boolean entregado;
    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    public CambioAvisoEntity() {}
    public CambioAvisoEntity(String idEvento, String mensaje) {
        this.id = UUID.randomUUID().toString();
        this.idEvento = idEvento;
        this.mensaje = mensaje;
        this.entregado = false;
        this.fechaCreacion = Instant.now();
    }
    public String getId() { return id; }
    public String getIdEvento() { return idEvento; }
    public String getMensaje() { return mensaje; }
    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }
    public Instant getFechaCreacion() { return fechaCreacion; }
}
