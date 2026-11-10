package com.microserviciocliente.repository;

import com.microserviciocliente.entity.PreferenciasEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PreferenciasRepository extends MongoRepository<PreferenciasEntity,String> {

    List<PreferenciasEntity> findByidCliente(String idCliente);

}
