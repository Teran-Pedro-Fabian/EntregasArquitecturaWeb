/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

    import com.example.dao.Factura_ProductoDAO;
import com.example.entity.Factura_Producto;

class MySQLFactura_ProductoDAO implements  Factura_ProductoDAO {

    private final Connection conn;

    public MySQLFactura_ProductoDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }


    private void crearTablaSiNoExiste() {
        String sql =" CREATE TABLE IF NOT EXISTS Factura_Producto ("+
                "idFactura INT,"+
                "idProducto INT,"+
                "cantidad INT,"+
                "PRIMARY KEY (idFactura, idProducto),"+
                "FOREIGN KEY (idFactura) REFERENCES Factura(idFactura),"+
                "FOREIGN KEY (idProducto) REFERENCES Producto(idProducto)";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Factura_Producto findById(int id) {
        String sql = "SELECT  * From Factura_Producto WHERE idFactura = " + id;
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Factura_Producto fp = new Factura_Producto();
                fp.setIdFactura(rs.getInt("idFactura"));
                fp.setIdProducto(rs.getInt("idProducto"));
                fp.setCantidad(rs.getInt("cantidad"));
                return fp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Factura_Producto> fintAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(Factura_Producto c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(Factura_Producto c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteByCliente(Long FPId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }







}