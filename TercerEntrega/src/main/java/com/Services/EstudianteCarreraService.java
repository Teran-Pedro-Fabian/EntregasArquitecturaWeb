package com.Services;

import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteEntity;
import com.Entitys.CarreraEntity;
import com.Repositorys.EstudianteCarreraRepository;
import com.Repositorys.EstudianteRepository;
import com.Repositorys.CarreraRepository;
import com.dtos.ConteoCarreraAnualDTO;
import com.dtos.EstudianteCarreraConDNIEstudianteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import com.dtos.ReporteCarreraAnualDTO;
import java.util.Map;
import java.util.TreeMap;


import java.util.ArrayList;
import java.util.List;

@Service
public class EstudianteCarreraService implements BaseService<EstudianteCarrera> {

    @Autowired
    private EstudianteCarreraRepository ECRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private CarreraRepository carreraRepository;
    
    public EstudianteCarreraService() {}

    @Override
    public List<EstudianteCarrera> findAll() throws Exception {
        return ECRepository.FindAll();
    }

    public List<EstudianteCarrera> findByEstudiante(int idEstudiante){
        return ECRepository.findByEstudiante(idEstudiante);
    }



    public EstudianteCarrera findById(int id) throws Exception {
        return ECRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EstudianteCarrera not found with id: " + id));

    }

    /*
     * Verificar que los años de la matricula sean validos
     * inscripcion > 0
     * graduacion >= 0 (si el estudiante no egreso)
     * egreso > inscripcion
     * La antiguedad no puede ser negativa
     */
    private void validarAnios(EstudianteCarrera entity) {
        if (entity.getInscripcion() <= 0) {
            throw new IllegalArgumentException("Año de inscripcion no valido");
        }
        if (entity.getGraduacion() < 0) {
            throw new IllegalArgumentException("Año de graduacion no valido");
        }
        if (entity.getGraduacion() != 0 && entity.getGraduacion() < entity.getInscripcion()) {
            throw new IllegalArgumentException("La graduacion no puede ser antes de la inscripcion");
        }
        if (entity.getAntiguedad() < 0) {
            throw new IllegalArgumentException("Antiguedad no valida");
        }
    }


    /*
     * save() guardada la matricula directamente
     * sin verificar si el estudiante o la carrera existian
     * Si no existian la DB rechazaba el registro y retornaba 500
     * Modificacion, se verifican las relaciones antes de guardar
     */
    @Override
    public EstudianteCarrera save(EstudianteCarrera entity) throws Exception {
        // Verificar que se reciban estudiante y carrera
        if (entity.getEstudiante() == null || entity.getCarrera() == null) {
            throw new IllegalArgumentException("Debe indicar estudiante y carrera");
        }
        // Verificar que el ID sea valido
        if (entity.getId() <= 0) {
            throw new IllegalArgumentException("ID de matricula no valido");
        }
        validarAnios(entity);
        // Verificar que no exista otra matricula con el mismo ID
        if (ECRepository.existsById(entity.getId())) {
            throw new IllegalStateException("Ya existe una matricula con ese ID");
        }
        // Verificar que exista el estudiante
        EstudianteEntity estudiante = estudianteRepository.findById(entity.getEstudiante().getDNI()).orElseThrow(() -> new EntityNotFoundException("No existe el estudiante"));

        // Verificar que exista la carrera
        CarreraEntity carrera = carreraRepository.findById(entity.getCarrera().getId()).orElseThrow(() -> new EntityNotFoundException("No existe la carrera"));

        /*
         * solo se verifica que el ID de la matricula no estuviera repetido
         * pero un estudiante podia inscribirse dos veces en la misma carrera
         * usando ID diferentes
         * Modificacion, se verifica si ya existe la inscripcion
         * Si existe -> 409
         */
        if (ECRepository.existeInscripcion(estudiante.getDNI(), carrera.getId())) {
            throw new IllegalStateException("El estudiante ya esta inscripto en esa carrera");
        }
        entity.setEstudiante(estudiante);
        entity.setCarrera(carrera);
        return ECRepository.save(entity);
    }


