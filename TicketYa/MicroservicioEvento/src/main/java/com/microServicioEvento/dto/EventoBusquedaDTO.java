package com.microServicioEvento.dto;

import com.microServicioEvento.entity.EventoEntity;
import lombok.Data;

/** Respuesta publica: datos del evento mas precio minimo de sus sectores. */
@Data
public class EventoBusquedaDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String organizador;
    private String fechaHora;
    private Long idLugar;
    private String estado;
    private String demanda;
    private Double precioDesde;

    public EventoBusquedaDTO(EventoEntity evento, Double precioDesde) {
        this.id = evento.getId();
        this.nombre = evento.getNombre();
        this.descripcion = evento.getDescripcion();
        this.tipo = evento.getTipo();
        this.organizador = evento.getOrganizador();
        this.fechaHora = evento.getFechaHora();
        this.idLugar = evento.getIdLugar();
        this.estado = evento.getEstado();
        this.demanda = evento.getDemanda();
        this.precioDesde = precioDesde;
    }
}
