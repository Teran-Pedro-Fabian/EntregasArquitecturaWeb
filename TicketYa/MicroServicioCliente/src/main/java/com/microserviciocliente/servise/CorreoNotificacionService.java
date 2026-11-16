package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

/** Envia correos fuera de la transaccion REST (con reintentos) */
@Service
public class CorreoNotificacionService {
    private static final Logger log = LoggerFactory.getLogger(CorreoNotificacionService.class);

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private JavaMailSender mailSender;

    @Value("${ticketya.notificaciones.email.habilitado:true}")
    private boolean habilitado;
    @Value("${ticketya.notificaciones.email.desde:notificaciones@ticketya.local}")
    private String remitente;

    @Scheduled(fixedDelayString = "${ticketya.notificaciones.intervalo-ms:10000}")
    public void enviarPendientes() {
        if (!habilitado) return;
        List<NotificacionEntity> pendientes = notificacionRepository
                .findTop50ByEstadoEmailInAndIntentosEmailLessThanOrderByFechaCreacionAsc(
                        List.of("PENDIENTE", "ERROR"), 5);
        for (NotificacionEntity n : pendientes) {
            ClienteEntity cliente = clienteRepository.findById(n.getIdCliente()).orElse(null);
            if (cliente == null || cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                n.setEstadoEmail("SIN_EMAIL");
                n.setUltimoError("El cliente no tiene email registrado");
                notificacionRepository.save(n);
                continue;
            }
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(remitente);
                mail.setTo(cliente.getEmail());
                mail.setSubject("TicketYa - " + n.getTipo());
                mail.setText(n.getMensaje());
                mailSender.send(mail);
                n.setEstadoEmail("ENVIADO");
                n.setUltimoError(null);
                notificacionRepository.save(n);
            } catch (RuntimeException ex) {
                n.setIntentosEmail(n.getIntentosEmail() + 1);
                n.setEstadoEmail("ERROR");
                n.setUltimoError(ex.getMessage() == null ? "No se pudo enviar" :
                        ex.getMessage().substring(0, Math.min(500, ex.getMessage().length())));
                notificacionRepository.save(n);
                log.warn("No se pudo enviar notificacion {}: {}", n.getId(), ex.getMessage());
            }
        }
    }

    /** Si se registra un email, reactivar los avisos que estaban esperando destinatario */
    public void reactivarSinEmail(String idCliente) {
        for (NotificacionEntity n : notificacionRepository.findByIdClienteAndEstadoEmail(idCliente, "SIN_EMAIL")) {
            n.setEstadoEmail("PENDIENTE");
            n.setUltimoError(null);
            notificacionRepository.save(n);
        }
    }
}
