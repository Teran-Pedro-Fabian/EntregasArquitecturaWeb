package com.example.repository.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.example.factory.ConnectionManager;

/**
 * Gestor de conexiones de MySQL. Antes era ejemplo.factory.ConnectionManager;
 * se movió acá para quedar junto a la familia de DAOs que la usa,
 * en simetría con DerbyConnectionManager.
 */
public final class MySQLConnectionManager implements ConnectionManager {

    /**
     * volatile garantiza que todos los hilos vean el mismo valor de instance en memoria,
     * incluso si un hilo la inicializa mientras otro la está leyendo.
     * Sin volatile, un hilo podría recibir una referencia a un objeto parcialmente construido por otro hilo.
     */
    private static volatile MySQLConnectionManager instance;
    private Connection connection; // java.sql.Connection (para SQL)

    // --- Configuración de conexión ---
    private static final String URL = "jdbc:mysql://localhost:3306/facturacion";
    private static final String USER = "usuario";
    private static final String PASSWORD = "usuario123"; // contraseña vacía

    // --- Constructor privado ---
    private MySQLConnectionManager() {
        try {
            // Registrar el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establecer la conexión
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión establecida correctamente con MySQL.");

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    // --- Singleton Thread-Safe ---
    public static MySQLConnectionManager getInstance() {
        if (instance == null) { // 1er chequeo: Evita bloquear si ya existe la instancia.
            synchronized (MySQLConnectionManager.class) { // Bloque sincronizado: Asegura que solo un hilo cree la instancia en caso de concurrencia.
                if (instance == null) { // 2do chequeo Confirma que instance sigue siendo null antes de crearla.
                    instance = new MySQLConnectionManager();
                }
            }
        }
        return instance;
    }

    // --- Retornar la conexión ---
    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * En MySQL alcanza con cerrar la conexión: no hay que apagar ningún motor,
     * porque el servidor corre en un proceso aparte.
     */
    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión con MySQL cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión con MySQL: " + e.getMessage());
        } finally {
            connection = null;
            synchronized (MySQLConnectionManager.class) {
                instance = null;
            }
        }
    }
}
