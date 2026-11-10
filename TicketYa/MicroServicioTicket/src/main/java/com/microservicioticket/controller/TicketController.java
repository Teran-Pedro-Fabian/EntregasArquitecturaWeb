package com.microservicioticket.controller;

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

    /**
     * Emitir un ticket
     * Validar la reserva antes de guardar
     */
    // Ruta anterior deshabilitada para evitar comprar sin validar precio ni limite
    // El endpoint /ticket/emitir ahora esta en CompraController
    @Deprecated
    public ResponseEntity<?> emitir(@RequestBody TicketEntity ticket) {
        try {
            // 1) Emitir el ticket
            TicketEntity nuevoTicket = ticketService.emitirTicket(ticket);
            // 2) Devolver el ticket creado
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTicket);
        } catch (IllegalArgumentException e) {
            // 3) Informar datos invalidos
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 4) Informar reserva invalida
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Operacion rechazada por Lugar: " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 5) Informar error de conexion con Lugar
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("No se pudo conectar con MicroServicioLugar");
        }
        catch (IllegalStateException e) {
            // Informar si el pago fue rechazado
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketEntity> obtener(@PathVariable Long id) {
        TicketEntity ticket = ticketService.obtenerTicket(id);
        return ticket != null ? ResponseEntity.ok(ticket) : ResponseEntity.notFound().build();
    }

    /**
     * Solicitar la devolucion de un ticket
     *
     * @param id ID del ticket
     * @param idCliente ID del cliente
     * @return resultado de la solicitud
     */
    @PostMapping("/{id}/solicitar-devolucion")
    public ResponseEntity<?> solicitarDevolucion(@PathVariable Long id, @RequestParam String idCliente) {
        try {
            // 1) Registrar la solicitud de devolucion
            TicketEntity ticket = ticketService.solicitarDevolucion(id, idCliente);
            // 2) Devolver el ticket con su nuevo estado
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            // 3) Informar si no se pudo realizar la solicitud
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Procesar el reintegro de una devolucion aprobada
     *
     * @param id ID del ticket
     * @return resultado del reintegro
     */
    @PatchMapping("/{id}/procesar-reintegro")
    public ResponseEntity<?> procesarReintegro(@PathVariable Long id) {
        try {
            // 1) Procesar el reintegro simulado
            TicketEntity ticket = ticketService.procesarReintegro(id);
            // 2) Devolver el ticket actualizado
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            // 3) Informar si no se pudo procesar
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Finalizar una devolucion aprobada
     * Liberar el asiento y actualizar el ticket
     *
     * @param id ID del ticket
     * @return ticket actualizado
     */
    @PatchMapping("/{id}/finalizar-devolucion")
    public ResponseEntity<?> finalizarDevolucion(@PathVariable Long id) {
        try {
            // 1) Finalizar la devolucion
            TicketEntity ticket = ticketService.finalizarDevolucion(id);
            // 2) Devolver el ticket actualizado
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            // 3) Informar si no se pudo finalizar
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    /**
     * Revisar una solicitud de devolucion
     * Permitir aprobar o rechazar
     *
     * @param id ID del ticket
     * @param idAdministrador ID del administrador
     * @param decision APROBAR o RECHAZAR
     * @return ticket actualizado
     */
    @PatchMapping("/{id}/revisar-devolucion")
    public ResponseEntity<?> revisarDevolucion(
            @PathVariable Long id,
            @RequestParam String idAdministrador,
            @RequestParam String decision) {
        try {
            // 1) Revisar la solicitud
            TicketEntity ticket = ticketService.revisarDevolucion(id, idAdministrador, decision);
            // 2) Devolver el ticket actualizado
            return ResponseEntity.ok(ticket);

        } catch (RuntimeException e) {
            // 3) Informar si no se pudo revisar
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}
