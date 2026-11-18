package com.microserviciocliente.controller;


import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LoginRequest;
import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.dto.TokenResponse;
import com.microserviciocliente.servise.NotificacionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.JwtService;
import com.microserviciocliente.servise.PreferenciasService;
import org.springframework.security.core.Authentication;




@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PreferenciasService preferenciasService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired 
    private JwtService jwtService;

     // Solo usuarios autenticados pueden guardar preferencias
    @PostMapping("/preferencias")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    public ResponseEntity<PreferenciasEntity> guardarPreferencias(@RequestBody PreferenciasEntity preferencias, Authentication authentication) {
        // Evita que un cliente cree preferencias a nombre de otro ID
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!esAdmin && !authentication.getName().equals(preferencias.getIdCliente())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PreferenciasEntity prefGuardada = preferenciasService.savePreferencia(preferencias);
        return new ResponseEntity<>(prefGuardada, HttpStatus.CREATED);
    }

    // Solo el propio dueño del ID o un ADMINISTRADOR pueden ver el perfil
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ClienteEntity> obtenerCliente(@PathVariable String id) {
        ClienteEntity cliente = clienteService.findById(id);
        if (cliente != null) {
            return ResponseEntity.ok(cliente);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Solo el propio dueño del ID o un ADMINISTRADOR pueden ver sus notificaciones
    @GetMapping("/{id}/notificaciones")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<NotificacionEntity>> consultarNotificaciones(@PathVariable String id) {
        return ResponseEntity.ok(notificacionService.listarPorCliente(id));
    }

    // Solo el propio dueño del ID o un ADMINISTRADOR pueden ver sus recomendaciones
    @GetMapping("/{id}/recomendaciones")
    @PreAuthorize("#idCliente == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<EventoEntityDTO>> obtenerRecomendaciones(@PathVariable("id") String idCliente) {
        List<EventoEntityDTO> recomendaciones = clienteService.obtenerRecomendaciones(idCliente);
        return ResponseEntity.ok(recomendaciones);
    }

    // Ticket envia este aviso cuando cancela un evento
    @PostMapping("/interno/notificaciones/cancelacion")
    public ResponseEntity<?> recibirCancelacion(@RequestBody NotificacionCancelacionRequest datos) {
        try {
            return ResponseEntity.ok(notificacionService.recibirCancelacion(datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        if (datos == null || datos.getIdCliente() == null || datos.getIdCliente().isBlank()) {
            return new ResponseEntity<>("Falta el idCliente del cliente", HttpStatus.BAD_REQUEST);
        }
        ClienteEntity cliente = clienteService.findById(datos.getIdCliente());
        if (cliente == null) {
            return new ResponseEntity<>("Cliente no encontrado", HttpStatus.NOT_FOUND);
        }

        // SEGURIDAD: Obtener el rol desde la entidad de BD (o fijo como "CLIENTE"), nunca desde 'datos.getRol()'
        // Si tu ClienteEntity tiene getRol(), usalo así:
        // String rol = (cliente.getRol() == null || cliente.getRol().isBlank()) ? "CLIENTE" : cliente.getRol().toUpperCase();
        String rol = "CLIENTE";

        String token = jwtService.generarTokenJwt(datos.getIdCliente(), rol);
        TokenResponse respuesta = new TokenResponse(token, datos.getIdCliente(), rol);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }




}
