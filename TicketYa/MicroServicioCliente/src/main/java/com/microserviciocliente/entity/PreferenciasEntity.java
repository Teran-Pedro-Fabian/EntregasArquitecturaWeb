package com.microserviciocliente.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="preferencias")
public class PreferenciasEntity {
    @Id
    private String id;

    private String idCliente;
    private String idEvento;
    private String idParticipante;
    private String idLugar;

}
