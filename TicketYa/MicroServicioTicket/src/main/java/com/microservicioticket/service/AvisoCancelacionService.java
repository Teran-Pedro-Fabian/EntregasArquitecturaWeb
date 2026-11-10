package com.microservicioticket.service;

import com.microservicioticket.Repository.AvisoCancelacionRepository;
import com.microservicioticket.entity.AvisoCancelacionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class AvisoCancelacionService {

    @Autowired
    private AvisoCancelacionRepository avisoRepository;

    /** Registrar un aviso persistente por cliente y evento. */
    @Transactional
    public void registrar(String idEvento, String idCliente) {
        if (idEvento == null || idEvento.isBlank() || idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Datos del aviso invalidos");
        }
        if (avisoRepository.existsByIdEventoAndIdCliente(idEvento, idCliente)) return;

        AvisoCancelacionEntity aviso = new AvisoCancelacionEntity();
        aviso.setIdEvento(idEvento);
        aviso.setIdCliente(idCliente);
        aviso.setMensaje("El evento " + idEvento
                + " fue cancelado. Las entradas vendidas se encuentran en proceso de reintegro.");
        aviso.setFechaCreacion(Instant.now());
        avisoRepository.saveAndFlush(aviso);
    }

    @Transactional(readOnly = true)
    public List<AvisoCancelacionEntity> listarPorCliente(String idCliente) {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Cliente no valido");
        }
        return avisoRepository.findByIdClienteOrderByFechaCreacionDesc(idCliente);
    }

    public long contarEntregados(String idEvento) {
        return avisoRepository.countByIdEventoAndFechaEntregaIsNotNull(idEvento);
    }

    public long contarPorEvento(String idEvento) {
        return avisoRepository.countByIdEvento(idEvento);
    }
}
