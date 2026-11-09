package com.microserviciocliente.cleaner;

import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClienteCleaner {
    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired private PreferenciasRepository prefRepo;

    public void clear() {
        prefRepo.deleteAll();
        clienteRepo.deleteAll();
        System.out.println("Base de datos de Clientes vaciada");
    }
}