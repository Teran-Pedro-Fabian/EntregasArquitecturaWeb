package com.microservicioticket.dto;

import lombok.Data;
import java.util.List;

/**
 * Datos que envia el cliente para comprar asientos numerados
 * El precio no se recibe del cliente
 */
@Data
public class CompraRequest {
    private String idCliente;
    private String idEvento;
    private Long idSector;
    private List<Long> idsAsientos;
    private String medioPago;
}
