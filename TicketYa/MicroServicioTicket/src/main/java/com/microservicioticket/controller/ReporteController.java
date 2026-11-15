package com.microservicioticket.controller;

import com.microservicioticket.dto.ReporteEventoDTO;
import com.microservicioticket.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/ticket/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Reporte del punto 12.
     * Por ahora el ID del organizador se recibe por parametro, igual que en la Entrega 1.
     * Cuando se incorpore JWT se debe obtener del usuario autenticado.
     */
    @GetMapping("/eventos/{idEvento}")
    public ResponseEntity<?> obtenerReporte(@PathVariable String idEvento,
                                            @RequestParam String idOrganizador) {
        try {
            ReporteEventoDTO reporte = reporteService.reportePorEvento(idEvento, idOrganizador);
            return ResponseEntity.ok(reporte);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo conectar con Evento o Lugar");
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error al consultar Evento o Lugar");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
