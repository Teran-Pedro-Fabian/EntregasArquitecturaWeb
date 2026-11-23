package com.microservicioticket.controller;

import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.service.CompraService;
import com.microservicioticket.service.ColaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/ticket")
public class CompraController {

    @Autowired
    private CompraService compraService;
    @Autowired
    private ColaService colaService;

    private static final Logger log = LoggerFactory.getLogger(CompraController.class);

    // La compra ya se confirmo en PostgreSQL, el retiro de la cola es posterior
    private void avanzarFila(CompraResponse respuesta, String idEvento, String idCliente) {
        if (!"CONFIRMADA".equals(respuesta.getEstado())) return;
        try {
            colaService.finalizarCompra(idEvento, idCliente);
        } catch (RuntimeException e) {
            log.warn("Compra confirmada pero no se pudo liberar el turno de fila: {}", e.getMessage());
        }
    }

    /**
     * Consultar una compra y sus tickets
     */
    @GetMapping("/compras/{idCompra}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @permisosTicket.esDuenioCompra(#idCompra, authentication.name)")
    public ResponseEntity<?> obtenerCompra(@PathVariable Long idCompra) {
        try {
            return ResponseEntity.ok(compraService.obtenerCompra(idCompra));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reintentar una compra incierta sin volver a cobrar.
     */
    @PostMapping("/compras/{idCompra}/reintentar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @permisosTicket.esDuenioCompra(#idCompra, authentication.name)")
    public ResponseEntity<?> reintentarConfirmacion(@PathVariable Long idCompra) {
        try {
            CompraResponse respuesta = compraService.reintentarConfirmacion(idCompra);
            if ("CONFIRMADA".equals(respuesta.getEstado()) && !respuesta.getTickets().isEmpty()) {
                var ticket = respuesta.getTickets().get(0);
                avanzarFila(respuesta, ticket.getIdEvento(), ticket.getIdCliente());
            }
            return ResponseEntity.status("CONFIRMADA".equals(respuesta.getEstado())
                    ? HttpStatus.OK : HttpStatus.ACCEPTED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /** Comprar entradas de Campo General (sector no numerado). */
    @PostMapping("/comprar-general")
    @PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('CLIENTE') and #request.idCliente == authentication.name)")
    public ResponseEntity<?> comprarGeneral(@RequestBody CompraGeneralRequest request) {
        try {
            CompraResponse respuesta = compraService.comprarGeneral(request);
            avanzarFila(respuesta, request.getIdEvento(), request.getIdCliente());
            return ResponseEntity.status("CONFIRMADA".equals(respuesta.getEstado())
                    ? HttpStatus.CREATED : HttpStatus.ACCEPTED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpClientErrorException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo conectar con otro microservicio");
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
    }

    /**
     * Comprar entradas numeradas
     * El precio se consulta a Lugar
     */
    @PostMapping({"/comprar", "/emitir"})
    @PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('CLIENTE') and #request.idCliente == authentication.name)")
    public ResponseEntity<?> comprar(@RequestBody CompraRequest request) {
        try {
            CompraResponse respuesta = compraService.comprar(request);
            avanzarFila(respuesta, request.getIdEvento(), request.getIdCliente());
            return ResponseEntity.status("CONFIRMADA".equals(respuesta.getEstado())
                    ? HttpStatus.CREATED : HttpStatus.ACCEPTED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Operacion rechazada por otro servicio: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo conectar con otro microservicio");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error de comunicacion entre microservicios");
        }
    }
}
