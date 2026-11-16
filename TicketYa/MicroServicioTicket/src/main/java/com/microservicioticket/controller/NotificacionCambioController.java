package com.microservicioticket.controller;

import com.microservicioticket.service.NotificacionCambioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ticket/interno/eventos")
public class NotificacionCambioController {
    @Autowired private NotificacionCambioService notificacionCambioService;

    @PostMapping("/{idEvento}/notificar-cambio")
    public ResponseEntity<Map<String, Integer>> notificarCambio(@PathVariable String idEvento,
            @RequestBody Map<String, String> datos) {
        int cantidad = notificacionCambioService.notificarCambio(idEvento,
                datos.get("referencia"), datos.get("mensaje"));
        return ResponseEntity.ok(Map.of("notificados", cantidad));
    }
}
