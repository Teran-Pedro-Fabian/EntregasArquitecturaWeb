
package com.Controllers;

import com.Entitys.EstudianteEntity;
import com.Services.EstudianteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/Estudiante")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /**
     * 2A
     */
    @PostMapping
    public ResponseEntity<EstudianteEntity> guardarEstudiante(@RequestBody EstudianteEntity estudiante) {
        try {
            EstudianteEntity nuevo = estudianteService.guardarEstudiante(estudiante);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    /**
     * 2C
     */
    @GetMapping
    public List<EstudianteEntity> obtenerEstudiantes() {
        return estudianteService.obtenerEstudiantes();
    }

    /**
     * 2D
     */
    @GetMapping("/libreta/{lu}")
    public EstudianteEntity obtenerPorLibreta(@PathVariable int lu) {
        EstudianteEntity estudiante = estudianteService.obtenerPorLibreta(lu);
        if (estudiante == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado");
        }
        return estudiante;
    }

    /**
     * 2E
     */
    @GetMapping("/genero/{genero}")
    public List<EstudianteEntity> obtenerPorGenero(@PathVariable String genero) {
        return estudianteService.obtenerPorGenero(genero);
    }
}
