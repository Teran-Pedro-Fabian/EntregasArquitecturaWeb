package com.microservicioticket.controller;

import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/ticket")
public class CompraController {

    @Autowired
    private CompraService compraService;

    /**
     * Consultar una compra y sus tickets
     */
    @GetMapping("/compras/{idCompra}")
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
    public ResponseEntity<?> reintentarConfirmacion(@PathVariable Long idCompra) {
        try {
            CompraResponse respuesta = compraService.reintentarConfirmacion(idCompra);
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
    public ResponseEntity<?> comprarGeneral(@RequestBody CompraGeneralRequest request) {
        try {
            CompraResponse respuesta = compraService.comprarGeneral(request);
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
    public ResponseEntity<?> comprar(@RequestBody CompraRequest request) {
        try {
            CompraResponse respuesta = compraService.comprar(request);
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
