package com.microserviciolugar.service;

import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.entity.LugarEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.LugarRepository;
import com.microserviciolugar.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.Instant;
import com.microserviciolugar.entity.LiberacionAsientoEntity;
import com.microserviciolugar.repository.LiberacionAsientoRepository;

@Service
public class LugarService {

    @Autowired
    private AsientoRepository asientoRepository;
    @Autowired
    private LugarRepository lugarRepository;
    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private StringRedisTemplate redisTemplate; // Para manejar el tiempo de 10 min
    @Autowired
    private LiberacionAsientoRepository liberacionAsientoRepository;

    /**
     * Reservar un asiento por 10 minutos
     * Verificar si esta disponible o si vencio una reserva anterior
     *
     * @param idAsiento ID del asiento
     * @param idCliente ID del cliente
     * @return asiento reservado
     */
    @Transactional
    public AsientoEntity reservarAsiento(Long idAsiento, String idCliente) {

        // 1) Verificar el cliente
        if (idCliente == null || idCliente.isBlank()) {
            throw new RuntimeException("Cliente no valido");
        }

        // 2) Buscar el asiento y bloquearlo
        AsientoEntity asiento = asientoRepository.findByIdWithLugar(idAsiento).orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        Instant ahora = Instant.now();

        // 3) Verificar si el asiento ya fue vendido
        if ("VENDIDO".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento ya fue vendido");
        }

        // 4) Verificar si tiene una reserva activa
        if ("RESERVADO".equals(asiento.getEstado())) {
            // Evitar liberar reservas antiguas sin vencimiento
            if (asiento.getReservaHasta() == null) {
                throw new RuntimeException("Reserva sin vencimiento registrado");
            }
            // Verificar si la reserva sigue vigente
            if (asiento.getReservaHasta().isAfter(ahora)) {
                throw new RuntimeException("El asiento ya esta reservado");
            }
        } else if (!"DISPONIBLE".equals(asiento.getEstado())) {
            throw new RuntimeException("Estado del asiento no valido");
        }

        // 5) Reservar el asiento por 10 minutos
        asiento.setEstado("RESERVADO");
        asiento.setIdClienteReserva(idCliente);
        asiento.setReservaHasta(ahora.plusSeconds(600));

        // 6) Guardar los cambios en PostgreSQL
        asientoRepository.save(asiento);

        // 7) Guardar la reserva en Redis con TTL de 10 minutos
        String clave = "reserva:asiento:" + idAsiento;
        redisTemplate.opsForValue().set(clave, idCliente, 10, TimeUnit.MINUTES);
        return asiento;
    }


    /**
     * Validar que una reserva siga vigente
     * Comprobar cliente, sector y evento
     */
    @Transactional(readOnly = true)
    public void validarReserva(Long idAsiento, String idCliente, Long idSector, String idEvento) {
        // 1) Buscar el asiento
        AsientoEntity asiento = asientoRepository.findById(idAsiento).orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        // 2) Verificar el estado
        if (!"RESERVADO".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento no esta reservado");
        }
        // 3) Verificar el cliente
        if (!idCliente.equals(asiento.getIdClienteReserva())) {
            throw new RuntimeException("La reserva pertenece a otro cliente");
        }
        // 4) Verificar el vencimiento
        if (asiento.getReservaHasta() == null ||
                !asiento.getReservaHasta().isAfter(Instant.now())) {
            throw new RuntimeException("La reserva esta vencida");
        }
        // 5) Verificar sector y evento
        if (!idSector.equals(asiento.getSector().getId()) ||
                !idEvento.equals(asiento.getSector().getIdEvento())) {
            throw new RuntimeException("El asiento no pertenece al evento");
        }
        // 6) Verificar la reserva en Redis
        String clienteRedis = redisTemplate.opsForValue().get("reserva:asiento:" + idAsiento);
        if (!idCliente.equals(clienteRedis)) {
            throw new RuntimeException("La reserva no esta vigente en Redis");
        }
    }

    /**
     * Confirmar la venta de un asiento reservado
     * Guardar el ticket asociado a la venta
     *
     * @param idAsiento ID del asiento
     * @param idCliente ID del cliente
     * @param idTicket ID del ticket emitido
     */
    @Transactional
    public void confirmarVenta(Long idAsiento, String idCliente, Long idTicket) {
        // 1) Verificar los datos
        if (idCliente == null || idCliente.isBlank() || idTicket == null || idTicket <= 0) {
            throw new RuntimeException("Datos de la venta no validos");
        }
        // 2) Buscar el asiento y bloquearlo
        AsientoEntity asiento = asientoRepository.findByIdWithLugar(idAsiento).orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        // 3) Verificar que este reservado
        if (!"RESERVADO".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento no tiene una reserva activa");
        }
        // 4) Verificar el cliente
        if (!idCliente.equals(asiento.getIdClienteReserva())) {
            throw new RuntimeException("La reserva pertenece a otro cliente");
        }
        // 5) Verificar el vencimiento
        if (asiento.getReservaHasta() == null || !asiento.getReservaHasta().isAfter(Instant.now())) {
            throw new RuntimeException("La reserva ya vencio");
        }
        // 6) Confirmar la venta y asociar el ticket
        asiento.setEstado("VENDIDO");
        asiento.setIdTicketVendido(idTicket);
        asiento.setIdClienteReserva(null);
        asiento.setReservaHasta(null);
        // 7) Guardar los cambios
        asientoRepository.save(asiento);
        // 8) Eliminar la reserva de Redis
        redisTemplate.delete("reserva:asiento:" + idAsiento);
    }

