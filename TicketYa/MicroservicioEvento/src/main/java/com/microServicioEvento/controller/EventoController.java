package com.microServicioEvento.controller;


import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evento")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @PostMapping
    public ResponseEntity<EventoEntity> crear(@RequestBody EventoEntity evento) {
        return new ResponseEntity<>(eventoService.crearEvento(evento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/publicar")
    public ResponseEntity<EventoEntity> publicar(@PathVariable String id) {
        return ResponseEntity.ok(eventoService.publicarEvento(id));
    }

    @GetMapping
    public ResponseEntity<List<EventoEntity>> listar() {
        return ResponseEntity.ok(eventoService.buscarEventosPublicados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoEntity> obtener(@PathVariable String id) {
        EventoEntity evento = eventoService.obtenerEvento(id);
        return evento != null ? ResponseEntity.ok(evento) : ResponseEntity.notFound().build();
    }
}