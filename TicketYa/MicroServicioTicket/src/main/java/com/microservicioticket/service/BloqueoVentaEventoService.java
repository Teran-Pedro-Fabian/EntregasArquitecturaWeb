package com.microservicioticket.service;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Una transaccion de compra mantiene el bloqueo del evento hasta su COMMIT.
 * Al cancelar esperamos a que terminen las compras ya iniciadas y cortamos las nuevas.
 */
@Service
public class BloqueoVentaEventoService {

    @Autowired private EntityManager entityManager;

    /** Se llama dentro de la transaccion de compra, antes del bloqueo de cliente. */
    public void permitirCompra(String idEvento) {
        bloquearFilaLogica(idEvento);
        if (estaBloqueado(idEvento)) {
            throw new IllegalStateException("El evento esta cancelado: no admite nuevas compras");
        }
    }

    /** Solo adquirir el bloqueo, para recuperar una compra previa sin crear otra. */
    public void bloquearOperacion(String idEvento) {
        bloquearFilaLogica(idEvento);
    }

    public boolean estaBloqueado(String idEvento) {
        Number cantidad = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM evento_bloqueo_venta WHERE id_evento = :idEvento")
                .setParameter("idEvento", idEvento)
                .getSingleResult();
        return cantidad.longValue() > 0;
    }

    /**
     * Llamado por Evento ANTES de guardar CANCELADO en MongoDB.
     * Espera el commit de cualquier compra que estuviera en curso.
     */
    @Transactional
    public void bloquearDefinitivamente(String idEvento) {
        if (idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Evento no valido");
        }
        bloquearFilaLogica(idEvento);
        entityManager.createNativeQuery(
                "INSERT INTO evento_bloqueo_venta(id_evento) VALUES (:idEvento) " +
                "ON CONFLICT (id_evento) DO NOTHING")
                .setParameter("idEvento", idEvento)
                .executeUpdate();
    }

    private void bloquearFilaLogica(String idEvento) {
        if (idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Evento no valido");
        }
        entityManager.createNativeQuery(
                "SELECT 1 FROM pg_advisory_xact_lock(CAST(hashtext(:clave) AS BIGINT))")
                .setParameter("clave", "ticketya|evento|" + idEvento)
                .getSingleResult();
    }
}
