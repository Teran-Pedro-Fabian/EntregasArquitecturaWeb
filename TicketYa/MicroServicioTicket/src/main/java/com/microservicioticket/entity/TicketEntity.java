package com.microservicioticket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ticket")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEvento;
    private String idCliente; //String porque viene de Mongo
    private Long idSector;

    // puede ser null si es entrada general
    private Long idAsiento;

    private Double precio;
    private String qrCode;    // Guardaremos aquí la cadena del QR
    private boolean usado = false; // Para el control de acceso (Entrega 2)
    private String estadoDevolucion = "SIN_SOLICITUD";
    // Administrador que reviso la solicitud
    private String idAdministradorRevision;
    // Fecha en la que se reviso
    private Instant fechaRevision;
}

