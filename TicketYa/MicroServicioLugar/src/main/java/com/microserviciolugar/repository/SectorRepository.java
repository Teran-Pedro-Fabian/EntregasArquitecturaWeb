package com.microserviciolugar.repository;

import com.microserviciolugar.entity.SectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorRepository extends JpaRepository<SectorEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SectorEntity s WHERE s.id = :id")
    Optional<SectorEntity> findByIdParaActualizar(@Param("id") Long id);
    List<SectorEntity> findByIdEvento(String idEvento);

    // Precio mas bajo de los sectores de cada evento; una sola consulta para el catalogo.
    @Query("SELECT s.idEvento, MIN(s.precio) FROM SectorEntity s " +
           "WHERE s.idEvento IN :ids AND s.precio IS NOT NULL AND s.precio > 0 GROUP BY s.idEvento")
    List<Object[]> findPreciosMinimosPorEvento(@Param("ids") List<String> ids);
}
