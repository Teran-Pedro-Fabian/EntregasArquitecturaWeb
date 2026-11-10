package com.microServicioEvento.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "participante_evento")
public class EventoParticipanteEntity {
    @Id
    private String id;
    private String idEvento;
    private String idParticipante;
}