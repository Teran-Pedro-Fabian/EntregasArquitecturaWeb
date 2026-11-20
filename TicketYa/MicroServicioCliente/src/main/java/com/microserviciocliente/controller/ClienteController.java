package com.microserviciocliente.controller;


import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LoginRequest;
import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.NotificacionRequest;
import com.microserviciocliente.dto.NotificacionPublicacionRequest;
import com.microserviciocliente.dto.TokenResponse;
import com.microserviciocliente.servise.NotificacionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.microserviciocliente.dto.LoginRequest;
import com.microserviciocliente.servise.LoginService;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.JwtService;
import com.microserviciocliente.servise.PreferenciasService;
import com.microserviciocliente.servise.RecomendacionService;
import org.springframework.web.client.RestClientException;
import java.util.NoSuchElementException;
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
    private RecomendacionService recomendacionService;
    @Autowired
    private LoginService loginService;

    @Autowired 
    private JwtService jwtService;

     // Solo usuarios autenticados pueden guardar preferencias
    // ENDPOINT: Crear un cliente
    // POST http://localhost:8081/clientes
    @PostMapping
    public ResponseEntity<ClienteEntity> crearCliente(@RequestBody ClienteEntity cliente) {
        // El ID lo genera el servidor, nunca se puede sobrescribir una cuenta desde el registro
        if (cliente == null || (cliente.getId() != null && !cliente.getId().isBlank())) {
            return ResponseEntity.badRequest().build();
        }
        ClienteEntity nuevoCliente;
        try {
            nuevoCliente = clienteService.save(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        // Devolvemos el cliente creado con un estado 201 (Created)
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    // ENDPOINT: Guardar preferencias de un cliente
    // POST http://localhost:8081/clientes/preferencias
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

    // Punto 10: recomendaciones personales y por clientes con gustos similares
    // GET http://localhost:8081/clientes/{id}/recomendaciones
    @GetMapping("/{id}/recomendaciones")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtenerRecomendaciones(@PathVariable String id) {
        try {
            List<EventoEntityDTO> recomendaciones = recomendacionService.obtenerRecomendaciones(id);
            return ResponseEntity.ok(recomendaciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudieron consultar los eventos o lugares");
        }
    }

    // Punto 10: recomendaciones personales y por clientes con gustos similares
    // GET http://localhost:8081/clientes/{id}/recomendaciones
    @GetMapping("/{id}/recomendaciones")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtenerRecomendaciones(@PathVariable String id) {
        try {
            List<EventoEntityDTO> recomendaciones = recomendacionService.obtenerRecomendaciones(id);
            return ResponseEntity.ok(recomendaciones);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudieron consultar los eventos o lugares");
        }
    }

    // Solo el propio dueño del ID o un ADMINISTRADOR pueden ver el perfil
    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
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

    // Registrar o actualizar el correo del cliente para el envio SMTP
    @PatchMapping("/{id}/email")
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
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
    @PreAuthorize("#id == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<NotificacionEntity>> consultarNotificaciones(@PathVariable String id) {
        return ResponseEntity.ok(notificacionService.listarPorCliente(id));
    }

    // ENDPOINT: Login que emite un JWT para el rol indicado
    // Fase 1: emision con rol CLIENTE | Fase 2: emision con los roles faltantes (ORGANIZADOR, ADMINISTRADOR, PERSONAL_ACCESO)
    // POST http://localhost:8081/clientes/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        if (datos == null || datos.getIdCliente() == null || datos.getIdCliente().isBlank()) {
            return new ResponseEntity<>("Falta el idCliente del cliente", HttpStatus.BAD_REQUEST);
        }

        // El rol se toma del request: CLIENTE, ORGANIZADOR, ADMINISTRADOR o PERSONAL_ACCESO
        // Fase 2: emision de tokens con los roles faltantes
        String rol = (datos.getRol() == null || datos.getRol().isBlank()) ? "CLIENTE" : datos.getRol().toUpperCase();
        if (!esRolValido(rol)) {
            return new ResponseEntity<>("Rol no valido: debe ser CLIENTE, ORGANIZADOR, ADMINISTRADOR o PERSONAL_ACCESO", HttpStatus.BAD_REQUEST);
        }

        // El CLIENTE se valida contra la base de datos; los demas roles viajan con el id indicado
        if ("CLIENTE".equals(rol)) {
            ClienteEntity cliente = clienteService.findById(datos.getIdCliente());
            if (cliente == null) {
                return new ResponseEntity<>("Cliente no encontrado", HttpStatus.NOT_FOUND);
            }
        }

        // Emision del token con el rol correspondiente
        String token = jwtService.generarTokenJwt(datos.getIdCliente(), rol);
        TokenResponse respuesta = new TokenResponse(token, datos.getIdCliente(), rol);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    private boolean esRolValido(String rol) {
        return rol != null && (rol.equals("CLIENTE") || rol.equals("ORGANIZADOR") || rol.equals("ADMINISTRADOR") || rol.equals("PERSONAL_ACCESO"));
    }




}
