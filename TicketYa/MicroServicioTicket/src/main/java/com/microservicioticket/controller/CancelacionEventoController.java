package com.microservicioticket.controller;

import com.microservicioticket.entity.AvisoCancelacionEntity;
import com.microservicioticket.service.AvisoCancelacionService;
import com.microservicioticket.service.CancelacionEventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ticket")
public class CancelacionEventoController {

    @Autowired private CancelacionEventoService cancelacionService;
    @Autowired private AvisoCancelacionService avisoService;
    @Autowired private com.microservicioticket.service.BloqueoVentaEventoService bloqueoVentaEventoService;

    /** Antes de cancelar, esperar todas las compras activas y cerrar nuevas ventas. */
    @PostMapping("/interno/eventos/{idEvento}/bloquear-ventas")
    public ResponseEntity<?> bloquearVentas(@PathVariable String idEvento) {
        try {
            bloqueoVentaEventoService.bloquearDefinitivamente(idEvento);
            return ResponseEntity.ok("Ventas bloqueadas para el evento");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Llamado por Evento luego de registrar CANCELADO en PostgreSQL */
    @PostMapping("/interno/eventos/{idEvento}/procesar-cancelacion")
    public ResponseEntity<?> procesar(@PathVariable String idEvento,
                                      @RequestParam String idAdministrador) {
        try {
            return ResponseEntity.ok(cancelacionService.procesar(idEvento, idAdministrador));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Consultar cuantas entradas siguen pendientes de reintegro */
    @GetMapping("/eventos/{idEvento}/estado-cancelacion")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable String idEvento) {
        return ResponseEntity.ok(cancelacionService.consultar(idEvento));
    }

    /** Avisos persistidos en Ticket, la bandeja activa esta en Cliente */
    @GetMapping("/avisos-cancelacion")
    public ResponseEntity<List<AvisoCancelacionEntity>> avisos(@RequestParam String idCliente) {
        return ResponseEntity.ok(avisoService.listarPorCliente(idCliente));
    }
}
