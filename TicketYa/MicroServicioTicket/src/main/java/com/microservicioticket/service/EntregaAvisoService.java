package com.microservicioticket.service;

import com.microservicioticket.Repository.AvisoCancelacionRepository;
import com.microservicioticket.entity.AvisoCancelacionEntity;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** Entrega el aviso automaticamente a Cliente sin duplicarlo al reintentar */
@Service
public class EntregaAvisoService {

    @Autowired private AvisoCancelacionRepository avisoRepository;
    @Autowired private RestTemplate restTemplate;

    public void entregar(String idEvento, String idCliente) {
        AvisoCancelacionEntity aviso = avisoRepository.findByIdEventoAndIdCliente(
                idEvento, idCliente).orElseThrow(() ->
                new IllegalStateException("Primero debe registrarse el aviso"));

        if (aviso.getFechaEntrega() != null) return;

        // Cliente crea el aviso en su bandeja de manera idempotente
        // Si falla la llamada, NO se marca entregado: permite reintentar
        Map<String, String> datos = Map.of(
                "idEvento", aviso.getIdEvento(),
                "idCliente", aviso.getIdCliente(),
                "mensaje", aviso.getMensaje());
        restTemplate.postForEntity(
                "http://localhost:8081/clientes/interno/notificaciones/cancelacion",
                datos, Void.class);
        aviso.setFechaEntrega(Instant.now());
        avisoRepository.saveAndFlush(aviso);
    }
}
