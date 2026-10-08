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

    @GetMapping("/{id}")
    public EstudianteCarrera findById(@PathVariable int id) throws Exception {
        return ECS.findById(id);
    }


    @PostMapping("")
    public EstudianteCarrera save(@RequestBody EstudianteCarrera entity) throws Exception {
        return ECS.save(entity);
    }

    @PutMapping("/{id}")
    public EstudianteCarrera update(@PathVariable Long id, @RequestBody EstudianteCarrera entity) throws Exception {
        return ECS.update(id, entity);
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
