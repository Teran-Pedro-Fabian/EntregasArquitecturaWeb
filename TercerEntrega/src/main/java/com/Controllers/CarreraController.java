package com.Controllers;

import com.Entitys.CarreraEntity;
import com.Services.CarreraService;
import com.dtos.CarreraInscriptosDTO;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/Carrera")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @GetMapping
    public List<CarreraEntity> obtenerCarreras() {
        return carreraService.obtenerCarreras();
    }

    /**
     * 2F
     */
    @GetMapping("/inscriptos")
    public List<CarreraInscriptosDTO> obtenerCarrerasPorInscriptos() {
        return carreraService.obtenerCarrerasPorInscriptos();
    }

    /*
     * Si la carrera no existe -> 404
     */
    @GetMapping("/{id}")
    public CarreraEntity obtenerPorId(@PathVariable int id) {
        try {
            return carreraService.obtenerPorId(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
