package com.microserviciolugar.controller;

import com.microserviciolugar.dto.SectorPublicoDTO;
import com.microserviciolugar.service.ReporteSectorLugarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lugares/eventos")
public class SectorPublicoController {

    private final ReporteSectorLugarService reporteService;

    public SectorPublicoController(ReporteSectorLugarService reporteService) {
        this.reporteService = reporteService;
    }

    // sectores visibles para el flujo de compra
    // no devuelve datos internos de reservas o ventas
    @GetMapping("/{idEvento}/sectores")
    public ResponseEntity<?> obtenerSectores(@PathVariable String idEvento) {
        try {
            List<SectorPublicoDTO> sectores = reporteService.obtenerSectoresPublicos(idEvento);
            return ResponseEntity.ok(sectores);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
