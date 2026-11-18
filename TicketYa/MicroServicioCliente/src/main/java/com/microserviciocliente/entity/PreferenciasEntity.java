package com.microserviciocliente.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "preferencias")
public class PreferenciasEntity {
    @Id
    private String id;

    private String idCliente;
    private String idEvento;
    private String idParticipante;
    private String idLugar;
    // Ciudad favorita para los avisos de nuevos eventos
    private String ciudad;
    // Tipo favorito, por ejemplo RECITAL o TEATRO
    private String tipo;

    // En MongoDB no se ejecuta el @PrePersist de JPA
    public void generarId() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
