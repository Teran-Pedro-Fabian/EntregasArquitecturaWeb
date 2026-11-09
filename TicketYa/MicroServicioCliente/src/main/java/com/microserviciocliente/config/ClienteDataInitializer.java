package com.microserviciocliente.config;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ClienteDataInitializer implements CommandLineRunner {
    @Autowired
    private ClienteRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.save(new ClienteEntity("1", "Juan Perez"));
            System.out.println("✅ Cliente creado");
        }
    }
}