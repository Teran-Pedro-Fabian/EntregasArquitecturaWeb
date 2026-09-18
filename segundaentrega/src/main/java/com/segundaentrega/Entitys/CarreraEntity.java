package com.segundaentrega.Entitys;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;





@Entity 
@Table(name = "carrera")
@AllArgsConstructor 
@Getter 
@Setter 
@NoArgsConstructor 
public class CarreraEntity {

    @Id 
    @Column(name="id_carrera")
    private int id;

    private String carrera;

    private int duracion;

    @OneToMany(mappedBy="carrera")
    private List<estudainteCarrera> estudiantes;
}
