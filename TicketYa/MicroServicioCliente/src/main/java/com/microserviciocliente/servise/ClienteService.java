package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Guarda un cliente en la base de datos
     *
     * @param clienteEntity cliente que queremos guardar
     * @return cliente guardado
     */
    public ClienteEntity save(ClienteEntity clienteEntity) {
        return clienteRepository.save(clienteEntity);
    }

    /**
     * Busca un cliente por su ID
     *
     * @param idCliente ID del cliente
     * @return cliente encontrado o null si no existe
     */
    public ClienteEntity findById(String idCliente) {
        return clienteRepository.findById(idCliente).orElse(null);
    }
}