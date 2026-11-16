package com.microservicioticket.controller;

import com.microservicioticket.dto.ColaRequest;
import com.microservicioticket.dto.ColaResponse;
import com.microservicioticket.service.ColaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class ColaController {

    @Autowired
    private ColaService colaService;

    /**
     * Ingresar a la fila virtual de un evento
     * Para eventos de alta demanda la venta se habilita en orden de llegada
     */
    @PostMapping("/cola/{idEvento}/entrar")
    public ResponseEntity<?> entrar(@PathVariable String idEvento, @RequestBody ColaRequest request) {
        try {
            ColaResponse respuesta = colaService.entrar(idEvento, request.getIdCliente());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Consultar la posicion de un cliente dentro de la fila virtual de un evento
     */
    @GetMapping("/cola/{idEvento}/posicion")
    public ResponseEntity<?> posicion(@PathVariable String idEvento, @RequestParam String idCliente) {
        try {
            ColaResponse respuesta = colaService.consultarPosicion(idEvento, idCliente);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
