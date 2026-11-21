package com.microserviciolugar.service;

import com.microserviciolugar.dto.ReporteSectorLugarDTO;
import com.microserviciolugar.dto.SectorPublicoDTO;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReporteSectorLugarService {

    private final SectorRepository sectorRepository;
    private final AsientoRepository asientoRepository;

    public ReporteSectorLugarService(SectorRepository sectorRepository,
                                    AsientoRepository asientoRepository) {
        this.sectorRepository = sectorRepository;
        this.asientoRepository = asientoRepository;
    }

    /**
     * Consultar los sectores de un evento sin modificar el stock.
     * En los numerados contamos los asientos creados.
     * En los generales usamos la capacidad del sector.
     */
    @Transactional(readOnly = true)
    public List<ReporteSectorLugarDTO> obtenerSectores(String idEvento) {
        validarEvento(idEvento);

        List<ReporteSectorLugarDTO> resultado = new ArrayList<>();
        List<SectorEntity> sectores = sectoresOrdenados(idEvento);

        for (SectorEntity sector : sectores) {
            String tipo = tipoSector(sector);
            resultado.add(new ReporteSectorLugarDTO(
                    sector.getId(),
                    sector.getNombre(),
                    tipo,
                    capacidadSector(sector, tipo)));
        }
        return resultado;
    }

    // sectores que puede ver el cliente antes de reservar
    // no mandamos reservas ni ventas internas
    @Transactional(readOnly = true)
    public List<SectorPublicoDTO> obtenerSectoresPublicos(String idEvento) {
        validarEvento(idEvento);

        List<SectorPublicoDTO> resultado = new ArrayList<>();
        List<SectorEntity> sectores = sectoresOrdenados(idEvento);

        for (SectorEntity sector : sectores) {
            String tipo = tipoSector(sector);
            Long idLugar = sector.getLugar() == null ? null : sector.getLugar().getId();

            resultado.add(new SectorPublicoDTO(
                    sector.getId(),
                    idLugar,
                    sector.getIdEvento(),
                    sector.getNombre(),
                    tipo,
                    sector.getPrecio(),
                    capacidadSector(sector, tipo)));
        }
        return resultado;
    }

    private void validarEvento(String idEvento) {
        if (idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Evento no valido");
        }
    }

    private List<SectorEntity> sectoresOrdenados(String idEvento) {
        List<SectorEntity> sectores = sectorRepository.findByIdEvento(idEvento);
        sectores.sort(Comparator.comparing(SectorEntity::getId));
        return sectores;
    }

    private String tipoSector(SectorEntity sector) {
        return sector.getTipo() == null || sector.getTipo().isBlank()
                ? "NUMERADO" : sector.getTipo();
    }

    private long capacidadSector(SectorEntity sector, String tipo) {
        if ("NO_NUMERADO".equals(tipo)) {
            return sector.getCupos() == null ? 0 : sector.getCupos();
        }
        if ("NUMERADO".equals(tipo)) {
            return asientoRepository.countBySector_Id(sector.getId());
        }
        throw new IllegalStateException("Tipo de sector no reconocido: " + tipo);
    }
}
