package com.Services;

import com.Entitys.CarreraEntity;
import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteCarreraId;
import com.Entitys.EstudianteEntity;
import com.Repositorys.CarreraRepository;
import com.Repositorys.EstudianteCarreraRepository;
import com.Repositorys.EstudianteRepository;
import com.dtos.ConteoCarreraAnualDTO;
import com.dtos.EstudianteCarreraConDNIEstudianteDTO;
import com.dtos.ReporteCarreraAnualDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class EstudianteCarreraService {

    @Autowired
    private EstudianteCarreraRepository ECRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    public EstudianteCarreraService() {}

    public List<EstudianteCarrera> findAll() throws Exception {
        return ECRepository.FindAll();
    }

    public List<EstudianteCarrera> findByEstudiante(int idEstudiante) {
        return ECRepository.findByEstudiante(idEstudiante);
    }

    // Buscar matricula por PK compuesta
    public EstudianteCarrera findById(int dni, int idCarrera) throws Exception {
        EstudianteCarreraId id = new EstudianteCarreraId(dni, idCarrera);

        return ECRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No existe la matricula"));
    }

    /*
     * Verificar que los años de la matricula sean validos
     * inscripcion > 0
     * graduacion >= 0
     * egreso >= inscripcion
     * La antiguedad no puede ser negativa
     */
    private void validarAnios(EstudianteCarrera entity) {
        if (entity.getInscripcion() <= 0) {
            throw new IllegalArgumentException("Año de inscripcion no valido");
        }
        if (entity.getGraduacion() < 0) {
            throw new IllegalArgumentException("Año de graduacion no valido");
        }

        if (entity.getGraduacion() != 0 &&
                entity.getGraduacion() < entity.getInscripcion()) {
            throw new IllegalArgumentException("La graduacion no puede ser antes de la inscripcion");
        }
        if (entity.getAntiguedad() < 0) {
            throw new IllegalArgumentException("Antiguedad no valida");
        }
    }

    /*
     * Guardar matricula
     * Verificar estudiante y carrera
     * Crear PK compuesta
     * Verificar duplicados
     */
    public EstudianteCarrera save(EstudianteCarrera entity) throws Exception {

        if (entity.getEstudiante() == null ||
                entity.getCarrera() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar estudiante y carrera");
        }

        validarAnios(entity);

        // Verificar que exista el estudiante
        EstudianteEntity estudiante = estudianteRepository
                .findById(entity.getEstudiante().getDNI())
                .orElseThrow(() ->
                        new EntityNotFoundException("No existe el estudiante"));

        // Verificar que exista la carrera
        CarreraEntity carrera = carreraRepository
                .findById(entity.getCarrera().getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("No existe la carrera"));

        // Crear PK compuesta
        EstudianteCarreraId id = new EstudianteCarreraId(estudiante.getDNI(), carrera.getId());

        // Verificar que no exista la matricula
        if (ECRepository.existsById(id)) {
            throw new IllegalStateException("El estudiante ya esta inscripto en esa carrera");
        }
        entity.setId(id);
        entity.setEstudiante(estudiante);
        entity.setCarrera(carrera);
        return ECRepository.save(entity);
    }

    /*
     * Actualizar matricula
     * Buscar por DNI y carrera
     * Modificar solo los datos de inscripcion
     */
    public EstudianteCarrera update(int dni, int idCarrera, EstudianteCarrera entity) throws Exception {

        EstudianteCarreraId id = new EstudianteCarreraId(dni, idCarrera);
        EstudianteCarrera existente = ECRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No existe la matricula"));

        validarAnios(entity);
        existente.setInscripcion(entity.getInscripcion());
        existente.setGraduacion(entity.getGraduacion());
        existente.setAntiguedad(entity.getAntiguedad());

        return ECRepository.save(existente);
    }

    /*
     * Eliminar matricula
     * Verificar si existe
     * Eliminar mediante PK compuesta
     */
    public boolean delete(int dni, int idCarrera) throws Exception {

        EstudianteCarreraId id =
                new EstudianteCarreraId(dni, idCarrera);

        if (!ECRepository.existsById(id)) {
            return false;
        }

        ECRepository.deleteById(id);
        return true;
    }

    public List<EstudianteCarrera> findByIdCarrera(int idCarrera) {
        return ECRepository.findByIdCarrera(idCarrera);
    }

    public List<EstudianteCarrera> findByIdEstudiante(int idEstudiante) {
        return ECRepository.findByEstudiante(idEstudiante);
    }

    public List<EstudianteCarreraConDNIEstudianteDTO> FindAllOrderByEstudiantes() {

        List<EstudianteCarrera> ecs =
                ECRepository.FindAllOrderByEstudiantes();

        List<EstudianteCarreraConDNIEstudianteDTO> ecDTO =
                new ArrayList<>();

        for (EstudianteCarrera ecu : ecs) {
            ecDTO.add(new EstudianteCarreraConDNIEstudianteDTO(
                    ecu.getEstudiante().getDNI(), ecu));
        }
        return ecDTO;
    }

    public List<EstudianteCarrera> FindAllEstudianteFilterCarrera(int idCarr) {
        return ECRepository.FindAllEstudianteFilterCarrera(idCarr);
    }

    public List<EstudianteEntity> FindEstudiantesByCarreraAndCiudad(
            int idCarrera, String ciudad) {

        return ECRepository.FindEstudiantesByCarreraAndCiudad(
                idCarrera, ciudad);
    }

    public List<ConteoCarreraAnualDTO> countInscriptosPorCarreraYAnio() {
        return ECRepository.countInscriptosPorCarreraYAnio();
    }

    public List<ConteoCarreraAnualDTO> countEgresadosPorCarreraYAnio() {
        return ECRepository.countEgresadosPorCarreraYAnio();
    }

    /*
     * 2.h
     * Juntar inscriptos y egresados por carrera y año
     * Si no hay registros se deja en 0
     * Ordenar por carrera y año
     */
    public List<ReporteCarreraAnualDTO> generarReporteAnual() {

        Map<String, Map<Integer, ReporteCarreraAnualDTO>> reporte =
                new TreeMap<>();

        for (ConteoCarreraAnualDTO dato :
                ECRepository.countInscriptosPorCarreraYAnio()) {

            Map<Integer, ReporteCarreraAnualDTO> anios =
                    reporte.computeIfAbsent(
                            dato.getCarrera(), c -> new TreeMap<>());

            ReporteCarreraAnualDTO fila = anios.computeIfAbsent(
                    dato.getAnio(),
                    anio -> new ReporteCarreraAnualDTO(
                            dato.getCarrera(), anio, 0, 0));

            fila.setInscriptos(dato.getCantidad());
        }

        for (ConteoCarreraAnualDTO dato :
                ECRepository.countEgresadosPorCarreraYAnio()) {

            Map<Integer, ReporteCarreraAnualDTO> anios =
                    reporte.computeIfAbsent(
                            dato.getCarrera(), c -> new TreeMap<>());

            ReporteCarreraAnualDTO fila = anios.computeIfAbsent(
                    dato.getAnio(),
                    anio -> new ReporteCarreraAnualDTO(
                            dato.getCarrera(), anio, 0, 0));

            fila.setEgresados(dato.getCantidad());
        }
        List<ReporteCarreraAnualDTO> resultado = new ArrayList<>();
        for (Map<Integer, ReporteCarreraAnualDTO> anios : reporte.values()) {
            resultado.addAll(anios.values());
        }
        return resultado;
    }
}
