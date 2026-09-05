/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.example.dao.FacturaDAO;
import com.example.entity.Factura;


public class MySQLFacturaDAO implements FacturaDAO {

    private final Connection conn;

    public MySQLFacturaDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }



    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS Factura ("+
            "idFactura INT AUTO_INCREMENT PRIMARY KEY,"+
            "idCliente INT,"+
            "FOREIGN KEY (idCliente) REFERENCES Cliente(idcliente)"+
        ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Factura findById(int id) {
        String sql = "SELECT * FROM Factura WHERE idFactura = " + id;
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("idFactura"));
                factura.setIdCliente(rs.getInt("idCliente"));
                return factura;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Factura> fintAll() {
        String sql = "SELECT * FROM Factura";
        List<Factura> list = new java.util.ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("idFactura"));
                factura.setIdCliente(rs.getInt("idCliente"));
                list.add(factura);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Factura> fintAllFacturasDeProducto(int productoId) {
        String sql = "SELECT f.* FROM Factura f " +
                "JOIN Factura_Producto fp ON f.idFactura = fp.idFactura " +
                "WHERE fp.idProducto = ?";
        List<Factura> list = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("idFactura"));
                factura.setIdCliente(rs.getInt("idCliente"));
                list.add(factura);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void create(Factura c) {
        String sql = "INSERT INTO Factura (idCliente) VALUES (" + c.getIdCliente() + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Factura c) {
        String sql = "UPDATE Factura SET idCliente = " + c.getIdCliente() +
                " WHERE idFactura = " + c.getIdFactura();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Factura WHERE idFactura = " + id;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteByFactura(Long FacturaId) {
        String sql = "DELETE FROM Factura WHERE idFactura = " + FacturaId;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Factura";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
