package com.microserviciocliente.dto;

/** Aviso que otro microservicio envia a Cliente */
public class NotificacionRequest {
    private String idEvento;
    private String idCliente;
    private String referencia;
    private String mensaje;

    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
