package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service("permisosTicket")
public class PermisosTicketService {
    private final CompraRepository compras;
    private final TicketRepository tickets;

    public PermisosTicketService(CompraRepository compras, TicketRepository tickets) {
        this.compras = compras;
        this.tickets = tickets;
    }

    public boolean esDuenioCompra(Long idCompra, String idCliente) {
        return idCliente != null && compras.findById(idCompra)
                .map(c -> idCliente.equals(c.getIdCliente())).orElse(false);
    }

    public boolean esDuenioTicket(Long idTicket, String idCliente) {
        return idCliente != null && tickets.findById(idTicket)
                .map(t -> idCliente.equals(t.getIdCliente())).orElse(false);
    }
}
