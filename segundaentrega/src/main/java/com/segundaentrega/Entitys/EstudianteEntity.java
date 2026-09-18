package com.segundaentrega.Entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


import java.util.List;



@Entity 
public class EstudianteEntity {
    @Id 
    private int id;

    private String nombre;

    private String apellido;

    private int edad;

    private String genero;

    private String ciudad;

    private String LU;

    private List<CarreraEntity> Carreras;


}
