package com.microserviciolugar.service;

import com.microserviciolugar.entity.ReservaGeneralEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.entity.VentaGeneralEntity;
import com.microserviciolugar.repository.ReservaGeneralRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.repository.VentaGeneralRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReservaGeneralService {

    @Autowired private SectorRepository sectorRepository;
    @Autowired private ReservaGeneralRepository reservaRepository;
    @Autowired private VentaGeneralRepository ventaRepository;
    @Autowired private EntityManager entityManager;

    /**
     * Reservar cupos de Campo General durante diez minutos
     * Bloquear la fila del sector para evitar sobreventa
     */
    @Transactional
    public ReservaGeneralEntity reservar(Long idSector, String idCliente, Integer cantidad) {
        if (idCliente == null || idCliente.isBlank() || cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Datos de la reserva no validos");
        }
        SectorEntity sector = bloquearSector(idSector);
        liberarVencidasDelSector(sector);
        int disponibles = cuposDisponibles(sector);
        if (cantidad > disponibles) {
            throw new IllegalStateException("No hay cupos suficientes. Disponibles: " + disponibles);
        }

        ReservaGeneralEntity reserva = new ReservaGeneralEntity();
        reserva.setSector(sector);
        reserva.setIdCliente(idCliente);
        reserva.setCantidad(cantidad);
        reserva.setEstado("RESERVADA");
        reserva.setReservaHasta(Instant.now().plusSeconds(600));
        reserva = reservaRepository.save(reserva);

        sector.setCuposReservados(reservados(sector) + cantidad);
        sectorRepository.save(sector);
        return reserva;
    }

    /** Validar antes de cobrar, sin modificar el stock. */
    @Transactional(readOnly = true)
    public void validar(Long idSector, Long idReserva, String idCliente,
                        String idEvento, Integer cantidad) {
        if (idSector == null || idReserva == null || cantidad == null || cantidad <= 0 ||
                idCliente == null || idCliente.isBlank() || idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Datos de la reserva no validos");
        }
        ReservaGeneralEntity reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        SectorEntity sector = reserva.getSector();
        if (!idSector.equals(sector.getId()) || !idEvento.equals(sector.getIdEvento()) ||
                !"NO_NUMERADO".equals(sector.getTipo()) ||
                !idCliente.equals(reserva.getIdCliente()) ||
                !cantidad.equals(reserva.getCantidad()) ||
                !"RESERVADA".equals(reserva.getEstado()) ||
                reserva.getReservaHasta() == null || !reserva.getReservaHasta().isAfter(Instant.now())) {
            throw new IllegalStateException("La reserva general no esta vigente o no corresponde al cliente");
        }
    }

    /**
     * Confirmar los tickets de la reserva
     * Actualizar contadores y registrar cada venta en la misma transaccion
     */
    @Transactional
    public void confirmar(Long idSector, Long idReserva, String idCliente,
                          Long idCompra, List<Long> idsTickets) {
        if (idReserva == null || idCompra == null || idCompra <= 0 ||
                idCliente == null || idCliente.isBlank() || idsTickets == null || idsTickets.isEmpty() ||
                idsTickets.stream().anyMatch(id -> id == null || id <= 0) ||
                new HashSet<>(idsTickets).size() != idsTickets.size()) {
            throw new IllegalArgumentException("Datos de la confirmacion no validos");
        }
        SectorEntity sector = bloquearSector(idSector);
        liberarVencidasDelSector(sector);
        ReservaGeneralEntity reserva = buscarReservaDelSector(idReserva, sector);

        // Reintento con los mismos tickets: devolver OK sin vender dos veces
        if ("VENDIDA".equals(reserva.getEstado())) {
            List<VentaGeneralEntity> ventas = ventaRepository.findByIdReservaGeneral(idReserva);
            Set<Long> anteriores = new HashSet<>();
            for (VentaGeneralEntity venta : ventas) anteriores.add(venta.getIdTicket());
            if (idCompra.equals(reserva.getIdCompra()) && anteriores.equals(new HashSet<>(idsTickets))) {
                return;
            }
            throw new IllegalStateException("La reserva ya fue utilizada en otra compra");
        }
        if (!"RESERVADA".equals(reserva.getEstado()) ||
                reserva.getReservaHasta() == null || !reserva.getReservaHasta().isAfter(Instant.now()) ||
                !idCliente.equals(reserva.getIdCliente()) ||
                reserva.getCantidad() != idsTickets.size()) {
            throw new IllegalStateException("La reserva general no esta vigente");
        }
        for (Long idTicket : idsTickets) {
            if (ventaRepository.existsById(idTicket)) {
                throw new IllegalStateException("El ticket ya esta asociado a una venta general");
            }
        }

        sector.setCuposReservados(reservados(sector) - reserva.getCantidad());
        sector.setCuposVendidos(vendidos(sector) + reserva.getCantidad());
        reserva.setEstado("VENDIDA");
        reserva.setIdCompra(idCompra);
        reservaRepository.save(reserva);
        sectorRepository.save(sector);
        for (Long idTicket : idsTickets) {
            VentaGeneralEntity venta = new VentaGeneralEntity();
            venta.setIdTicket(idTicket);
            venta.setIdSector(idSector);
            venta.setIdReservaGeneral(idReserva);
            ventaRepository.save(venta);
        }
    }

    /** Cancelar una reserva y devolver sus cupos. */
    @Transactional
    public void cancelar(Long idSector, Long idReserva, String idCliente) {
        if (idReserva == null || idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Datos de la cancelacion no validos");
        }
        SectorEntity sector = bloquearSector(idSector);
        ReservaGeneralEntity reserva = buscarReservaDelSector(idReserva, sector);
        if (!idCliente.equals(reserva.getIdCliente())) {
            throw new IllegalStateException("La reserva pertenece a otro cliente");
        }
        if ("CANCELADA".equals(reserva.getEstado()) || "VENCIDA".equals(reserva.getEstado())) return;
        if (!"RESERVADA".equals(reserva.getEstado())) {
            throw new IllegalStateException("La reserva ya fue vendida");
        }
        reserva.setEstado("CANCELADA");
        sector.setCuposReservados(reservados(sector) - reserva.getCantidad());
        reservaRepository.save(reserva);
        sectorRepository.save(sector);
    }

    /** Liberar un cupo vendido durante una devolucion de ticket. */
    @Transactional
    public void liberarVenta(Long idSector, Long idTicket) {
        if (idTicket == null || idTicket <= 0) {
            throw new IllegalArgumentException("Ticket no valido");
        }
        SectorEntity sector = bloquearSector(idSector);
        VentaGeneralEntity venta = ventaRepository.findById(idTicket)
                .orElseThrow(() -> new IllegalArgumentException("Venta general no encontrada"));
        if (!idSector.equals(venta.getIdSector())) {
            throw new IllegalArgumentException("El ticket pertenece a otro sector");
        }
        if ("DEVUELTA".equals(venta.getEstado())) return;
        if (!"VENDIDO".equals(venta.getEstado())) {
            throw new IllegalStateException("La venta general no tiene estado valido");
        }
        if (vendidos(sector) <= 0) {
            throw new IllegalStateException("No hay cupos vendidos para liberar");
        }
        venta.setEstado("DEVUELTA");
        sector.setCuposVendidos(vendidos(sector) - 1);
        ventaRepository.save(venta);
        sectorRepository.save(sector);
    }

    /** Mostrar la capacidad disponible de un sector general. */
    @Transactional
    public int consultarDisponibles(Long idSector) {
        SectorEntity sector = bloquearSector(idSector);
        liberarVencidasDelSector(sector);
        return cuposDisponibles(sector);
    }

    /** Liberar una reserva vencida con el mismo orden de bloqueos que las compras. */
    @Transactional
    public void expirarReserva(Long idReserva) {
        ReservaGeneralEntity previa = reservaRepository.findById(idReserva).orElse(null);
        if (previa == null) return;
        SectorEntity sector = bloquearSector(previa.getSector().getId());
        ReservaGeneralEntity reserva = buscarReservaDelSector(idReserva, sector);
        if ("RESERVADA".equals(reserva.getEstado()) && reserva.getReservaHasta() != null &&
                !reserva.getReservaHasta().isAfter(Instant.now())) {
            reserva.setEstado("VENCIDA");
            sector.setCuposReservados(reservados(sector) - reserva.getCantidad());
            reservaRepository.save(reserva);
            sectorRepository.save(sector);
        }
    }

    private SectorEntity bloquearSector(Long idSector) {
        if (idSector == null) throw new IllegalArgumentException("Sector no valido");
        SectorEntity sector = sectorRepository.findByIdParaActualizar(idSector)
                .orElseThrow(() -> new IllegalArgumentException("Sector no encontrado"));
        if (!"NO_NUMERADO".equals(sector.getTipo())) {
            throw new IllegalStateException("El sector es numerado");
        }
        if (sector.getCupos() == null || sector.getCupos() <= 0) {
            throw new IllegalStateException("El sector no tiene cupos validos");
        }
        return sector;
    }

    private ReservaGeneralEntity buscarReservaDelSector(Long idReserva, SectorEntity sector) {
        ReservaGeneralEntity reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        entityManager.refresh(reserva);
        if (!sector.getId().equals(reserva.getSector().getId())) {
            throw new IllegalArgumentException("La reserva pertenece a otro sector");
        }
        return reserva;
    }

    private void liberarVencidasDelSector(SectorEntity sector) {
        List<ReservaGeneralEntity> vencidas = reservaRepository
                .findBySectorIdAndEstadoAndReservaHastaLessThanEqual(
                        sector.getId(), "RESERVADA", Instant.now());
        for (ReservaGeneralEntity reserva : vencidas) {
            entityManager.refresh(reserva);
            if ("RESERVADA".equals(reserva.getEstado()) && reserva.getReservaHasta() != null &&
                    !reserva.getReservaHasta().isAfter(Instant.now())) {
                reserva.setEstado("VENCIDA");
                sector.setCuposReservados(reservados(sector) - reserva.getCantidad());
                reservaRepository.save(reserva);
            }
        }
        sectorRepository.save(sector);
    }

    private int reservados(SectorEntity sector) {
        return sector.getCuposReservados() == null ? 0 : sector.getCuposReservados();
    }

    private int vendidos(SectorEntity sector) {
        return sector.getCuposVendidos() == null ? 0 : sector.getCuposVendidos();
    }

    private int cuposDisponibles(SectorEntity sector) {
        return sector.getCupos() - reservados(sector) - vendidos(sector);
    }
}
