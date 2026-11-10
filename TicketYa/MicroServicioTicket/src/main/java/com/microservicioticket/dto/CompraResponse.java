package com.microservicioticket.dto;

import com.microservicioticket.entity.TicketEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CompraResponse {
    private Long idCompra;
    private String estado;
    private String medioPago;
    private Double total;
    private List<TicketEntity> tickets;
}
