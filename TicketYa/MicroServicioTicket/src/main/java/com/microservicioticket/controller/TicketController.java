package com.microservicioticket.controller;

import com.microservicioticket.dto.LoginRequest;
import com.microservicioticket.dto.TokenResponse;
import com.microservicioticket.entity.TicketEntity;
import com.microservicioticket.service.TicketService;
import com.microservicioticket.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private TokenService tokenService;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketEntity> obtener(@PathVariable Long id) {
        TicketEntity ticket = ticketService.obtenerTicket(id);
        return ticket != null ? ResponseEntity.ok(ticket) : ResponseEntity.notFound().build();
    }

    /**
     * Solicitar la devolucion de un ticket (solo el cliente dueño o un administrador)
     */
    @PostMapping("/{id}/solicitar-devolucion")
    @PreAuthorize("#idCliente == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> solicitarDevolucion(@PathVariable Long id, @RequestParam String idCliente) {
        try {
            TicketEntity ticket = ticketService.solicitarDevolucion(id, idCliente);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Procesar el reintegro de una devolucion aprobada (solo administrador)
     */
    @PatchMapping("/{id}/procesar-reintegro")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> procesarReintegro(@PathVariable Long id) {
        try {
            TicketEntity ticket = ticketService.procesarReintegro(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Finalizar una devolucion aprobada y liberar el asiento (solo administrador)
     */
    @PatchMapping("/{id}/finalizar-devolucion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> finalizarDevolucion(@PathVariable Long id) {
        try {
            TicketEntity ticket = ticketService.finalizarDevolucion(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Revisar una solicitud de devolucion para aprobar o rechazar (solo administrador)
     */
    @PatchMapping("/{id}/revisar-devolucion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> revisarDevolucion(@PathVariable Long id,
                                               @RequestParam String idAdministrador,
                                               @RequestParam String decision) {
        try {
            TicketEntity ticket = ticketService.revisarDevolucion(id, idAdministrador, decision);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Registrar el ingreso en puerta escaneando el codigo QR (personal de acceso o administrador)
     */
    @PostMapping("/escanearQrcode")
    @PreAuthorize("hasAnyRole('PERSONAL_ACCESO', 'ADMINISTRADOR')")
    public ResponseEntity<?> escanearQrcode(@RequestParam String qrCode) {
        try {
            TicketEntity ticket = ticketService.marcarEntradaUtilizada(qrCode);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Login que emite un JWT exclusivo para el personal de acceso
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        if (datos == null || datos.getIdUsuario() == null || datos.getIdUsuario().isBlank()) {
            return new ResponseEntity<>("Falta el idUsuario del personal de acceso", HttpStatus.BAD_REQUEST);
        }
        if (datos.getRol() == null || !datos.getRol().trim().equalsIgnoreCase("PERSONAL_ACCESO")) {
            return new ResponseEntity<>("Rol no valido: debe ser PERSONAL_ACCESO", HttpStatus.BAD_REQUEST);
        }
        String rolNormalizado = "PERSONAL_ACCESO";
        String token = tokenService.generarTokenJwt(datos.getIdUsuario(), rolNormalizado);
        TokenResponse respuesta = new TokenResponse(token, datos.getIdUsuario(), rolNormalizado);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }
}

