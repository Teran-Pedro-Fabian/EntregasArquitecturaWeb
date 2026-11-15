package com.microserviciolugar.controller;

import com.microserviciolugar.dto.ReporteSectorLugarDTO;
import com.microserviciolugar.service.ReporteSectorLugarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lugares/interno/reportes")
public class ReporteSectorLugarController {

    private final ReporteSectorLugarService reporteService;

    public ReporteSectorLugarController(ReporteSectorLugarService reporteService) {
        this.reporteService = reporteService;
    }

    /** Solo consulta datos de sectores. Lo consume MicroServicioTicket. */
    @GetMapping("/eventos/{idEvento}/sectores")
    public ResponseEntity<List<ReporteSectorLugarDTO>> obtenerSectores(
            @PathVariable String idEvento) {
        return ResponseEntity.ok(reporteService.obtenerSectores(idEvento));
    }
}
