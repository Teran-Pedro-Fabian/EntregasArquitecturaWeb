package com.example.repository.MySQL;

import java.sql.Connection;

import com.example.dao.ClienteDAO;
import com.example.dao.FacturaDAO;
import com.example.dao.Factura_ProductoDAO;
import com.example.dao.ProductoDAO;
import com.example.factory.DAOFactory;

public class MySQLDAOFactory extends DAOFactory{

    
    /**
     * Implementacion MySQL del Factory Method de la conexion.
     * Toda la dependencia con MySQL (driver, URL, usuario, password) queda
     * encerrada en MySQLConnectionManager y solo esta clase lo conoce.
     */
    @Override
    protected Connection getConnection() {
        return MySQLConnectionManager.getInstance().getConnection();
    }

    /** Cierre especifico de MySQL: delega en su propio gestor de conexiones. */
    @Override
    protected void doShutdown() {
        MySQLConnectionManager.getInstance().shutdown();
    }

    /**
     * Implementacion MySQL de los Factory Methods de cada DAO.
     * Cada DAO concreto se construye con la conexion concreta que devuelve getConnection().
     */

    @Override
    public ClienteDAO createClienteDAO() {
        return new MySQLClienteDAO(getConnection());
    }

    @Override
    public ProductoDAO createProductoDAO() {
        return new MySQLProductoDAO(getConnection());
    }

    @Override
    public FacturaDAO createFacturaDAO() {
        return new MySQLFacturaDAO(getConnection());
    }

    @Override
    public Factura_ProductoDAO createFactura_ProductoDAO() {
        return new MySQLFactura_ProductoDAO(getConnection());
    }
}
