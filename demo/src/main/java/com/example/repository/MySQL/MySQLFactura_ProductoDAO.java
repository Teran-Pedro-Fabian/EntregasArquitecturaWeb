/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
                "FOREIGN KEY (idProducto) REFERENCES Producto(idProducto))";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Factura_Producto findById(int id) {
        String sql = "SELECT * FROM Factura_Producto WHERE idFactura = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
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
        return null;
    }

    @Override
    public List<Factura_Producto> fintAll() {
        String sql = "SELECT * FROM Factura_Producto";
        List<Factura_Producto> list = new java.util.ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Factura_Producto fp = new Factura_Producto();
                fp.setIdFactura(rs.getInt("idFactura"));
                fp.setIdProducto(rs.getInt("idProducto"));
                fp.setCantidad(rs.getInt("cantidad"));
                list.add(fp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void create(Factura_Producto c) {
        String sql = "INSERT INTO Factura_Producto (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getIdFactura());
            ps.setInt(2, c.getIdProducto());
            ps.setInt(3, c.getCantidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Factura_Producto c) {
        String sql = "UPDATE Factura_Producto SET cantidad = ? WHERE idFactura = ? AND idProducto = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getCantidad());
            ps.setInt(2, c.getIdFactura());
            ps.setInt(3, c.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Factura_Producto WHERE idFactura = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteByCliente(Long FPId) {
        String sql = "DELETE FROM Factura_Producto WHERE idFactura = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, FPId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Factura_Producto";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }







}