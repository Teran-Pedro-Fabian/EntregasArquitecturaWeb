package com.microserviciocliente.entity;

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
@Table(name = "preferencias")
public class PreferenciasEntity {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "id_cliente")
    private String idCliente;
    @Column(name = "id_evento")
    private String idEvento;
    @Column(name = "id_participante")
    private String idParticipante;
    @Column(name = "id_lugar")
    private String idLugar;

    @PrePersist
    public void generarId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
