package com.microserviciolugar.controller;


import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.entity.LugarEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.service.LugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lugares")
public class LugarController {

    @Autowired
    private LugarService lugarService;

    // --- GESTIÓN DE RECINTOS (LUGAR) ---
    @PostMapping
    public ResponseEntity<LugarEntity> crearLugar(@RequestBody LugarEntity lugar) {
        LugarEntity nuevoLugar = lugarService.guardarLugar(lugar);
        return new ResponseEntity<>(nuevoLugar, HttpStatus.CREATED);
    }

    // --- TRAE LUGARES ---
    @GetMapping("/sector/{id}/asiento")
    public ResponseEntity<List<LugarEntity>> listarLugares() {
        return ResponseEntity.ok(lugarService.listarTodosLosLugares());
    }

    // --- GESTIÓN DE SECTORES ---

    @PostMapping("/sectores")
    public ResponseEntity<SectorEntity> crearSector(@RequestBody SectorEntity sector) {
        SectorEntity nuevoSector = lugarService.guardarSector(sector);
        return new ResponseEntity<>(nuevoSector, HttpStatus.CREATED);
    }

    // --- EL CORAZÓN DEL SISTEMA: RESERVAS Y ASIENTOS ---

    /**
     * Endpoint para reservar un asiento temporalmente.
     * Body esperado: {"idAsiento": 1, "idCliente": "ID_DE_MONGO"}
     */
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarAsiento(@RequestBody Map<String, Object> request) {
        try {
            Long idAsiento = Long.valueOf(request.get("idAsiento").toString());
            String idCliente = request.get("idCliente").toString();

            AsientoEntity asientoReservado = lugarService.reservarAsiento(idAsiento, idCliente);
            return ResponseEntity.ok(asientoReservado);

        } catch (RuntimeException e) {
            // Si el asiento no está disponible, devolvemos 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Endpoint para confirmar la venta después de que el pago sea aprobado.
     */
    @PostMapping("/asiento/confirmar")
    public ResponseEntity<?> confirmarVenta(@RequestBody Map<String, Object> request) {
        try {
            Long idAsiento = Long.valueOf(request.get("idAsiento").toString());
            lugarService.confirmarVenta(idAsiento);
            return ResponseEntity.ok("Venta confirmada y asiento marcado como VENDIDO");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint para cancelar una reserva manualmente (o por timeout).
     */
    @DeleteMapping("/reservas/{idAsiento}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long idAsiento) {
        try {
            lugarService.cancelarReserva(idAsiento);
            return ResponseEntity.ok("Reserva cancelada, asiento disponible nuevamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @DeleteMapping("/asientos/liberar/{idAsiento}")
    public ResponseEntity<?> liberarAsiento(@PathVariable Long idAsiento) {
        try {
            lugarService.cancelarReserva(idAsiento);
            return ResponseEntity.ok("Asiento liberado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @GetMapping("/buscarPorUbicacion")
    public ResponseEntity<List<Long>> buscarPorUbicacion(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        List<Long> ids = lugarService.buscarIdsPorUbicacionExacta(lat, lon);
        return ResponseEntity.ok(ids);
    }
}