    @Override
    public EstudianteCarrera update(Long id, EstudianteCarrera entity) throws Exception {
        int idMatricula = Math.toIntExact(id);
        EstudianteCarrera existente = ECRepository.findById(idMatricula).orElseThrow(() -> new EntityNotFoundException("No existe una matricula con id: " + id));
        validarAnios(entity);
        existente.setInscripcion(entity.getInscripcion());
        existente.setGraduacion(entity.getGraduacion());
        existente.setAntiguedad(entity.getAntiguedad());

        return ECRepository.save(existente);
    }


    /*
     * findById() verifica si existia la matricula
     * pero si no existia lanzaba una EntityNotFoundException
     * por lo que nunca llegaba al else ni retornaba false
     * Modificacion, existsById() para verificar si existe
     * y delete() para eliminarla
     */
    @Override
    public boolean delete(Long id) throws Exception {
        int idMatricula = Math.toIntExact(id);
        // Verificar si existe antes de eliminar
        if (!ECRepository.existsById(idMatricula)) {
            return false;
        }
        ECRepository.delete(idMatricula);
        return true;
    }


    public List<EstudianteCarrera> findByIdCarrera(int idCarrera){
        return ECRepository.findByIdCarrera(idCarrera);
    }

    public List<EstudianteCarrera> findByIdEstudiante(int idEstudiante){
        return ECRepository.findByEstudiante(idEstudiante);
    }

    public List<EstudianteCarreraConDNIEstudianteDTO> FindAllOrderByEstudiantes(){
        List<EstudianteCarrera> ecs = ECRepository.FindAllOrderByEstudiantes();
        List<EstudianteCarreraConDNIEstudianteDTO>ecDTO = new ArrayList<>();
        for(EstudianteCarrera ecu : ecs){
            ecDTO.add(new EstudianteCarreraConDNIEstudianteDTO(ecu.getEstudiante().getDNI(), ecu));
        }
        return ecDTO;
    }

    public List<EstudianteCarrera> FindAllEstudianteFilterCarrera(int idCarr){
        return ECRepository.FindAllEstudianteFilterCarrera(idCarr);
    }

    public List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad( int idCarrera,String ciudad){
        return ECRepository.FindEstudiantesByCarreraAndCiudad(idCarrera,ciudad);
    }

    public List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio(){
        return ECRepository.countInscriptosPorCarreraYAnio();
    }

    public List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio(){
        return ECRepository.countEgresadosPorCarreraYAnio();
    }

    /*
     * 2.h
     * Juntar los inscriptos y egresados por carrera y año
     * Si en un año no hay inscriptos o egresados se deja en 0
     * Ordenado por carrera y año
     */
    public List<ReporteCarreraAnualDTO> generarReporteAnual() {
        Map<String, Map<Integer, ReporteCarreraAnualDTO>> reporte = new TreeMap<>();
        for (ConteoCarreraAnualDTO dato : ECRepository.countInscriptosPorCarreraYAnio()) {
            Map<Integer, ReporteCarreraAnualDTO> anios = reporte.computeIfAbsent(dato.getCarrera(), c -> new TreeMap<>());
            ReporteCarreraAnualDTO fila = anios.computeIfAbsent(
                    dato.getAnio(),anio -> new ReporteCarreraAnualDTO(dato.getCarrera(), anio, 0, 0));
            fila.setInscriptos(dato.getCantidad());
        }
        for (ConteoCarreraAnualDTO dato : ECRepository.countEgresadosPorCarreraYAnio()) {
            Map<Integer, ReporteCarreraAnualDTO> anios = reporte.computeIfAbsent(dato.getCarrera(), c -> new TreeMap<>());
            ReporteCarreraAnualDTO fila = anios.computeIfAbsent(dato.getAnio(),anio -> new ReporteCarreraAnualDTO(dato.getCarrera(), anio, 0, 0));
            fila.setEgresados(dato.getCantidad());
        }
        List<ReporteCarreraAnualDTO> resultado = new ArrayList<>();
        for (Map<Integer, ReporteCarreraAnualDTO> anios : reporte.values()) {
            resultado.addAll(anios.values());
        }
        return resultado;
    }
}
