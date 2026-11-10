package com.microserviciolugar.controller;

import com.microserviciolugar.entity.ReservaGeneralEntity;
import com.microserviciolugar.service.ReservaGeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lugares")
public class ReservaGeneralController {

    @Autowired private ReservaGeneralService reservaGeneralService;

    /** Consultar cupos disponibles en un sector general. */
    @GetMapping("/sectores/{idSector}/disponibilidad")
    public ResponseEntity<?> disponibilidad(@PathVariable Long idSector) {
        try {
            int libres = reservaGeneralService.consultarDisponibles(idSector);
            return ResponseEntity.ok(Map.of("idSector", idSector, "disponibles", libres));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Reservar N cupos generales durante 10 minutos. */
    @PostMapping("/sectores/generales/reservar")
    public ResponseEntity<?> reservar(@RequestBody Map<String, Object> datos) {
        try {
            Long idSector = Long.valueOf(datos.get("idSector").toString());
            String idCliente = datos.get("idCliente").toString();
            Integer cantidad = Integer.valueOf(datos.get("cantidad").toString());
            ReservaGeneralEntity reserva = reservaGeneralService.reservar(idSector, idCliente, cantidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "idReserva", reserva.getId(),
                    "idSector", idSector,
                    "idCliente", reserva.getIdCliente(),
                    "cantidad", reserva.getCantidad(),
                    "estado", reserva.getEstado(),
                    "reservaHasta", reserva.getReservaHasta().toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Cancelar la reserva del cliente ante pago rechazado. */
    @DeleteMapping("/sectores/generales/reservas/{idReserva}")
    public ResponseEntity<?> cancelar(@PathVariable Long idReserva,
                                     @RequestParam Long idSector,
                                     @RequestParam String idCliente) {
        try {
            reservaGeneralService.cancelar(idSector, idReserva, idCliente);
            return ResponseEntity.ok("Reserva general cancelada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Uso interno de Ticket: validar la reserva antes del pago. */
    @PostMapping("/interno/sectores/generales/reservas/validar")
    public ResponseEntity<?> validar(@RequestBody Map<String, Object> datos) {
        try {
            reservaGeneralService.validar(
                    Long.valueOf(datos.get("idSector").toString()),
                    Long.valueOf(datos.get("idReserva").toString()),
                    datos.get("idCliente").toString(),
                    datos.get("idEvento").toString(),
                    Integer.valueOf(datos.get("cantidad").toString()));
            return ResponseEntity.ok("Reserva general valida");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Uso interno de Ticket: confirmar los N tickets de la reserva. */
    @PostMapping("/interno/sectores/generales/confirmar")
    public ResponseEntity<?> confirmar(@RequestBody Map<String, Object> datos) {
        try {
            Object ids = datos.get("idsTickets");
            if (!(ids instanceof List<?> lista)) {
                return ResponseEntity.badRequest().body("Faltan IDs de tickets");
            }
            List<Long> idsTickets = lista.stream().map(id -> Long.valueOf(id.toString())).toList();
            reservaGeneralService.confirmar(
                    Long.valueOf(datos.get("idSector").toString()),
                    Long.valueOf(datos.get("idReserva").toString()),
                    datos.get("idCliente").toString(),
                    Long.valueOf(datos.get("idCompra").toString()),
                    idsTickets);
            return ResponseEntity.ok("Venta general confirmada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Uso interno de Ticket: devolver un cupo vendido por ticket. */
    @PostMapping("/interno/sectores/generales/{idSector}/liberar-venta")
    public ResponseEntity<?> liberarVenta(@PathVariable Long idSector, @RequestParam Long idTicket) {
        try {
            reservaGeneralService.liberarVenta(idSector, idTicket);
            return ResponseEntity.ok("Cupo general liberado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
