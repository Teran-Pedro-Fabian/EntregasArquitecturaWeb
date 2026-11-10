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
    private String tipo; // Recital, Teatro, etc.
    private String organizador;
    private String fechaHora;
    private Long idLugar; // Vínculo lógico al MicroServicioLugar
    private String estado; // BORRADOR, PUBLICADO, AGOTADO, CANCELADO
    private String demanda; // BAJA, ALTA
}
