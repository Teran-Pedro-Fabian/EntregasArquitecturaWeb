package com.microserviciolugar.cleaner;

import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.LugarRepository;
import com.microserviciolugar.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LugarCleaner {
    @Autowired
    private AsientoRepository asientoRepo;
    @Autowired
    private SectorRepository sectorRepo;
    @Autowired
    private LugarRepository lugarRepo;

    @Transactional
    public void clear() {
        // ORDEN CORRECTO: De la entidad más pequeña a la más grande
        asientoRepo.deleteAll();
        sectorRepo.deleteAll();
        lugarRepo.deleteAll();
        System.out.println("Base de datos de Lugares vaciada");
    }
}