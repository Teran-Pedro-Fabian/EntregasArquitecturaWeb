package com.microServicioEvento.controller;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.dto.BusquedaEventoFiltro;
import com.microServicioEvento.dto.EventoBusquedaDTO;
import com.microServicioEvento.dto.LoginRequest;
import com.microServicioEvento.dto.TokenResponse;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.entity.ParticipanteEntity;
import com.microServicioEvento.service.EventoService;
import com.microServicioEvento.service.JwtService;
import com.microServicioEvento.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evento")
public class EventoController {

    @Autowired
    private EventoService eventoService;
    @Autowired
    private TokenService tokenService;
    @Autowired 
    private JwtService jwtService;

    /** Crear un borrador del organizador indicado en el JSON. */
    @PostMapping
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> crear(@RequestBody EventoEntity evento,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crearEvento(evento));
    }

    /** Modificar los datos de un borrador. */
    @PutMapping("/{id}")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> actualizar(@PathVariable String id,
            @RequestParam String idOrganizador, @RequestBody EventoEntity datos,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.actualizarBorrador(id, idOrganizador, datos));
    }

    /** Se conserva la ruta /estado que ya usaba el proyecto. */
    @PutMapping("/{id}/estado")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> publicar(@PathVariable String id,
            @RequestParam String idOrganizador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.publicarEvento(id, idOrganizador));
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> suspender(@PathVariable String id,
            @RequestParam String idAdministrador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.suspenderEvento(id, idAdministrador));
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> reactivar(@PathVariable String id,
            @RequestParam String idAdministrador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.reactivarEvento(id, idAdministrador));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> cancelar(@PathVariable String id,
            @RequestParam String idAdministrador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.cancelarEvento(id, idAdministrador));
    }

    /** Reprocesar reintegros si un servicio estuvo caido. */
    @PostMapping("/{id}/reintentar-cancelacion")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<java.util.Map<String, Object>> reintentarCancelacion(
            @PathVariable String id, @RequestParam String idAdministrador,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.reintentarCancelacion(id, idAdministrador));
    }

    @PatchMapping("/{id}/agotar")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> agotar(@PathVariable String id,
            @RequestParam String idAdministrador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.marcarAgotado(id, idAdministrador));
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoEntity> finalizar(@PathVariable String id,
            @RequestParam String idAdministrador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
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
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ParticipanteEntity> crearParticipante(@RequestBody ParticipanteEntity participante,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crearParticipante(participante));
    }

    @GetMapping("/participantes")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<ParticipanteEntity>> listarParticipantes() {
        return ResponseEntity.ok(eventoService.listarParticipantes());
    }

    @PostMapping("/{idEvento}/participantes/{idParticipante}")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<EventoParticipanteEntity> agregarParticipante(
            @PathVariable String idEvento, @PathVariable String idParticipante,
            @RequestParam String idOrganizador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.ok(eventoService.agregarParticipante(idEvento, idParticipante, idOrganizador));
    }

    @DeleteMapping("/{idEvento}/participantes/{idParticipante}")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Void> quitarParticipante(
            @PathVariable String idEvento, @PathVariable String idParticipante,
            @RequestParam String idOrganizador, @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (!autorizado(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        eventoService.quitarParticipante(idEvento, idParticipante, idOrganizador);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idEvento}/participantes")
    @PreAuthorize ("hasAnyRole('ORGANIZADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<ParticipanteEntity>> participantesDelEvento(@PathVariable String idEvento) {
        return ResponseEntity.ok(eventoService.participantesDelEvento(idEvento));
    }

    /** Punto 9: filtros combinables y orden por fecha, precio o relevancia. */
    @GetMapping("/buscar")
    public ResponseEntity<List<EventoBusquedaDTO>> buscar(@ModelAttribute BusquedaEventoFiltro filtro) {
        return ResponseEntity.ok(eventoService.buscarEventos(filtro));
    }


    @GetMapping ("/participante/{id}")
    public ResponseEntity<ParticipanteEntity> obtenerParticipantePorId(@PathVariable String id) {
        ParticipanteEntity participante = eventoService.findbyparticipanteByidParticipante(id);
        return participante != null ? ResponseEntity.ok(participante) : ResponseEntity.notFound().build();
    }

    /**
     * Organizadores y administradores pueden administrar eventos.
     * El personal de acceso solo consulta datos (no escribe sobre eventos).
     */
    private boolean autorizado(String roles) {
        return roles != null && (roles.contains("ORGANIZADOR") || roles.contains("ADMINISTRADOR"));
    }

    // ENDPOINT: Login que emite un JWT para organizadores y administradores
    // POST /evento/login (a través del Gateway)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        if (datos == null || datos.getIdUsuario() == null || datos.getIdUsuario().isBlank()) {
            return new ResponseEntity<>("Falta el idUsuario del organizador o administrador", HttpStatus.BAD_REQUEST);
        }
        if (datos.getRol() == null) {
            return new ResponseEntity<>("Falta el rol del usuario", HttpStatus.BAD_REQUEST);
        }

        String rolNormalizado = datos.getRol().trim().toUpperCase();
        if (!rolNormalizado.equals("ORGANIZADOR") && !rolNormalizado.equals("ADMINISTRADOR")) {
            return new ResponseEntity<>("Rol no valido: debe ser ORGANIZADOR o ADMINISTRADOR", HttpStatus.BAD_REQUEST);
        }

        String token = jwtService.generarTokenJwt(datos.getIdUsuario(), rolNormalizado);
        TokenResponse respuesta = new TokenResponse(token, datos.getIdUsuario(), rolNormalizado);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

}
