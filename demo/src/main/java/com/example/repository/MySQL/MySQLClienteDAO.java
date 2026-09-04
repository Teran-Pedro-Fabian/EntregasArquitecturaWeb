package com.example.repository.MySQL;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.example.dao.ClienteDAO;


public class MySQLClienteDAO implements ClienteDAO {

    private final Connection conn;

    public MySQLClienteDAO(Connection conn) {
        this.conn = conn;
        crearTablaSiNoExiste();
    }


        /* crea la tabla si no existe */
    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS cliente ("
                + "idCliente INT AUTO_INCREMENT PRIMARY KEY,"
                + "nombre VARCHAR(500) NOT NULL,"
                + "email VARCHAR(150) NOT NULL UNIQUE,"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
