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
import java.util.ArrayList;
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
        return null;
    }

    /**
     * Devuelve todo entre facturas y productos
     * SELECT de Factura_Producto
     * Ejecutar la consulta
     * Recorrer todas las filas
     * Crear un Factura de Producto por cada fila
     * Cargar idFactura idProducto y cantidad
     * Agregar cada relacion a la lista
     * @return lista de relaciones Factura_Producto
     */
    @Override
    public List<Factura_Producto> fintAll() {
        String sql = "SELECT * FROM Factura_Producto";

        List<Factura_Producto> facturaProductos = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Factura_Producto fp = new Factura_Producto();
                fp.setIdFactura(rs.getInt("idFactura"));
                fp.setIdProducto(rs.getInt("idProducto"));
                fp.setCantidad(rs.getInt("cantidad"));

                facturaProductos.add(fp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return facturaProductos;
    }

    /**
     * Crear una relacion entre una factura y un producto
     * Recibe un Factura_Producto
     * INSERT en Factura_Producto
     * ? por idFactura, idProducto y cantidad
     * Ejecutar la consulta
     * @param fp relacion Factura_Producto a guardar
     */
    @Override
    public void create(Factura_Producto fp) {
        String sql = "INSERT INTO Factura_Producto " +
                "(idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fp.getIdFactura());
            ps.setInt(2, fp.getIdProducto());
            ps.setInt(3, fp.getCantidad());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualizar la cantidad de un producto dentro de una factura
     * Recibe un Factura_Producto
     * Buscar la relacion usando idFactura e idProducto
     * Actualizar cantidad
     * Ejecutar la consulta
     * @param fp relacion Factura_Producto a actualizar
     */
    @Override
    public void update(Factura_Producto fp) {
        String sql = "UPDATE Factura_Producto SET cantidad = ? " +
                "WHERE idFactura = ? AND idProducto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fp.getCantidad());
            ps.setInt(2, fp.getIdFactura());
            ps.setInt(3, fp.getIdProducto());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eliminar las relaciones de una factura
     * Recibe idFactura
     * DELETE en Factura_Producto
     * ? por idFactura
     * Ejecutar la consulta
     * @param id id de la factura
     */
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

    /**
     * Eliminar las relaciones usando idFactura
     * Recibe FPId
     * Reutilizar el metodo delete()
     * @param FPId id de la factura
     */
    @Override
    public void deleteByCliente(Long FPId) {
        delete(FPId);
    }

    /**
     * Eliminar todas las relaciones entre facturas y productos
     * DELETE de todos los registros de Factura_Producto
     * Ejecutar la consulta
     */
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Factura_Producto";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}