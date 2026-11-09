package com.microServicioEvento.config;

import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.ParticipanteEntity;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EventoDataInitializer implements CommandLineRunner {
    @Autowired
    private EventoRepository eventoRepo;
    @Autowired
    private ParticipanteRepository partRepo;

    @Override
    public void run(String... args) {
        if (eventoRepo.count() == 0) {
            EventoEntity e = new EventoEntity();
            e.setNombre("Rock Fest");
            e.setEstado("PUBLICADO");
            eventoRepo.save(e);

            ParticipanteEntity p = new ParticipanteEntity();
            p.setNombre("Coldplay");
            partRepo.save(p);

            System.out.println("✅ Evento y Artista creados");
        }
    }
}