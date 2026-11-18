package com.microservicioticket.controller;

import com.microservicioticket.dto.ColaRequest;
import com.microservicioticket.dto.ColaResponse;
import com.microservicioticket.service.ColaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/ticket/cola")
public class ColaController {

    @Autowired
    private ColaService colaService;

    @PostMapping("/{idEvento}/entrar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('CLIENTE') and #request.idCliente == authentication.name)")
    public ResponseEntity<?> entrar(@PathVariable String idEvento, @RequestBody ColaRequest request) {
        try {
            String idCliente = request == null ? null : request.getIdCliente();
            ColaResponse respuesta = colaService.entrar(idEvento, idCliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo consultar el evento");
        }
    }

    @GetMapping("/{idEvento}/posicion")
    @PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('CLIENTE') and #idCliente == authentication.name)")
    public ResponseEntity<?> posicion(@PathVariable String idEvento, @RequestParam String idCliente) {
        try {
            return ResponseEntity.ok(colaService.consultarPosicion(idEvento, idCliente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
