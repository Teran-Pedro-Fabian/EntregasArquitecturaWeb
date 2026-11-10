package com.Services;

import com.Entitys.CarreraEntity;
import com.Repositorys.CarreraRepository;
import com.dtos.CarreraInscriptosDTO;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public CarreraService(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    /**
     * Recupera todas las carreras
     */
    public List<CarreraEntity> obtenerCarreras() {
        return carreraRepository.findAll();
    }

    /**
     * Recupera una carrera por su ID
     * si no existe lanza EntityNotFoundException
     */
    public CarreraEntity obtenerPorId(int id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la carrera con id: " + id));
    }

    /**
     * Recupera las carreras con estudiantes inscriptos
     * ordenadas por cantidad de inscriptos (de mayor a menor)
     */
    public List<CarreraInscriptosDTO> obtenerCarrerasPorInscriptos() {
        return carreraRepository.FindCarrerasOrderByInscriptos();
    }
}
