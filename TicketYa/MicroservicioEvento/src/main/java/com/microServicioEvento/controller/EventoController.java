package com.microServicioEvento.controller;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.dto.BusquedaEventoFiltro;
import com.microServicioEvento.dto.EventoBusquedaDTO;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.entity.ParticipanteEntity;
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

    /** Crear un borrador del organizador indicado en el JSON. */
    @PostMapping
    public ResponseEntity<EventoEntity> crear(@RequestBody EventoEntity evento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crearEvento(evento));
    }

    /** Modificar los datos de un borrador. */
    @PutMapping("/{id}")
    public ResponseEntity<EventoEntity> actualizar(@PathVariable String id,
            @RequestParam String idOrganizador, @RequestBody EventoEntity datos) {
        return ResponseEntity.ok(eventoService.actualizarBorrador(id, idOrganizador, datos));
    }

    /** Se conserva la ruta /estado que ya usaba el proyecto. */
    @PutMapping("/{id}/estado")
    public ResponseEntity<EventoEntity> publicar(@PathVariable String id,
            @RequestParam String idOrganizador) {
        return ResponseEntity.ok(eventoService.publicarEvento(id, idOrganizador));
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<EventoEntity> suspender(@PathVariable String id,
            @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.suspenderEvento(id, idAdministrador));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EventoEntity> reactivar(@PathVariable String id,
            @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.reactivarEvento(id, idAdministrador));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EventoEntity> cancelar(@PathVariable String id,
            @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.cancelarEvento(id, idAdministrador));
    }

    /** Reprocesar reintegros si un servicio estuvo caido. */
    @PostMapping("/{id}/reintentar-cancelacion")
    public ResponseEntity<java.util.Map<String, Object>> reintentarCancelacion(
            @PathVariable String id, @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.reintentarCancelacion(id, idAdministrador));
    }

    @PatchMapping("/{id}/agotar")
    public ResponseEntity<EventoEntity> agotar(@PathVariable String id,
            @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.marcarAgotado(id, idAdministrador));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<EventoEntity> finalizar(@PathVariable String id,
            @RequestParam String idAdministrador) {
        return ResponseEntity.ok(eventoService.finalizarEvento(id, idAdministrador));
    }

    /** Lista publica. No muestra borradores, suspendidos ni cancelados. */
    @GetMapping
    public ResponseEntity<List<EventoEntity>> listar() {
        return ResponseEntity.ok(eventoService.buscarEventosPublicados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoEntity> obtener(@PathVariable String id) {
        EventoEntity evento = eventoService.obtenerEvento(id);
        return evento != null ? ResponseEntity.ok(evento) : ResponseEntity.notFound().build();
    }

    /** Crear un participante reutilizable entre distintos eventos. */
    @PostMapping("/participantes")
    public ResponseEntity<ParticipanteEntity> crearParticipante(@RequestBody ParticipanteEntity participante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crearParticipante(participante));
    }

    @GetMapping("/participantes")
    public ResponseEntity<List<ParticipanteEntity>> listarParticipantes() {
        return ResponseEntity.ok(eventoService.listarParticipantes());
    }

    @PostMapping("/{idEvento}/participantes/{idParticipante}")
    public ResponseEntity<EventoParticipanteEntity> agregarParticipante(
            @PathVariable String idEvento, @PathVariable String idParticipante,
            @RequestParam String idOrganizador) {
        return ResponseEntity.ok(eventoService.agregarParticipante(idEvento, idParticipante, idOrganizador));
    }

    @DeleteMapping("/{idEvento}/participantes/{idParticipante}")
    public ResponseEntity<Void> quitarParticipante(
            @PathVariable String idEvento, @PathVariable String idParticipante,
            @RequestParam String idOrganizador) {
        eventoService.quitarParticipante(idEvento, idParticipante, idOrganizador);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idEvento}/participantes")
    public ResponseEntity<List<ParticipanteEntity>> participantesDelEvento(@PathVariable String idEvento) {
        return ResponseEntity.ok(eventoService.participantesDelEvento(idEvento));
    }

    /** Punto 9: filtros combinables y orden por fecha, precio o relevancia. */
    @GetMapping("/buscar")
    public ResponseEntity<List<EventoBusquedaDTO>> buscar(@ModelAttribute BusquedaEventoFiltro filtro) {
        return ResponseEntity.ok(eventoService.buscarEventos(filtro));
    }

}
