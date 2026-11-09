package com.microServicioEvento.cleaner;

import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventoCleaner {
    @Autowired
    private EventoRepository eventoRepo;
    @Autowired
    private ParticipanteRepository partRepo;
    @Autowired
    private EventoParticipanteRepositoy peRepo;

    public void clear() {
        peRepo.deleteAll();
        eventoRepo.deleteAll();
        partRepo.deleteAll();
        System.out.println("Base de datos de Eventos vaciada");
    }
}