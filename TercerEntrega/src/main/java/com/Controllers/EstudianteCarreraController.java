package com.Controllers;

import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteEntity;
import com.Services.EstudianteCarreraService;
import com.dtos.ConteoCarreraAnualDTO;
import com.dtos.EstudianteCarreraConDNIEstudianteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.dtos.ReporteCarreraAnualDTO;

@RestController
@RequestMapping("/EstudianteCarrera")
public class EstudianteCarreraController {

    @Autowired
    private EstudianteCarreraService ECS;

    public EstudianteCarreraController() {}

    @GetMapping
    public List<EstudianteCarrera> findAll() throws Exception {
        return ECS.findAll();
    }

    /*
     * Buscar matricula por PK compuesta
     * DNI del estudiante + ID de carrera
     * No existe -> 404
     */
    @GetMapping("/{dni}/{idCarrera}")
    public EstudianteCarrera findById(
            @PathVariable int dni,
            @PathVariable int idCarrera) throws Exception {

        try {
            return ECS.findById(dni, idCarrera);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }



    /*
     * save() guardaba directamente la matricula
     * Si el estudiante o carrera no existian retornaba 500
     * Modificacion, se toma en cuenta las excepciones del Service
     * Se guarda -> 201
     * Falta data -> 400
     * No existe estudiante/carrera -> 404
     * Duplicados -> 409
     */
    @PostMapping("")
    public ResponseEntity<EstudianteCarrera> save(@RequestBody EstudianteCarrera entity) throws Exception {
        try {
            EstudianteCarrera nueva = ECS.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /*
     * Actualizar matricula por PK compuesta
     * Solo modificar los datos de inscripcion
     * No existe -> 404
     * Datos invalidos -> 400
     */
    @PutMapping("/{dni}/{idCarrera}")
    public EstudianteCarrera update(@PathVariable int dni, @PathVariable int idCarrera, @RequestBody EstudianteCarrera entity) throws Exception {
        try {
            return ECS.update(dni, idCarrera, entity);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /*
     * Eliminar matricula por PK compuesta
     * Elimina -> 204
     * No existe -> 404
     */
    @DeleteMapping("/{dni}/{idCarrera}")
    public ResponseEntity<Void> delete(@PathVariable int dni, @PathVariable int idCarrera) throws Exception {
        if (ECS.delete(dni, idCarrera)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("idCarrera/{id}")
    public List<EstudianteCarrera> findByIdCarrera(@PathVariable int id){
        return ECS.findByIdCarrera(id);
    }

    @GetMapping("idEstudiante/{id}")
    public List<EstudianteCarrera> findByIdEstudiante(@PathVariable int id){
        return ECS.findByEstudiante(id);
    }

    @GetMapping("/OrdenadoPorEstudiante")
    public List<EstudianteCarreraConDNIEstudianteDTO> FindAllOrderByEstudiantes(){
        return ECS.FindAllOrderByEstudiantes();
    }

    @GetMapping("FiltradoPorCarrera/{id}")
    public List<EstudianteCarrera> FindAllEstudianteFilterCarrera(@PathVariable int id){
        return ECS.FindAllEstudianteFilterCarrera(id);
    }

    @GetMapping("FiltradoPorCarreraYCiudad/{idCarrera}/{ciudad}")
    public List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad(@PathVariable int idCarrera,
                                                                    @PathVariable String ciudad){
        return ECS.FindEstudiantesByCarreraAndCiudad(idCarrera,ciudad);
    }

    @GetMapping("/ReporteDeInscriptos")
    public List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio(){
        return ECS.countInscriptosPorCarreraYAnio();
    }

    @GetMapping("/ContarEgresados")
    public List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio(){
        return ECS.countEgresadosPorCarreraYAnio();
    }

    /*
     * Reporte anual de carreras
     * Mostrar inscriptos y egresados por carrera y año
     */
    @GetMapping("/ReporteAnual")
    public List<ReporteCarreraAnualDTO> generarReporteAnual() {
        return ECS.generarReporteAnual();
    }


}
