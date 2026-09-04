/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

    import com.example.dao.Factura_ProductoDAO;

class MySQLFactura_ProductoDAO implements  Factura_ProductoDAO {

    private final Connection conn;

    public MySQLFactura_ProductoDAO(Connection connection) {
        this.conn = connection;
        crearTablaSiNoExiste();
    }


    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS factura_producto ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "factura_id INT NOT NULL,"
                + "producto_id INT NOT NULL,"
                + "cantidad INT NOT NULL,"
                + "FOREIGN KEY (factura_id) REFERENCES factura(id),"
                + "FOREIGN KEY (producto_id) REFERENCES producto(id)"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
