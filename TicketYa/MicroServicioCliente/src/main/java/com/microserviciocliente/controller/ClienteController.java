package com.microserviciocliente.controller;


import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.PreferenciasService;

@RestController
@RequestMapping("/clientes") // Todas las rutas de este controller empiezan con /clientes
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PreferenciasService preferenciasService;

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
}