    public LugarEntity guardarLugar(LugarEntity lugar) {
        return lugarRepository.save(lugar); // Necesitas crear LugarRepository
    }

    public List<LugarEntity> listarTodosLosLugares() {
        return lugarRepository.findAll();
    }

    public SectorEntity guardarSector(SectorEntity sector) {
        return sectorRepository.save(sector); // Necesitas crear SectorRepository
    }

    /**
     * Crear un asiento dentro de un sector
     * Verificar que el sector exista
     *
     * @param idSector ID del sector
     * @param fila fila del asiento
     * @param columna numero del asiento
     * @return asiento creado
     */
    @Transactional
    public AsientoEntity crearAsiento(Long idSector, String fila, Integer columna) {
        // 1) Verificar los datos
        if (idSector == null || fila == null || fila.isBlank()
                || columna == null || columna <= 0) {
            throw new RuntimeException("Datos del asiento no validos");
        }
        // 2) Buscar el sector
        SectorEntity sector = sectorRepository.findById(idSector).orElseThrow(() -> new RuntimeException("Sector no encontrado"));
        // 3) Crear el asiento
        AsientoEntity asiento = new AsientoEntity();
        asiento.setSector(sector);
        asiento.setFila(fila);
        asiento.setColumna(columna);
        asiento.setEstado("DISPONIBLE");
        // 4) Guardar el asiento en PostgreSQL
        return asientoRepository.save(asiento);
    }

    /**
     * Cancelar la reserva de un asiento
     * Verificar que pertenezca al cliente
     *
     * @param idAsiento ID del asiento
     * @param idCliente ID del cliente
     */
    @Transactional
    public void cancelarReserva(Long idAsiento, String idCliente) {
        // 1) Verificar el cliente
        if (idCliente == null || idCliente.isBlank()) {
            throw new RuntimeException("Cliente no valido");
        }
        // 2) Buscar el asiento y bloquearlo
        AsientoEntity asiento = asientoRepository.findByIdWithLugar(idAsiento).orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        // 3) Verificar que este reservado
        if (!"RESERVADO".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento no tiene una reserva activa");
        }
        // 4) Verificar que la reserva pertenezca al cliente
        if (!idCliente.equals(asiento.getIdClienteReserva())) {
            throw new RuntimeException("La reserva pertenece a otro cliente");
        }
        // 5) Liberar el asiento
        asiento.setEstado("DISPONIBLE");
        asiento.setIdClienteReserva(null);
        asiento.setReservaHasta(null);
        // 6) Guardar los cambios en PostgreSQL
        asientoRepository.save(asiento);
        // 7) Eliminar la reserva temporal de Redis
        redisTemplate.delete("reserva:asiento:" + idAsiento);
    }


    /**
     * Liberar un asiento vendido luego de una devolucion
     * Evitar procesar dos veces la misma liberacion
     *
     * @param idAsiento ID del asiento
     * @param idTicket ID del ticket devuelto
     */
    @Transactional
    public void liberarAsientoVendido(Long idAsiento, Long idTicket) {
        // 1) Verificar los datos recibidos
        if (idAsiento == null || idTicket == null || idTicket <= 0) {
            throw new RuntimeException("Datos de la devolucion no validos");
        }
        // 2) Verificar si el ticket ya fue devuelto
        LiberacionAsientoEntity liberacionAnterior = liberacionAsientoRepository.findById(idTicket).orElse(null);
        if (liberacionAnterior != null) {
            if (!idAsiento.equals(liberacionAnterior.getIdAsiento())) {
                throw new RuntimeException("El ticket fue liberado para otro asiento");
            }
            return;
        }
        // 3) Buscar el asiento y bloquearlo
        AsientoEntity asiento = asientoRepository.findByIdWithLugar(idAsiento).orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        // 4) Volver a verificar por si otra transaccion lo libero
        liberacionAnterior = liberacionAsientoRepository.findById(idTicket).orElse(null);
        if (liberacionAnterior != null) {
            if (!idAsiento.equals(liberacionAnterior.getIdAsiento())) {
                throw new RuntimeException("El ticket fue liberado para otro asiento");
            }
            return;
        }
        // 5) Verificar que el asiento este vendido
        if (!"VENDIDO".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento no esta vendido");
        }
        // 6) Verificar que el ticket corresponda al asiento
        if (!idTicket.equals(asiento.getIdTicketVendido())) {
            throw new RuntimeException("El ticket no corresponde a la venta");
        }
        // 7) Liberar el asiento
        asiento.setEstado("DISPONIBLE");
        asiento.setIdTicketVendido(null);
        asiento.setIdClienteReserva(null);
        asiento.setReservaHasta(null);
        asientoRepository.save(asiento);
        // 8) Registrar que el ticket ya fue liberado
        LiberacionAsientoEntity liberacion = new LiberacionAsientoEntity();
        liberacion.setIdTicket(idTicket);
        liberacion.setIdAsiento(idAsiento);
        liberacion.setFechaLiberacion(Instant.now());
        // 9) Guardar la liberacion
        liberacionAsientoRepository.save(liberacion);
    }

    public List<Long> buscarIdsPorUbicacionExacta(Double lat, Double lon) {
        List<LugarEntity> lugares = lugarRepository.findByLatitudAndLongitud(lat, lon);

        return lugares.stream()
                .map(LugarEntity::getId)
                .collect(Collectors.toList());
    }
}