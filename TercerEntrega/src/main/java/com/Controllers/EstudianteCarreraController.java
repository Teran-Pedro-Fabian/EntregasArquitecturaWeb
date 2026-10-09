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
     * findById() lanza un NotFoundException
     * cuando no encontraba la matricula
     * El Controller no maneja la excepcion y retorna 500
     * Modificacion, se captura la excepcion y retorna 404
     */
    @GetMapping("/{id}")
    public EstudianteCarrera findById(@PathVariable int id) throws Exception {
        try {
            return ECS.findById(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
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
     * update() lanza un NotFoundException
     * cuando se intenta actualizar una matricula que no existe
     * El Controller no maneja la excepcion y retorna 500
     * Modificacion, se captura la excepcion y retorna 404
     */
    @PutMapping("/{id}")
    public EstudianteCarrera update(@PathVariable Long id, @RequestBody EstudianteCarrera entity) throws Exception {
        try {
            return ECS.update(id, entity);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /*
     * findById() para verificar si existia la matricula
     * pero si no existia lanzaba un NotFoundException
     * por lo que nunca llegaba al else ni retornaba false
     * Modificacion ECS.delete(id) para delegar al Service
     * Elimina -> 204 No Content
     * No existe -> 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        if (ECS.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        // return 404
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



}
