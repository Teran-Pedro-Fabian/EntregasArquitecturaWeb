package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private ClienteRepository clienteRepository;

    public ClienteEntity save(ClienteEntity clienteEntity){
        return clienteRepository.save(clienteEntity);

    }

    public ClienteEntity findById(String idCliente){
        return clienteRepository.findById(idCliente).get();
    }
}
