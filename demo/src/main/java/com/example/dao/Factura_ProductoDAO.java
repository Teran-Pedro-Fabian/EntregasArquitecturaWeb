package com.example.dao;

import java.util.List;

import com.example.entity.Factura_Producto;

public interface  Factura_ProductoDAO {
    Factura_Producto findById(int id);
    List<Factura_Producto> fintAll();

    void create(Factura_Producto c);
    void update(Factura_Producto c);
    void delete(Long id);
    void deleteByCliente(Long FPId);

    void deleteAll();
}
