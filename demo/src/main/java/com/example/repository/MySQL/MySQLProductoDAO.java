/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.repository.MySQL;

import com.example.dao.ProductoDAO;
import com.example.entity.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
            "idProducto INT PRIMARY KEY,"+
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

    /**
     * Buscar un producto desde su id
     * Recibe un id
     * SELECT de Producto
     *  ? por el id
     * Ejecutar la consulta si existe una fila
     * Crear un Producto
     * Cargar idProducto nombre y valor
     * Devolver producto encontrado
     * @param id id del producto a buscar
     * @return producto encontrado, o null si no existe
     */
    @Override
    public Producto findById(int id) {
        String sql = "SELECT * FROM Producto WHERE idProducto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("idProducto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setValor(rs.getDouble("valor"));
                return producto;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Devolver todos los productos de la base de datos
     * SELECT de Producto
     * Ejecutar consulta
     * Recorrer todas las filas
     * Crear un Producto por cada fila
     * Cargar idProducto nombre y valor
     * Agregar cada producto a lista
     * Devolver lista de productos
     * @return lista de productos
     */
    @Override
    public List<Producto> fintAll() {
        String sql = "SELECT * FROM Producto";

        List<Producto> productos = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("idProducto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setValor(rs.getDouble("valor"));

                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Crear un producto en la base de datos
     * Recibe un Producto
     * INSERT en Producto
     * ? por idProducto nombre y valor
     * Ejecutar la consulta
     * @param producto producto a guardar
     */
    @Override
    public void create(Producto producto) {
        String sql = "INSERT INTO Producto (idProducto, nombre, valor) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, producto.getIdProducto());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getValor());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualizar un producto en la base de datos
     * Recibe un Producto
     * Buscar el producto usando idProducto
     * Actualizar nombre y valor
     * Ejecutar la consulta
     * @param producto producto a actualizar
     */
    @Override
    public void update(Producto producto) {
        String sql = "UPDATE Producto SET nombre = ?, valor = ? WHERE idProducto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getValor());
            ps.setInt(3, producto.getIdProducto());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eliminar un producto de la base de datos
     * Recibe el id del producto
     * DELETE en Producto
     * ? por idProducto
     * Ejecutar la consulta
     * @param id id del producto a eliminar
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Producto WHERE idProducto = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eliminar un producto usando su id
     * Recibe productoId
     * Reutilizar el metodo delete()
     * @param productoId id del producto a eliminar
     */
    @Override
    public void deleteByCliente(Long productoId) {
        delete(productoId);
    }

    /**
     * Elimina todos los productos de la base de datos
     * DELETE de todo Producto
     * Ejecutar la consulta
     */
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Producto";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * punto 3 del enunciado a realizar
     */
    @Override
    public Producto findByProductoQueMasRecaudo() {
        return null;
    }


}
