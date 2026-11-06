package com.microserviciocliente.servise;

import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.PreferenciasRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PreferenciasService {

    private PreferenciasRepository preferenciasRepository;

    public List<PreferenciasEntity> findByidCliente(String idCliente) {
        return preferenciasRepository.findByidCliente(idCliente);
    }


    public PreferenciasEntity savePreferencia(PreferenciasEntity preferenciasEntity) {
        return preferenciasRepository.save(preferenciasEntity);
    }
}
