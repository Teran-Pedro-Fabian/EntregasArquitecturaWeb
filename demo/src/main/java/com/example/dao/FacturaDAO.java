package com.example.dao;

import java.util.List;

import com.example.entity.Factura;

public interface FacturaDAO {

    Factura findById(int id);
    List<Factura> fintAll();
    List<Factura> fintAllFacturasDeProducto(int productoId);

    void create(Factura c);
    void update(Factura c);
    void delete(Long id);
    void deleteByFactura(Long FacturaId);

    void deleteAll();
}
