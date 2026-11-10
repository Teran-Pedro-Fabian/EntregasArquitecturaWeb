package com.microServicioEvento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "evento")
public class EventoEntity {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;
    private String nombre;
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    private String tipo;
    private String organizador;
    @Column(name = "fecha_hora", length = 40)
    private String fechaHora;
    @Column(name = "id_lugar")
    private Long idLugar;
    // BORRADOR, PUBLICADO, SUSPENDIDO, AGOTADO, CANCELADO, FINALIZADO
    private String estado;
    private String demanda;

    @PrePersist
    public void generarId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
