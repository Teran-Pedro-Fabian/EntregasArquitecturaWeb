package com.microserviciocliente.servise;

import com.microserviciocliente.client.ApiService;
import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PreferenciasService preferenciasService;
    @Autowired 
    private ApiService apiService;

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

    /**
     * Actualiza el correo del cliente para el envio de notificaciones por SMTP
     *
     * @param idCliente ID del cliente
     * @param email nuevo correo a registrar
     * @return cliente actualizado
     */
    public ClienteEntity actualizarEmail(String idCliente, String email) {
        ClienteEntity cliente = findById(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        cliente.setEmail(email);
        return clienteRepository.save(cliente);
    }
}
