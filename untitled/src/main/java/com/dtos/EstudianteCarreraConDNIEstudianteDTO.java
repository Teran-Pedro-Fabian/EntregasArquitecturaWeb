package com.dtos;

import com.Entitys.EstudianteCarrera;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstudianteCarreraConDNIEstudianteDTO {
    private int DNIEstudiante;
    private EstudianteCarrera estudianteCarrera;



}
