
package com.Services;

import com.Entitys.EstudianteEntity;
import com.Repositorys.EstudianteRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    /**
     * Dar de alta un estudiante
     * verifica que no exista el DNI/LU
     * guarda el estudiante en la DB
     */
    public EstudianteEntity guardarEstudiante(EstudianteEntity estudiante) {

        if (estudianteRepository.existsById(estudiante.getDNI())) {
            throw new IllegalArgumentException("DNI existente");
        }
        if (estudianteRepository.FindByNumeroDeLibreta(estudiante.getLU()) != null) {
            throw new IllegalArgumentException("LU existente");
        }
        return estudianteRepository.save(estudiante);
    }

    /**
     * Recupera todos los estudiantes
     * ordenados por DNI
     */
    public List<EstudianteEntity> obtenerEstudiantes() {
        return estudianteRepository.FindAllOrderByDNI();
    }

    /**
     * Recupera un estudiante por numero de libreta universitaria
     */
    public EstudianteEntity obtenerPorLibreta(int libreta) {
        return estudianteRepository.FindByNumeroDeLibreta(libreta);
    }

    /**
     * Recupera todos los estudiantes de un genero determinado
     */
    public List<EstudianteEntity> obtenerPorGenero(String genero) {
        return estudianteRepository.FindByGenero(genero);
    }
}
