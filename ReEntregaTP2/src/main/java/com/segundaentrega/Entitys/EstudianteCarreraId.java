
package com.segundaentrega.Entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EstudianteCarreraId implements Serializable {

    @Column(name = "id_estudiante")
    private int idEstudiante;

    @Column(name = "id_carrera")
    private int idCarrera;
}