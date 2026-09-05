/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import com.example.dao.FacturaDAO;
import com.example.entity.Factura;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class MySQLFacturaDAO implements FacturaDAO {

    private final Connection conn;

    public MySQLFacturaDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }


    //idFactura sale del csv y asi mantener la relacion con Factura_Producto
    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS Factura ("+
                "idFactura INT PRIMARY KEY," +
                "idCliente INT," +
                "FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente)" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca una factura a partir de su id.
     * Recibe un id, hace un SELECT de Factura
     * Reemplaza ? por el id
     * Ejecuta la consulta, si existe una fila>
     * Crea una Factura
     * Carga idFactura e idCliente
     * Devuelve la factura encontrada
     * @param id id de la factura a buscar
     * @return factura encontrada, o null si no existe
     */
    @Override
    public Factura findById(int id) {
        String sql = "SELECT * FROM Factura WHERE idFactura = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

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

    /**
     * Busca todas las facturas
     * Hace un SELECT de Factura
     * Ejecuta la consulta
     * Recorre todas las filas
     * Crea una Factura por cada fila
     * Carga idFactura e idCliente
     * Agrega cada factura a la lista
     * Devuelve la lista de facturas
     * @return lista con todas las facturas
     */
    @Override
    public List<Factura> fintAll() {
        String sql = "SELECT * FROM Factura";
        List<Factura> facturas = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("idFactura"));
                factura.setIdCliente(rs.getInt("idCliente"));
                facturas.add(factura);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return facturas;
    }
}
