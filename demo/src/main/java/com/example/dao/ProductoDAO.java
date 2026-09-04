package com.example.dao;

import java.util.List;

import com.example.entity.Producto;

public interface  ProductoDAO {

    Producto findById(int id);
    Producto findByProductoQueMasRecaudo();
    List<Producto> fintAll();

    void create(Producto P);
    void update(Producto P);
    void delete(Long id);
    void deleteByCliente(Long ProductoId);

    void deleteAll();
}
