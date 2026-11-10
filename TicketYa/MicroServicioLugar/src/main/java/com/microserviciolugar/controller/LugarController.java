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

    /**
     * Obtener todos los lugares registrados
     * @return lista de lugares
     */
    @GetMapping
    public ResponseEntity<List<LugarEntity>> listarLugares() {
        // Obtener todos los lugares desde el servicio
        return ResponseEntity.ok(lugarService.listarTodosLosLugares());
    }


    // --- GESTIÓN DE SECTORES ---

    @PostMapping("/sectores")
    public ResponseEntity<?> crearSector(@RequestBody SectorEntity sector) {
        try {
            SectorEntity nuevoSector = lugarService.guardarSector(sector);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSector);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Devolver el precio oficial del sector para una compra
     */
    @GetMapping("/interno/sectores/{idSector}/precio")
    public ResponseEntity<?> obtenerPrecioSector(
            @PathVariable Long idSector, @RequestParam String idEvento) {
        try {
            Double precio = lugarService.obtenerPrecioSector(idSector, idEvento);
            return ResponseEntity.ok(Map.of(
                    "idSector", idSector,
                    "idEvento", idEvento,
                    "precio", precio));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
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
     * Confirmar la venta de un asiento
     * Asociar el ticket a la venta
     *
     * @param request datos de la venta
     * @return resultado de la confirmacion
     */
    @PostMapping("/asiento/confirmar")
    public ResponseEntity<?> confirmarVenta(@RequestBody Map<String, Object> request) {
        // 1) Verificar los datos recibidos
        if (request.get("idAsiento") == null || request.get("idCliente") == null || request.get("idTicket") == null) {
            return ResponseEntity.badRequest().body("Faltan datos de la venta");
        }
        try {
            // 2) Obtener los datos
            Long idAsiento = Long.valueOf(request.get("idAsiento").toString());
            String idCliente = request.get("idCliente").toString();
            Long idTicket = Long.valueOf(request.get("idTicket").toString());
            // 3) Confirmar la venta
            lugarService.confirmarVenta(idAsiento, idCliente, idTicket);
            return ResponseEntity.ok("Venta confirmada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancelar una reserva realizada por un cliente
     *
     * @param idAsiento ID del asiento
     * @param idCliente ID del cliente
     * @return resultado de la cancelacion
     */
    @DeleteMapping("/reservas/{idAsiento}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long idAsiento, @RequestParam String idCliente) {
        try {
            // 1) Cancelar la reserva del cliente
            lugarService.cancelarReserva(idAsiento, idCliente);
            return ResponseEntity.ok("Reserva cancelada correctamente");
        } catch (RuntimeException e) {
            // 2) Informar si no se pudo cancelar
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Liberar un asiento despues de una devolucion
     * Verificar el ticket asociado a la venta
     *
     * @param idAsiento ID del asiento
     * @param idTicket ID del ticket devuelto
     * @return resultado de la liberacion
     */
    @PostMapping("/interno/asientos/{idAsiento}/liberar-venta")
    public ResponseEntity<?> liberarAsientoVendido(@PathVariable Long idAsiento, @RequestParam Long idTicket) {
        try {
            // 1) Liberar el asiento vendido
            lugarService.liberarAsientoVendido(idAsiento, idTicket);
            // 2) Confirmar la liberacion
            return ResponseEntity.ok("Asiento liberado correctamente");
        } catch (RuntimeException e) {
            // 3) Informar si no se pudo liberar
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/buscarPorUbicacion")
    public ResponseEntity<List<Long>> buscarPorUbicacion(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        List<Long> ids = lugarService.buscarIdsPorUbicacionExacta(lat, lon);
        return ResponseEntity.ok(ids);
    }


    /**
     * Crear un asiento dentro de un sector
     *
     * @param request datos del asiento
     * @return asiento creado
     */
    @PostMapping("/asientos")
    public ResponseEntity<?> crearAsiento(@RequestBody Map<String, Object> request) {
        // 1) Verificar los datos
        if (request.get("idSector") == null || request.get("fila") == null || request.get("columna") == null) {
            return ResponseEntity.badRequest().body("Faltan datos del asiento");
        }
        try {
            // 2) Obtener los datos
            Long idSector = Long.valueOf(request.get("idSector").toString());
            String fila = request.get("fila").toString();
            Integer columna = Integer.valueOf(request.get("columna").toString());
            // 3) Crear el asiento
            AsientoEntity asiento = lugarService.crearAsiento(idSector, fila, columna);
            // 4) Devolver el asiento creado
            return ResponseEntity.status(HttpStatus.CREATED).body(asiento);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ID o columna no validos");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Validar una reserva para la emision de tickets
     */
    @PostMapping("/interno/reservas/validar")
    public ResponseEntity<?> validarReserva(@RequestBody Map<String, Object> request) {
        try {
            // 1) Obtener los datos
            Long idAsiento = Long.valueOf(request.get("idAsiento").toString());
            String idCliente = request.get("idCliente").toString();
            Long idSector = Long.valueOf(request.get("idSector").toString());
            String idEvento = request.get("idEvento").toString();
            // 2) Validar la reserva
            lugarService.validarReserva(idAsiento, idCliente, idSector, idEvento);
            // 3) Confirmar que es valida
            return ResponseEntity.ok("Reserva valida");
        } catch (RuntimeException e) {
            // 4) Informar el error
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}