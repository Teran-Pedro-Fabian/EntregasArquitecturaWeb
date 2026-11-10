package com.microServicioEvento.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "evento")
public class EventoEntity {
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String organizador;
    private String fechaHora;
    private Long idLugar;
    // BORRADOR, PUBLICADO, SUSPENDIDO, AGOTADO, CANCELADO, FINALIZADO
    private String estado;
    // BAJA o ALTA (la fila virtual corresponde a la Entrega 2)
    private String demanda;
}
