package com.microserviciocliente.servise;

import com.microserviciocliente.dto.NotificacionCancelacionRequest;
import com.microserviciocliente.dto.NotificacionRequest;
import com.microserviciocliente.dto.NotificacionPublicacionRequest;
import com.microserviciocliente.entity.NotificacionEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.NotificacionRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private PreferenciasRepository preferenciasRepository;

    /** Ahoira conservar el endpoint que se usa Ticket para las cancelaciones */
    @Transactional
    public NotificacionEntity recibirCancelacion(NotificacionCancelacionRequest datos) {
        if (datos == null) throw new IllegalArgumentException("Datos invalidos");
        return registrar("CANCELACION", "evento", datos.getIdEvento(),
                datos.getIdCliente(), datos.getMensaje());
    }

    @Transactional
    public NotificacionEntity recibirCompra(NotificacionRequest datos) {
        return recibir("COMPRA", datos);
    }

    @Transactional
    public NotificacionEntity recibirCambio(NotificacionRequest datos) {
        return recibir("CAMBIO", datos);
    }

    private NotificacionEntity recibir(String tipo, NotificacionRequest datos) {
        if (datos == null) throw new IllegalArgumentException("Datos invalidos");
        return registrar(tipo, datos.getReferencia(), datos.getIdEvento(),
                datos.getIdCliente(), datos.getMensaje());
    }

    /** Avisar una sola vez a cada cliente cuyo artista favorito actua en su ciudad */
    @Transactional
    public int recibirPublicacion(NotificacionPublicacionRequest datos) {
        if (datos == null || vacio(datos.getIdEvento()) || vacio(datos.getNombreEvento()) ||
                datos.getIdsParticipantes() == null || datos.getIdsParticipantes().isEmpty()) {
            throw new IllegalArgumentException("Datos de publicacion invalidos");
        }
        List<PreferenciasEntity> preferencias = preferenciasRepository
                .findByIdParticipanteIn(datos.getIdsParticipantes());
        Set<String> destinatarios = new HashSet<>();
        for (PreferenciasEntity p : preferencias) {
            if (vacio(p.getIdCliente())) continue;
            boolean mismaCiudad = !vacio(datos.getCiudad()) && !vacio(p.getCiudad()) &&
                    datos.getCiudad().trim().equalsIgnoreCase(p.getCiudad().trim());
            // Compatibilidad con preferencias antiguas: mismo lugar implica misma ciudad
            boolean mismoLugar = !vacio(datos.getIdLugar()) && !vacio(p.getIdLugar()) &&
                    datos.getIdLugar().equals(p.getIdLugar());
            if (mismaCiudad || mismoLugar) destinatarios.add(p.getIdCliente());
        }
        for (String idCliente : destinatarios) {
            registrar("ARTISTA", "evento", datos.getIdEvento(), idCliente,
                    "Un artista que seguis participa en " + datos.getNombreEvento() +
                            (vacio(datos.getCiudad()) ? "." : " en " + datos.getCiudad() + "."));
        }
        return destinatarios.size();
    }

    /** Un evento puede tener varios tipos de avisos y varios cambios */
    @Transactional
    public NotificacionEntity registrar(String tipo, String referencia, String idEvento,
                                        String idCliente, String mensaje) {
        if (vacio(tipo) || vacio(referencia) || vacio(idEvento) ||
                vacio(idCliente) || vacio(mensaje) || mensaje.length() > 500) {
            throw new IllegalArgumentException("Datos de la notificacion invalidos");
        }
        return notificacionRepository.findByTipoAndReferenciaAndIdEventoAndIdCliente(
                tipo, referencia, idEvento, idCliente).orElseGet(() -> {
            NotificacionEntity n = new NotificacionEntity();
            n.setTipo(tipo);
            n.setReferencia(referencia);
            n.setIdEvento(idEvento);
            n.setIdCliente(idCliente);
            n.setMensaje(mensaje);
            n.setFechaCreacion(Instant.now());
            return notificacionRepository.saveAndFlush(n);
        });
    }

    @Transactional(readOnly = true)
    public List<NotificacionEntity> listarPorCliente(String idCliente) {
        if (vacio(idCliente)) throw new IllegalArgumentException("Cliente no valido");
        return notificacionRepository.findByIdClienteOrderByFechaCreacionDesc(idCliente);
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}
