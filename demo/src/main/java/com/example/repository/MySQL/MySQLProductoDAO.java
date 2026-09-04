/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import com.example.dao.ProductoDAO;

import java.sql.Connection;
import java.util.List;

import com.example.entity.Producto;


public class MySQLProductoDAO implements ProductoDAO {

    private final Connection conn;

    public MySQLProductoDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }



}
