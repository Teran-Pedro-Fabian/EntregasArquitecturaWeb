package com.example.repository.MySQL;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.dao.ClienteDAO;
import com.example.dto.ClienteConFacturacion;
import com.example.entity.Cliente;


public class MySQLClienteDAO implements ClienteDAO {

    private final Connection conn;

    public MySQLClienteDAO(Connection conn) {
        this.conn = conn;
        crearTablaSiNoExiste();
    }


        /* crea la tabla si no existe */
    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS Cliente ("
                + "idCliente INT PRIMARY KEY,"
                + "nombre VARCHAR(500),"
                + "email VARCHAR(150)"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Cliente findById(int id) {
        String sql = "SELECT  * From cliente WHERE idCliente = " + id;
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setNombre(rs.getString("nombre"));
                c.setEmail(rs.getString("email"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /* este metodo captura de la base de datos a los clientes y los ordena por
    que pago mas dinero por sus facturas*/
    @Override
    public List<ClienteConFacturacion> fintAllOrdenadoPorFacturacion() {
        String sql = "SELECT c.idCliente, c.nombre, c.email, SUM(p.valor * fp.cantidad) AS facturacion "
                + "FROM cliente c "
                + "LEFT JOIN factura f ON c.idCliente = f.idCliente "
                + "LEFT JOIN factura_producto fp ON f.idFactura = fp.idFactura "
                + "LEFT JOIN producto p ON fp.idProducto = p.idProducto "
                + "GROUP BY c.idCliente, c.nombre, c.email "
                + "ORDER BY facturacion DESC";
        
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(sql);
            List<ClienteConFacturacion> clientes = new ArrayList<>();
            while (rs.next()) {
                ClienteConFacturacion c = new ClienteConFacturacion(rs.getString("nombre"), rs.getString("email"),rs.getInt("facturacion"));
                clientes.add(c);
            }
            return clientes;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void create(Cliente c) {
        String sql = "INSERT INTO cliente (nombre, email) VALUES ('" + c.getNombre() + "', '" + c.getEmail() + "')";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Cliente c) {
        String sql = "UPDATE cliente SET nombre = '" + c.getNombre() + "', email = '" + c.getEmail() + "' WHERE idCliente = " + c.getIdCliente();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM cliente WHERE idCliente = " + id;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteByCliente(Long clienteId) {
        String sql = "DELETE FROM cliente WHERE idCliente = " + clienteId;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM cliente";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
