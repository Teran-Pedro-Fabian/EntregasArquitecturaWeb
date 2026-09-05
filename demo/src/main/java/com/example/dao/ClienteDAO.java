package com.example.dao;

import java.util.List;

import com.example.dto.ClienteConFacturacion;
import com.example.entity.Cliente;


public interface  ClienteDAO {

    Cliente findById(int id);
    List<ClienteConFacturacion> fintAllOrdenadoPorFacturacion();

    void create(Cliente c);
    void update(Cliente c);
    void delete(Long id);
    void deleteByCliente(Long clienteId);

    void deleteAll();
}
