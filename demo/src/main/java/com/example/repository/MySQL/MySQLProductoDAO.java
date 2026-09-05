/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.example.dao.ProductoDAO;
import com.example.dto.ProductoMayorRecaudacion;
import com.example.entity.Producto;


public class MySQLProductoDAO implements ProductoDAO {

    private final Connection conn;

    public MySQLProductoDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }


    private void crearTablaSiNoExiste() {
        String sql = " CREATE TABLE IF NOT EXISTS Producto ("+
            "idProducto INT AUTO_INCREMENT PRIMARY KEY,"+
            "nombre VARCHAR(45),"+
            "valor FLOAT"+
        ")"
        ;

    try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(sql);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    }


    /* este metodo extrae todos los productos y calcula la recaudacion
    de cada uno de ellos, posteriormente los ordena por recaudacion y 
    selecciona el primero */
    @Override 
    public ProductoMayorRecaudacion getProductoConMasRecaudacion() {
        String sql = "SELECT p.nombre, SUM(fp.cantidad * p.valor) AS recaudacion " +
                "FROM Producto p " +
                "JOIN Factura_Producto fp ON p.idProducto = fp.idProducto " +
                "GROUP BY p.idProducto" +
                "ORDER BY recaudacion DESC " +
                "LIMIT 1";

        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                int recaudacion = rs.getInt("recaudacion");
                return new ProductoMayorRecaudacion(nombre, recaudacion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Producto findById(int id) {
        String sql = "SELECT * FROM Producto WHERE idProducto = " + id;
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombre(rs.getString("nombre"));
                p.setValor(rs.getFloat("valor"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<Producto> fintAll() {
        String sql = "SELECT * FROM Producto";
        List<Producto> list = new java.util.ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombre(rs.getString("nombre"));
                p.setValor(rs.getFloat("valor"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void create(Producto P) {
        String sql = "INSERT INTO Producto (nombre, valor) VALUES ('" +
                P.getNombre() + "', " +
                P.getValor() + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Producto P) {
        String sql = "UPDATE Producto SET nombre = '" + P.getNombre() + "', valor = " + P.getValor() +
                " WHERE idProducto = " + P.getIdProducto();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Producto WHERE idProducto = " + id;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteByCliente(Long ProductoId) {
        String sql = "DELETE FROM Producto WHERE idProducto = " + ProductoId;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
    }
}

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Producto";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
