package com.microserviciocliente.servise;

import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.PreferenciasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreferenciasService {

    @Autowired
    private PreferenciasRepository preferenciasRepository;

    /**
     * Busca las preferencias de un cliente por su ID
     *
     * @param idCliente ID del cliente
     * @return lista de preferencias del cliente
     */
    public List<PreferenciasEntity> findByidCliente(String idCliente) {
        return preferenciasRepository.findByidCliente(idCliente);
    }

    /**
     * Guarda las preferencias de un cliente en la BD
     *
     * @param preferenciasEntity preferencias que queremos guardar
     * @return preferencias guardadas
     */
    public PreferenciasEntity savePreferencia(PreferenciasEntity preferenciasEntity) {
        return preferenciasRepository.save(preferenciasEntity);
    }
}