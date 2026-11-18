package com.microserviciocliente.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String idCliente;
    private String rol;
}
