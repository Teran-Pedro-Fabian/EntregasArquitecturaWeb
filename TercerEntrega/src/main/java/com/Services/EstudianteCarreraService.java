package com.Services;

import com.Entitys.EstudianteCarrera;
import com.Entitys.EstudianteEntity;
import com.Repositorys.EstudianteCarreraRepository;
import com.dtos.ConteoCarreraAnualDTO;
import com.dtos.EstudianteCarreraConDNIEstudianteDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class EstudianteCarreraService implements BaseService<EstudianteCarrera> {

    @Autowired
    private EstudianteCarreraRepository ECRepository;

    public  EstudianteCarreraService() {}

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


    @Override
    public EstudianteCarrera save(EstudianteCarrera entity) throws Exception {
        return ECRepository.save(entity);
    }


    @Override
    public EstudianteCarrera update(Long id, EstudianteCarrera entity) throws Exception {
        int idMatricula = Math.toIntExact(id);
        EstudianteCarrera existente = ECRepository.findById(idMatricula).orElseThrow(() -> new EntityNotFoundException("No existe una matricula con id: " + id));
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
     * y deleteById() para eliminarla
     */
    @Override
    public boolean delete(Long id) throws Exception {
        int idMatricula = Math.toIntExact(id);
        // Verificar si existe antes de eliminar
        if (!ECRepository.existsById(idMatricula)) {
            return false;
        }
        ECRepository.deleteById(idMatricula);
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
}
