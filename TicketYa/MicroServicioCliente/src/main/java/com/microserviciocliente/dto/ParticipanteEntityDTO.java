package com.microserviciocliente.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor
@NoArgsConstructor
public class ParticipanteEntityDTO {
    private String id;
    private String nombre;
}
