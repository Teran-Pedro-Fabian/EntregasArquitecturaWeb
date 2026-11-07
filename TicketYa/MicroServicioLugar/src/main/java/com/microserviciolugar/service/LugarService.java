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

    @Transactional
    public AsientoEntity reservarAsiento(Long idAsiento, String idCliente) {
        AsientoEntity asiento = asientoRepository.findByIdWithLugar(idAsiento)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        //Verificamos si está disponible
        if (!"DISPONIBLE".equals(asiento.getEstado())) {
            throw new RuntimeException("El asiento ya está ocupado o reservado");
        }

        // Lo marco como RESERVADO en la DB
        asiento.setEstado("RESERVADO");
        asientoRepository.save(asiento);

        // Cre0 la reserva en Redis con un TTL de 10 minutos
        // Clave: "reserva:asiento:ID" -> Valor: "ID_CLIENTE"
        redisTemplate.opsForValue().set("reserva:asiento:" + idAsiento, idCliente, 10, TimeUnit.MINUTES);

        return asiento;
    }

    @Transactional
    public void confirmarVenta(Long idAsiento) {
        AsientoEntity asiento = asientoRepository.findById(idAsiento).orElseThrow();
        asiento.setEstado("VENDIDO");
        asientoRepository.save(asiento);

        // Borramos la reserva de Redis porque ya se vendió
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

    public void cancelarReserva(Long idAsiento) {
        AsientoEntity asiento = asientoRepository.findById(idAsiento)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        // Solo cancelamos si estaba RESERVADO
        if ("RESERVADO".equals(asiento.getEstado())) {
            asiento.setEstado("DISPONIBLE");
            asientoRepository.save(asiento);
            redisTemplate.delete("reserva:asiento:" + idAsiento);
        }
    }

    public List<Long> buscarIdsPorUbicacionExacta(Double lat, Double lon) {
        List<LugarEntity> lugares = lugarRepository.findByLatitudAndLongitud(lat, lon);

        return lugares.stream()
                .map(LugarEntity::getId)
                .collect(Collectors.toList());
    }
}