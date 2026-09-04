/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import com.example.dao.ProductoDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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



}
