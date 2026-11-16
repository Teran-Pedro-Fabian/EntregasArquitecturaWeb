package com.microserviciocliente.controller;


import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.dto.NotificacionRequest;
import com.microserviciocliente.dto.NotificacionPublicacionRequest;
import com.microserviciocliente.servise.NotificacionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.PreferenciasService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PreferenciasService preferenciasService;

    @Autowired
    private NotificacionService notificacionService;

    // ENDPOINT: Crear un cliente
    // POST http://localhost:8081/clientes
    @PostMapping
    public ResponseEntity<ClienteEntity> crearCliente(@RequestBody ClienteEntity cliente) {
        ClienteEntity nuevoCliente = clienteService.save(cliente);
        // Devolvemos el cliente creado con un estado 201 (Created)
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    // ENDPOINT: Guardar preferencias de un cliente
    // POST http://localhost:8081/clientes/preferencias
    @PostMapping("/preferencias")
    public ResponseEntity<PreferenciasEntity> guardarPreferencias(@RequestBody PreferenciasEntity preferencias) {
        PreferenciasEntity prefGuardada = preferenciasService.savePreferencia(preferencias);
        return new ResponseEntity<>(prefGuardada, HttpStatus.CREATED);
    }

    // ENDPOINT: Obtener un cliente por su ID
    // GET http://localhost:8081/clientes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ClienteEntity> obtenerCliente(@PathVariable String id) {
        ClienteEntity cliente = clienteService.findById(id);
        if (cliente != null) {
            return ResponseEntity.ok(cliente); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
    // Registrar o actualizar el correo del cliente para el envio SMTP
    @PatchMapping("/{id}/email")
    public ResponseEntity<?> actualizarEmail(@PathVariable String id, @RequestBody java.util.Map<String, String> datos) {
        try {
            return ResponseEntity.ok(clienteService.actualizarEmail(id, datos.get("email")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Avisos internos que llegan desde Ticket
    @PostMapping("/interno/notificaciones/compra")
    public ResponseEntity<?> recibirCompra(@RequestBody NotificacionRequest datos) {
        try {
            return ResponseEntity.ok(notificacionService.recibirCompra(datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/interno/notificaciones/cambio")
    public ResponseEntity<?> recibirCambio(@RequestBody NotificacionRequest datos) {
        try {
            return ResponseEntity.ok(notificacionService.recibirCambio(datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Evento informa cuando se publica un artista favorito
    @PostMapping("/interno/notificaciones/publicacion")
    public ResponseEntity<?> recibirPublicacion(@RequestBody NotificacionPublicacionRequest datos) {
        try {
            return ResponseEntity.ok(java.util.Map.of("notificados", notificacionService.recibirPublicacion(datos)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Ticket envia este aviso cuando cancela un evento
    // El correo se procesa despues, sin bloquear la cancelacion
    @PostMapping("/interno/notificaciones/cancelacion")
    public ResponseEntity<?> recibirCancelacion(@RequestBody NotificacionCancelacionRequest datos) {
        try {
            return ResponseEntity.ok(notificacionService.recibirCancelacion(datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bandeja de notificaciones que consulta el comprador desde el Gateway
    @GetMapping("/{id}/notificaciones")
    public ResponseEntity<List<NotificacionEntity>> consultarNotificaciones(@PathVariable String id) {
        return ResponseEntity.ok(notificacionService.listarPorCliente(id));
    }
}
