package com.microservicioticket.controller;

import com.microservicioticket.cleaner.TicketCleaner;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private TicketCleaner ticketCleaner;

    @PostMapping("/emitir")
    public ResponseEntity<TicketEntity> emitir(@RequestBody TicketEntity ticket) {
        TicketEntity nuevoTicket = ticketService.emitirTicket(ticket);
        return new ResponseEntity<>(nuevoTicket, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketEntity> obtener(@PathVariable Long id) {
        TicketEntity ticket = ticketService.obtenerTicket(id);
        return ticket != null ? ResponseEntity.ok(ticket) : ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable Long id) {
        try {
            ticketService.devolverTicket(id);
            return ResponseEntity.ok("Ticket eliminado y asiento liberado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @DeleteMapping("/admin/clear")
    public ResponseEntity<String> clearDb() {
        ticketCleaner.clear();
        return ResponseEntity.ok("Base de datos de tickets vaciada");
    }
}
