package com.microserviciocliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cliente")
public class ClienteEntity {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "nombre")
    private String nombre;

    // Destinatario de las notificaciones por correo
    @Column(name = "email", length = 254)
    private String email;

    // Nunca devolver el hash al consumidor de la API
    @JsonIgnore
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    // Solo se recibe al registrar un cliente nuevo
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // los ID heredados de Mongo se conservan al importar
    @PrePersist
    public void generarId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
