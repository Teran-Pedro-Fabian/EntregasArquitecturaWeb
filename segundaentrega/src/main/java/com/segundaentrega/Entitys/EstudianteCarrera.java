package com.segundaentrega.Entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estudiante_carrera")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class EstudianteCarrera {

    @Id
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_estudiante")
    private EstudianteEntity estudiante;

    @ManyToOne
    @JoinColumn(name = "id_carrera")
    private CarreraEntity carrera;

    private int inscripcion;

    private int graduacion;

    private int antiguedad;
}