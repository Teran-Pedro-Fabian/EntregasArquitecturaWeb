package com.microserviciocliente.repository;

import com.microserviciocliente.entity.ClienteEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends MongoRepository<ClienteEntity, String> {


}
