package com.microserviciolugar.config;

import com.microserviciolugar.entity.AsientoEntity;
import com.microserviciolugar.entity.LugarEntity;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.AsientoRepository;
import com.microserviciolugar.repository.LugarRepository;
import com.microserviciolugar.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LugarDataInitializer implements CommandLineRunner {
    @Autowired
    private LugarRepository lRepo;
    @Autowired
    private SectorRepository sRepo;
    @Autowired
    private AsientoRepository aRepo;

    @Override
    public void run(String... args) {
        if (lRepo.count() == 0) {
            LugarEntity l = lRepo.save(new LugarEntity("Estadio Arena", "Av. 123", -34.6, -58.3));

            SectorEntity s = new SectorEntity();
            s.setNombre("VIP");
            s.setLugar(l);
            sRepo.save(s);

            AsientoEntity as = new AsientoEntity();
            as.setSector(s);
            as.setEstado("DISPONIBLE");
            aRepo.save(as);

            System.out.println("✅ Lugar, Sector y Asiento creados");
        }
    }
}
