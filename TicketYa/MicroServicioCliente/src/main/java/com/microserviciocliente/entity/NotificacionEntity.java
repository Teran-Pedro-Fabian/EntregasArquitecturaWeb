package com.microserviciocliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Aviso persistente: compra, cancelacion, cambio o artista favorito */
@Entity
@Table(name = "notificacion_cliente")
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_evento", nullable = false, length = 64)
    private String idEvento;

    @Column(name = "id_cliente", nullable = false, length = 64)
    private String idCliente;

    @Column(name = "tipo", nullable = false, length = 32)
    private String tipo = "CANCELACION";

    // Compra: idCompra, cambio: UUID, cancelacion/publicacion: evento
    @Column(name = "referencia", nullable = false, length = 128)
    private String referencia = "evento";

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "estado_email", nullable = false, length = 20)
    private String estadoEmail = "PENDIENTE";

    @Column(name = "intentos_email", nullable = false)
    private int intentosEmail;

    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    public Long getId() { return id; }
    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getEstadoEmail() { return estadoEmail; }
    public void setEstadoEmail(String estadoEmail) { this.estadoEmail = estadoEmail; }
    public int getIntentosEmail() { return intentosEmail; }
    public void setIntentosEmail(int intentosEmail) { this.intentosEmail = intentosEmail; }
    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
