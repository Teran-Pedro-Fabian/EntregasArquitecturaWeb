package com.microserviciocliente.dto;

import java.util.List;

/** Datos de un evento publicado, enviados desde Evento */
public class NotificacionPublicacionRequest {
    private String idEvento;
    private String nombreEvento;
    private String ciudad;
    private String idLugar;
    private List<String> idsParticipantes;
    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }
    public String getNombreEvento() { return nombreEvento; }
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getIdLugar() { return idLugar; }
    public void setIdLugar(String idLugar) { this.idLugar = idLugar; }
    public List<String> getIdsParticipantes() { return idsParticipantes; }
    public void setIdsParticipantes(List<String> idsParticipantes) { this.idsParticipantes = idsParticipantes; }
}
