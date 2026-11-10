package com.microserviciocliente.dto;

/** Datos que envia Ticket cuando se cancela un evento */
public class NotificacionCancelacionRequest {
    private String idEvento;
    private String idCliente;
    private String mensaje;

    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
