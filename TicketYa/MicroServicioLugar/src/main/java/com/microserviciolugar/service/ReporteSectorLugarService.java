package com.microserviciolugar.service;

import com.microserviciolugar.dto.ReporteSectorLugarDTO;
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
        if (idEvento == null || idEvento.isBlank()) {
            throw new IllegalArgumentException("Evento no valido");
        }

        List<ReporteSectorLugarDTO> resultado = new ArrayList<>();
        List<SectorEntity> sectores = sectorRepository.findByIdEvento(idEvento);
        sectores.sort(Comparator.comparing(SectorEntity::getId));

        for (SectorEntity sector : sectores) {
            String tipo = sector.getTipo() == null || sector.getTipo().isBlank()
                    ? "NUMERADO" : sector.getTipo();
            long capacidad;
            if ("NO_NUMERADO".equals(tipo)) {
                capacidad = sector.getCupos() == null ? 0 : sector.getCupos();
            } else if ("NUMERADO".equals(tipo)) {
                capacidad = asientoRepository.countBySector_Id(sector.getId());
            } else {
                throw new IllegalStateException("Tipo de sector no reconocido: " + tipo);
            }
            resultado.add(new ReporteSectorLugarDTO(sector.getId(), sector.getNombre(),
                    tipo, capacidad));
        }
        return resultado;
    }
}
