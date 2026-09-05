package com.example.factory;

import java.sql.Connection;

import com.example.dao.ClienteDAO;
import com.example.dao.FacturaDAO;
import com.example.dao.Factura_ProductoDAO;
import com.example.dao.ProductoDAO;
import com.example.repository.MySQL.MySQLDAOFactory;





    public abstract class DAOFactory {


        /**
         * Notas
         * DAOFactory es la Abstract Factory.
         * getInstance(DBType) aplica Singleton sobre la fábrica elegida.
         * Los métodos createXxxDAO() son los Factory Method.
         */
        // --- Singleton de la fábrica seleccionada ---
        private static volatile DAOFactory instance;
        /**
         * Por qué usamos volatile??
         * volatile en Java garantiza visibilidad y orden de memoria entre hilos para esa variable.
         * Visibilidad: cuando un hilo escribe instance, otro hilo la lee actualizada (no una copia vieja en caché de CPU).
         * Orden: una escritura a una variable volatile ocurre antes que cualquier lectura posterior de esa misma variable en otro hilo.
         */



        /**
         * Obtiene la fábrica (Singleton) según el tipo de base de datos.
         * Usá este método desde el código cliente: DAOFactory f = DAOFactory.getInstance(DBType.MYSQL);
         */
        public static DAOFactory getInstance(DBType type) { // Realiza un DCL = Double-Checked Locking (bloqueo con doble verificación).
            if (instance == null) { // 1er chequeo
                synchronized (DAOFactory.class) { // Bloque sincronizado
                    if (instance == null) { // 2do chequeo
                                instance = new MySQLDAOFactory();
                        }
                    }
                }
            return instance;
        }
        /**
         * Resumen del patrón utilizado en el método getInstance
         * Este patrón se llama Double-Checked Locking (DCL), y la combinación volatile + synchronized asegura:
         * Inicialización perezosa (lazy initialization)
         * Seguridad en entornos multihilo (thread-safe)
         * Sin penalización de rendimiento innecesaria después de creada la instancia.
         */


        /**
         * Otra forma es no pasar el tipo en cada llamada, se puede setear una propiedad del sistema:
         * -Ddb.type=MYSQL  (o DERBY cuando se active)
         *
         * Lee una “system property” llamada db.type.
         * Si no existe, usa "MYSQL" como valor por defecto.
         * Se setea así:
         * En ejecución: System.setProperty("db.type", "MYSQL");
         * Al iniciar la JVM: java -Ddb.type=MYSQL ...
         * Normaliza a mayúsculas (toUpperCase()) para que valores como "mysql" funcionen.
         * Convierte el String al enum DBType con DBType.valueOf(...).
         * Si el string no coincide con un literal del enum, lanza IllegalArgumentException.
         * Delegación: llama al getInstance(type) (el que hace el Singleton real con DCL) y devuelve la fábrica concreta (MySQL, Derby, etc.).
         */
        public static DAOFactory getInstance() {
            String v = System.getProperty("db.type", "MYSQL");  // lee una “system property” llamada db.type. Si no existe, usa "MYSQL" como valor por defecto.
            DBType type = DBType.valueOf(v.toUpperCase());
            return getInstance(type);
        }



        // ------ Factory Methods (contratos por cada DAO de tu dominio) ------

        public abstract ClienteDAO createClienteDAO();
        public abstract ProductoDAO createProductoDAO();
        public abstract FacturaDAO createFacturaDAO();
        public abstract Factura_ProductoDAO createFactura_ProductoDAO();
        
        // public abstract OtraEntidadDAO createOtraEntidadDAO();


        // ------ Factory Method de la conexion ------

        /**
         * La conexion tambien es parte de la familia de productos: una fabrica MySQL
         * solo puede producir DAOs MySQL, y esos DAOs solo funcionan sobre una conexion MySQL.
         * Por eso el "como se obtiene la conexion" se delega a la subclase, igual que los DAO.
         *
         * Es protected: solo la propia fabrica concreta la usa para cablear sus DAOs.
         * El codigo cliente (Main, utils) nunca ve una Connection ni sabe que motor hay debajo.
         */
        protected abstract Connection getConnection();


        // ------ Cierre de la fabrica (Template Method) ------

        /**
         * Cierra la base sin que el cliente sepa que motor hay debajo:
         *
         *     DAOFactory f = DAOFactory.getInstance(DBType.DERBY);
         *     ...
         *     f.shutdown();     // Main no sabe si cerro MySQL o Derby
         *
         * Es final y delega el "como" en doShutdown(): eso es un Template Method.
         * El paso fijo es invalidar el Singleton; el paso variable lo define cada motor.
         *
         * Por que hay que limpiar 'instance': si no lo hicieramos, un getInstance()
         * posterior devolveria esta misma fabrica con la conexion ya cerrada.
         * Limpiandola, la proxima llamada reconstruye todo desde cero.
         */
        public final void shutdown() {
            doShutdown();
            synchronized (DAOFactory.class) {
                instance = null;
            }
        }

        /** Cada fabrica concreta cierra SU gestor de conexiones. */
        protected abstract void doShutdown();
    }
