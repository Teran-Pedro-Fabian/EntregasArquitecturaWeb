/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import com.example.dao.FacturaDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


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
}
