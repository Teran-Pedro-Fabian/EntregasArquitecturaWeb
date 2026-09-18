package com.segundaentrega.Entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;






@Entity 
@Table(name = "estudiante")
@AllArgsConstructor 
@Getter 
@Setter 
@NoArgsConstructor 
public class EstudianteEntity {
    
    @Id 
    @Column(name = "DNI")
    private int DNI;

    
    private String nombre;


    private String apellido;


    private int edad;


    private String genero;


    private String ciudad;


    private int LU;

    @OneToMany
    (mappedBy="estudiante")
    private List<estudainteCarrera> Carreras;


}
