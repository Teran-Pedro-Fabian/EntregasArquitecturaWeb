package com.microservicioticket.service;

import com.microservicioticket.Repository.CompraRepository;
import com.microservicioticket.Repository.TicketRepository;
import com.microservicioticket.dto.CompraRequest;
import com.microservicioticket.dto.CompraResponse;
import com.microservicioticket.dto.CompraGeneralRequest;
import com.microservicioticket.entity.CompraEntity;
import com.microservicioticket.entity.TicketEntity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CompraService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CompraRepository compraRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PagoMockService pagoMockService;
    @Autowired
    private BloqueoVentaEventoService bloqueoVentaEventoService;

    @Value("${ticketya.compra.max-entradas:4}")
    private int maxEntradas;

    /**
     * Comprar uno o varios asientos numerados
     * Consultar precio y reservas antes de solicitar el pago
     */
    @Transactional
    public CompraResponse comprar(CompraRequest request) {
        // 1) Verificar los datos de la compra
        validarDatos(request);

        List<Long> ids = request.getIdsAsientos();
        String idCliente = request.getIdCliente();
        String idEvento = request.getIdEvento();
        Long idSector = request.getIdSector();

        // Punto 5: serializar compras del mismo cliente y evento usando PostgreSQL
        // Funciona aunque existan varias instancias del microservicio
        // Misma cerradura que usa la cancelacion. Hasta que termine este COMMIT
        // el administrador no puede cerrar el evento ni comenzar reintegros.
        bloqueoVentaEventoService.bloquearOperacion(idEvento);
        bloquearComprasClienteEvento(idCliente, idEvento);
        String huella = huellaNumerada(request);
        String clave = claveNumerada(request);
        CompraEntity anterior = compraRepository.findByClaveIdempotencia(clave).orElse(null);
        if (anterior != null) {
            verificarReintento(anterior, huella);
            if (bloqueoVentaEventoService.estaBloqueado(idEvento) &&
                    "PENDIENTE_CONFIRMACION".equals(anterior.getEstado())) {
                throw new IllegalStateException("Evento cancelado, compra pendiente de revision");
            }
            return reintentarCompra(anterior);
        }
        if (bloqueoVentaEventoService.estaBloqueado(idEvento)) {
            throw new IllegalStateException("El evento no admite nuevas compras");
        }

        // 2) Comprobar que el evento esta publicado
        String urlEvento = "http://localhost:8082/evento/" + idEvento;
        Map<String, Object> evento = restTemplate.getForObject(urlEvento, Map.class);
        if (evento == null || !"PUBLICADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta publicado");
        }

        // 3) Verificar que el cliente no supere el limite
        long activos = ticketRepository.contarActivos(idCliente, idEvento);
        if (activos + ids.size() > maxEntradas) {
            throw new IllegalStateException("Limite de entradas por cliente y evento superado");
        }

        // 4) Consultar el precio oficial de Lugar
        String urlPrecio = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8083/lugares/interno/sectores/" + idSector + "/precio")
                .queryParam("idEvento", idEvento).toUriString();
        Map<String, Object> datosPrecio = restTemplate.getForObject(urlPrecio, Map.class);
        if (datosPrecio == null || !(datosPrecio.get("precio") instanceof Number)) {
            throw new IllegalStateException("No se pudo consultar el precio del sector");
        }
        Double precioUnitario = ((Number) datosPrecio.get("precio")).doubleValue();
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalStateException("Precio del sector no valido");
        }

        // 5) Validar que el cliente tenga reservados todos los asientos
        for (Long idAsiento : ids) {
            Map<String, Object> datosReserva = Map.of(
                    "idAsiento", idAsiento,
                    "idCliente", idCliente,
                    "idSector", idSector,
                    "idEvento", idEvento);
            restTemplate.postForEntity(
                    "http://localhost:8083/lugares/interno/reservas/validar",
                    datosReserva, String.class);
        }

        // 6) Calcular el total desde el precio oficial
        Double total = Math.round(precioUnitario * ids.size() * 100.0) / 100.0;

        // 7) Simular el pago de la compra
        boolean aprobado = pagoMockService.procesarPago(idCliente, total);
        if (!aprobado) {
            // Rechazar el pago y liberar las reservas del cliente
            cancelarReservas(ids, idCliente);
            throw new IllegalStateException("El pago fue rechazado");
        }

        // 8) Registrar la compra y el medio de pago
        CompraEntity compra = new CompraEntity();
        compra.setClaveIdempotencia(clave);
        compra.setHuellaSolicitud(huella);
        compra.setIdCliente(idCliente);
        compra.setIdEvento(idEvento);
        compra.setIdSector(idSector);
        compra.setCantidadEntradas(ids.size());
        compra.setTotal(total);
        compra.setMedioPago(request.getMedioPago().toUpperCase());
        compra.setEstado("PENDIENTE_CONFIRMACION");
        compra.setFechaCompra(Instant.now());
        compra = compraRepository.saveAndFlush(compra);

        // 9) Generar un ticket con QR unico por cada asiento
        List<TicketEntity> tickets = new ArrayList<>();
        for (Long idAsiento : ids) {
            TicketEntity ticket = new TicketEntity();
            ticket.setIdCompra(compra.getId());
            ticket.setIdCliente(idCliente);
            ticket.setIdEvento(idEvento);
            ticket.setIdSector(idSector);
            ticket.setIdAsiento(idAsiento);
            ticket.setPrecio(precioUnitario);
            ticket.setQrCode("TICKETYA-" + UUID.randomUUID().toString().toUpperCase());
            ticket.setEstadoDevolucion("SIN_SOLICITUD");
            ticket.setUsado(false);
            tickets.add(ticketRepository.saveAndFlush(ticket));
        }

        // 10) Confirmar los asientos. Un error de red deja PENDIENTE
        // en lugar de borrar tickets que Lugar pudo haber vendido
        return completarCompraNumerada(compra, tickets);
    }

    /**
     * Comprar entradas de un sector NO_NUMERADO
     * El stock se confirma en Lugar con bloqueo del sector
     */
    @Transactional
    public CompraResponse comprarGeneral(CompraGeneralRequest request) {
        validarDatosGeneral(request);

        String idCliente = request.getIdCliente();
        String idEvento = request.getIdEvento();
        Long idSector = request.getIdSector();
        Long idReserva = request.getIdReserva();
        int cantidad = request.getCantidad();

        bloqueoVentaEventoService.bloquearOperacion(idEvento);
        bloquearComprasClienteEvento(idCliente, idEvento);
        String huella = huellaGeneral(request);
        String clave = claveGeneral(request);
        CompraEntity anterior = compraRepository.findByClaveIdempotencia(clave).orElse(null);
        if (anterior != null) {
            verificarReintento(anterior, huella);
            if (bloqueoVentaEventoService.estaBloqueado(idEvento) &&
                    "PENDIENTE_CONFIRMACION".equals(anterior.getEstado())) {
                throw new IllegalStateException("Evento cancelado, compra pendiente de revision");
            }
            return reintentarCompra(anterior);
        }
        if (bloqueoVentaEventoService.estaBloqueado(idEvento)) {
            throw new IllegalStateException("El evento no admite nuevas compras");
        }

        // 1) Comprobar que el evento esta publicado
        Map<String, Object> evento = restTemplate.getForObject(
                "http://localhost:8082/evento/" + idEvento, Map.class);
        if (evento == null || !"PUBLICADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta publicado");
        }

        // 2) Verificar el limite de entradas del cliente
        long activos = ticketRepository.contarActivos(idCliente, idEvento);
        if (activos + cantidad > maxEntradas) {
            throw new IllegalStateException("Limite de entradas por cliente y evento superado");
        }

        // 3) Consultar precio desde Lugar, nunca desde el cliente
        String urlPrecio = UriComponentsBuilder.fromHttpUrl(
                "http://localhost:8083/lugares/interno/sectores/" + idSector + "/precio")
                .queryParam("idEvento", idEvento).toUriString();
        Map<String, Object> datosPrecio = restTemplate.getForObject(urlPrecio, Map.class);
        if (datosPrecio == null || !(datosPrecio.get("precio") instanceof Number)) {
            throw new IllegalStateException("No se pudo consultar el precio general");
        }
        double precioUnitario = ((Number) datosPrecio.get("precio")).doubleValue();
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalStateException("Precio del sector no valido");
        }

        // 4) Comprobar la reserva general de este cliente
        Map<String, Object> datosReserva = Map.of(
                "idSector", idSector, "idReserva", idReserva,
                "idCliente", idCliente, "idEvento", idEvento, "cantidad", cantidad);
        restTemplate.postForEntity(
                "http://localhost:8083/lugares/interno/sectores/generales/reservas/validar",
                datosReserva, String.class);

        double total = Math.round(precioUnitario * cantidad * 100.0) / 100.0;

        // 5) Simular el pago y cancelar la reserva si fue rechazado
        boolean aprobado = pagoMockService.procesarPago(idCliente, total);
        if (!aprobado) {
            cancelarReservaGeneral(idReserva, idSector, idCliente);
            throw new IllegalStateException("El pago fue rechazado");
        }

        // 6) Guardar la compra y un QR por cada entrada general
        CompraEntity compra = new CompraEntity();
        compra.setClaveIdempotencia(clave);
        compra.setHuellaSolicitud(huella);
        compra.setIdCliente(idCliente);
        compra.setIdEvento(idEvento);
        compra.setIdSector(idSector);
        compra.setIdReservaGeneral(idReserva);
        compra.setCantidadEntradas(cantidad);
        compra.setTotal(total);
        compra.setMedioPago(request.getMedioPago().toUpperCase());
        compra.setEstado("PENDIENTE_CONFIRMACION");
        compra.setFechaCompra(Instant.now());
        compra = compraRepository.saveAndFlush(compra);

        List<TicketEntity> tickets = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            TicketEntity ticket = new TicketEntity();
            ticket.setIdCompra(compra.getId());
            ticket.setIdCliente(idCliente);
            ticket.setIdEvento(idEvento);
            ticket.setIdSector(idSector);
            ticket.setIdAsiento(null);
            ticket.setIdReservaGeneral(idReserva);
            ticket.setPrecio(precioUnitario);
            ticket.setQrCode("TICKETYA-" + UUID.randomUUID().toString().toUpperCase());
            ticket.setEstadoDevolucion("SIN_SOLICITUD");
            ticket.setUsado(false);
            ticket = ticketRepository.saveAndFlush(ticket);
            tickets.add(ticket);
        }

        // 7) Confirmar los cupos en Lugar con los mismos IDs en cada reintento
        return completarCompraGeneral(compra, tickets);
    }

    /**
     * Reintenta una compra pendiente sin cobrar nuevamente ni crear otro QR.
     * Es util cuando la respuesta de Lugar se pierde despues de confirmar.
     */
    @Transactional
    public CompraResponse reintentarConfirmacion(Long idCompra) {
        CompraEntity compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        bloqueoVentaEventoService.bloquearOperacion(compra.getIdEvento());
        bloquearComprasClienteEvento(compra.getIdCliente(), compra.getIdEvento());
        entityManager.refresh(compra);
        if (bloqueoVentaEventoService.estaBloqueado(compra.getIdEvento()) &&
                "PENDIENTE_CONFIRMACION".equals(compra.getEstado())) {
            throw new IllegalStateException("Evento cancelado: recuperacion solo por el proceso de reintegros");
        }
        return reintentarCompra(compra);
    }

    /** Recuperacion interna durante cancelacion: no procesa un segundo pago. */
    @Transactional
    public CompraResponse reintentarParaCancelacion(Long idCompra) {
        CompraEntity compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        bloqueoVentaEventoService.bloquearOperacion(compra.getIdEvento());
        bloquearComprasClienteEvento(compra.getIdCliente(), compra.getIdEvento());
        entityManager.refresh(compra);
        if (!bloqueoVentaEventoService.estaBloqueado(compra.getIdEvento())) {
            throw new IllegalStateException("El evento aun admite ventas");
        }
        return reintentarCompra(compra);
    }

    private CompraResponse reintentarCompra(CompraEntity compra) {
        List<TicketEntity> tickets = ticketRepository.findByIdCompra(compra.getId());
        if ("CONFIRMADA".equals(compra.getEstado())) return respuesta(compra, tickets);
        if (!"PENDIENTE_CONFIRMACION".equals(compra.getEstado())) {
            throw new IllegalStateException("La compra no puede reintentarse en estado " + compra.getEstado());
        }
        if (tickets.isEmpty() || tickets.size() != compra.getCantidadEntradas()) {
            throw new IllegalStateException("La compra pendiente tiene tickets incompletos");
        }
        if (compra.getIdReservaGeneral() != null) return completarCompraGeneral(compra, tickets);
        return completarCompraNumerada(compra, tickets);
    }

    /**
     * Cada asiento acepta solo su mismo ID de ticket en el reintento.
     * Si Lugar no responde guardamos PENDIENTE_CONFIRMACION para recuperacion.
     */
    private CompraResponse completarCompraNumerada(CompraEntity compra, List<TicketEntity> tickets) {
        try {
            for (TicketEntity ticket : tickets) {
                restTemplate.postForEntity("http://localhost:8083/lugares/asiento/confirmar",
                        Map.of("idAsiento", ticket.getIdAsiento(),
                               "idCliente", compra.getIdCliente(),
                               "idTicket", ticket.getId()), String.class);
            }
        } catch (RestClientException e) {
            // No liberar asientos en un resultado ambiguo: Lugar pudo haberlos vendido
            System.err.println("Compra " + compra.getId() + " pendiente de confirmacion numerada: "
                    + e.getMessage());
            return respuesta(compra, tickets);
        }
        compra.setEstado("CONFIRMADA");
        compraRepository.saveAndFlush(compra);
        return respuesta(compra, tickets);
    }

    /**
     * Lugar ya es idempotente para una reserva general vendida
     * Reusar idCompra y los mismos tickets evita gastar cupos dos veces
     */
    private CompraResponse completarCompraGeneral(CompraEntity compra, List<TicketEntity> tickets) {
        try {
            List<Long> idsTickets = tickets.stream().map(TicketEntity::getId).toList();
            restTemplate.postForEntity(
                    "http://localhost:8083/lugares/interno/sectores/generales/confirmar",
                    Map.of("idSector", compra.getIdSector(),
                           "idReserva", compra.getIdReservaGeneral(),
                           "idCliente", compra.getIdCliente(),
                           "idCompra", compra.getId(),
                           "idsTickets", idsTickets), String.class);
        } catch (RestClientException e) {
            System.err.println("Compra " + compra.getId() + " pendiente de confirmacion general: "
                    + e.getMessage());
            return respuesta(compra, tickets);
        }
        compra.setEstado("CONFIRMADA");
        compraRepository.saveAndFlush(compra);
        return respuesta(compra, tickets);
    }

    private CompraResponse respuesta(CompraEntity compra, List<TicketEntity> tickets) {
        return new CompraResponse(compra.getId(), compra.getEstado(),
                compra.getMedioPago(), compra.getTotal(), tickets);
    }

    private void verificarReintento(CompraEntity anterior, String huella) {
        if (!huella.equals(anterior.getHuellaSolicitud())) {
            throw new IllegalStateException(
                    "La clave de idempotencia ya se utilizo con otros datos");
        }
    }

    private String huellaNumerada(CompraRequest request) {
        List<Long> ids = new ArrayList<>(request.getIdsAsientos());
        Collections.sort(ids);
        return hash("NUM|" + request.getIdCliente() + "|" + request.getIdEvento() + "|"
                + request.getIdSector() + "|" + ids + "|" + request.getMedioPago().toUpperCase());
    }

    private String huellaGeneral(CompraGeneralRequest request) {
        return hash("GEN|" + request.getIdCliente() + "|" + request.getIdEvento() + "|"
                + request.getIdSector() + "|" + request.getIdReserva() + "|"
                + request.getCantidad() + "|" + request.getMedioPago().toUpperCase());
    }

    private String claveNumerada(CompraRequest request) {
        List<Long> ids = new ArrayList<>(request.getIdsAsientos());
        Collections.sort(ids);
        String natural = "NUM|" + request.getIdSector() + "|" + ids;
        return claveCompra(request.getIdCliente(), request.getIdEvento(),
                request.getClaveIdempotencia(), natural);
    }

    private String claveGeneral(CompraGeneralRequest request) {
        return claveCompra(request.getIdCliente(), request.getIdEvento(),
                request.getClaveIdempotencia(), "GEN|" + request.getIdReserva());
    }

    private String claveCompra(String idCliente, String idEvento, String explicita, String natural) {
        if (explicita != null && !explicita.isBlank()) {
            String limpia = explicita.trim();
            if (limpia.length() < 8 || limpia.length() > 100 ||
                    !limpia.matches("[A-Za-z0-9._:-]+")) {
                throw new IllegalArgumentException("Clave de idempotencia no valida");
            }
            return hash("CLIENTE|" + idCliente + "|" + idEvento + "|" + limpia);
        }
        // Compatibilidad: sin clave explicita se deduce de la misma reserva
        return hash("AUTO|" + idCliente + "|" + idEvento + "|" + natural);
    }

    /**
     * Bloqueo transaccional de PostgreSQL compartido entre todas las instancias de Ticket.
     * Dos compras del mismo cliente/evento no superan el limite por carrera.
     */
    private void bloquearComprasClienteEvento(String idCliente, String idEvento) {
        entityManager.createNativeQuery(
                "SELECT 1 FROM pg_advisory_xact_lock(CAST(hashtext(:clave) AS BIGINT))")
                .setParameter("clave", "ticketya|" + idCliente + "|" + idEvento)
                .getSingleResult();
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular la clave de compra", e);
        }
    }

    private void validarDatosGeneral(CompraGeneralRequest request) {
        if (request == null || request.getIdCliente() == null ||
                request.getIdCliente().isBlank() || request.getIdEvento() == null ||
                request.getIdEvento().isBlank() || request.getIdSector() == null ||
                request.getIdReserva() == null || request.getCantidad() == null ||
                request.getCantidad() <= 0 || request.getCantidad() > maxEntradas ||
                request.getMedioPago() == null || request.getMedioPago().isBlank()) {
            throw new IllegalArgumentException("Datos de la compra general no validos");
        }
        String medio = request.getMedioPago().toUpperCase();
        if (!"TARJETA".equals(medio) && !"TRANSFERENCIA".equals(medio)
                && !"BILLETERA_VIRTUAL".equals(medio)) {
            throw new IllegalArgumentException("Medio de pago no valido");
        }
    }

    private void cancelarReservaGeneral(Long idReserva, Long idSector, String idCliente) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(
                    "http://localhost:8083/lugares/sectores/generales/reservas/" + idReserva)
                    .queryParam("idSector", idSector)
                    .queryParam("idCliente", idCliente).toUriString();
            restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (RuntimeException e) {
            System.err.println("No se pudo cancelar la reserva general " + idReserva
                    + ": " + e.getMessage());
        }
    }

    /**
     * Consultar el detalle de una compra ya registrada
     */
    @Transactional(readOnly = true)
    public CompraResponse obtenerCompra(Long idCompra) {
        CompraEntity compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        List<TicketEntity> tickets = ticketRepository.findByIdCompra(idCompra);
        return new CompraResponse(compra.getId(), compra.getEstado(),
                compra.getMedioPago(), compra.getTotal(), tickets);
    }

    /**
     * Verificar los datos recibidos
     * El usuario no puede enviar precio ni tickets creados
     */
    private void validarDatos(CompraRequest request) {
        if (request == null || request.getIdCliente() == null ||
                request.getIdCliente().isBlank() || request.getIdEvento() == null ||
                request.getIdEvento().isBlank() || request.getIdSector() == null ||
                request.getIdsAsientos() == null || request.getIdsAsientos().isEmpty() ||
                request.getMedioPago() == null || request.getMedioPago().isBlank()) {
            throw new IllegalArgumentException("Datos de la compra incompletos");
        }
        List<Long> ids = request.getIdsAsientos();
        if (maxEntradas < 1 || ids.size() > maxEntradas) {
            throw new IllegalArgumentException("Cantidad de entradas no valida");
        }
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.size() != ids.size() || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Asientos repetidos o invalidos");
        }
        String medio = request.getMedioPago().toUpperCase();
        if (!"TARJETA".equals(medio) && !"TRANSFERENCIA".equals(medio)
                && !"BILLETERA_VIRTUAL".equals(medio)) {
            throw new IllegalArgumentException("Medio de pago no valido");
        }
    }

    /**
     * Cancelar reservas luego de un pago rechazado
     * Se verifica el propietario en Lugar
     */
    private void cancelarReservas(List<Long> ids, String idCliente) {
        for (Long idAsiento : ids) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl(
                        "http://localhost:8083/lugares/reservas/" + idAsiento)
                        .queryParam("idCliente", idCliente).toUriString();
                restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            } catch (RuntimeException e) {
                // La limpieza de Lugar libera las reservas vencidas
                System.err.println("No se pudo cancelar la reserva " + idAsiento
                        + ": " + e.getMessage());
            }
        }
    }
}
