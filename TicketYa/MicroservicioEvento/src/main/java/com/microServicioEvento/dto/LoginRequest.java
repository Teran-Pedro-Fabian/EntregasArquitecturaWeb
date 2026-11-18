package com.microServicioEvento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String idUsuario;
    private String rol;
}
