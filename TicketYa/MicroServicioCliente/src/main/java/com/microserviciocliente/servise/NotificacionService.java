package com.microserviciocliente.servise;

import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;

    /** Registrar el aviso que envia Ticket, si ya estaba, devolver el mismo */
    @Transactional
    public NotificacionEntity recibirCancelacion(NotificacionCancelacionRequest datos) {
        if (datos == null || vacio(datos.getIdEvento()) || vacio(datos.getIdCliente())
                || vacio(datos.getMensaje())) {
            throw new IllegalArgumentException("Datos de la notificacion invalidos");
        }
        return notificacionRepository.findByIdEventoAndIdCliente(
                datos.getIdEvento(), datos.getIdCliente()).orElseGet(() -> {
            NotificacionEntity notificacion = new NotificacionEntity();
            notificacion.setIdEvento(datos.getIdEvento());
            notificacion.setIdCliente(datos.getIdCliente());
            notificacion.setMensaje(datos.getMensaje());
            notificacion.setFechaCreacion(Instant.now());
            return notificacionRepository.saveAndFlush(notificacion);
        });
    }

    /** Avisos de un cliente */
    @Transactional(readOnly = true)
    public List<NotificacionEntity> listarPorCliente(String idCliente) {
        if (vacio(idCliente)) throw new IllegalArgumentException("Cliente no valido");
        return notificacionRepository.findByIdClienteOrderByFechaCreacionDesc(idCliente);
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}
