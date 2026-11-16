package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.entity.CompraEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/** Reintentar avisos de compras confirmadas sin volver a cobrar */
@Service
public class NotificacionCompraService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionCompraService.class);
    @Autowired private CompraRepository compraRepository;
    @Autowired private RestTemplate restTemplate;
    @Value("${ticketya.cliente.url:http://localhost:8081}") private String clienteUrl;

    @Scheduled(fixedDelayString = "${ticketya.notificaciones.intervalo-ms:10000}")
    public void avisarComprasConfirmadas() {
        for (CompraEntity compra : compraRepository
                .findTop50ByEstadoAndNotificacionEnviadaFalseOrderByIdAsc("CONFIRMADA")) {
            if (compra.getIdCliente() == null || compra.getIdEvento() == null) {
                log.warn("Compra {} sin cliente o evento; revisar datos antes de avisar", compra.getId());
                continue;
            }
            try {
                restTemplate.postForEntity(clienteUrl + "/clientes/interno/notificaciones/compra",
                        Map.of("idEvento", compra.getIdEvento(),
                               "idCliente", compra.getIdCliente(),
                               "referencia", "compra-" + compra.getId(),
                               "mensaje", "Tu compra " + compra.getId() + " del evento " + compra.getIdEvento()
                                       + " fue confirmada. Importe: $" + compra.getTotal() + "."),
                        Void.class);
                compra.setNotificacionEnviada(true);
                compraRepository.save(compra);
            } catch (RestClientException ex) {
                log.warn("Compra {}: Cliente no disponible para enviar el aviso: {}",
                        compra.getId(), ex.getMessage());
            }
        }
    }
}
