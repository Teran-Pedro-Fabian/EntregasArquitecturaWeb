package com.example;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.example.dao.ClienteDAO;
import com.example.dao.FacturaDAO;
import com.example.dao.Factura_ProductoDAO;
import com.example.dao.ProductoDAO;
import com.example.dto.ClienteConFacturacion;
import com.example.dto.ProductoMayorRecaudacion;
import com.example.entity.Cliente;
import com.example.entity.Factura;
import com.example.entity.Factura_Producto;
import com.example.entity.Producto;
import com.example.factory.DAOFactory;
import com.example.factory.DBType;

public class Main {

    private static final String CSV_PATH = "DB/Datos/";

    public static void main(String[] args) throws Exception {

        DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);

        // ENUNCIADO: Punto 1 - Creacion de tablas (ocurre al instanciar los DAOs)
        ClienteDAO clienteDAO = factory.createClienteDAO();
        ProductoDAO productoDAO = factory.createProductoDAO();
        FacturaDAO facturaDAO = factory.createFacturaDAO();
        Factura_ProductoDAO fpDAO = factory.createFactura_ProductoDAO();

        System.out.println("Tablas creadas correctamente.\n");

        // --- LIMPIEZA previa por si hay datos viejos (orden por FKs al reves) ---
        fpDAO.deleteAll();
        facturaDAO.deleteAll();
        productoDAO.deleteAll();
        clienteDAO.deleteAll();

        // ============================================================
        // ENUNCIADO: Carga masiva de datos desde archivos CSV
        // ORDEN IMPORTANTE: Cliente -> Producto -> Factura -> Factura_Producto
        // (respeta restricciones de Foreign Key)
        // ============================================================

        // --- CARGA 1: Clientes ---
        Reader inClientes = new FileReader(CSV_PATH + "clientes.csv");
        Iterable<CSVRecord> recordsClientes = CSVFormat.DEFAULT
                .builder()
                .setHeader("idCliente", "nombre", "email")
                .setSkipHeaderRecord(true)
                .build()
                .parse(inClientes);
        int countClientes = 0;
        for (CSVRecord r : recordsClientes) {
            Cliente c = new Cliente();
            c.setIdCliente(Integer.parseInt(r.get("idCliente")));
            c.setNombre(r.get("nombre"));
            c.setEmail(r.get("email"));
            clienteDAO.create(c);
            countClientes++;
        }
        inClientes.close();
        System.out.println("Cargados " + countClientes + " registros de Clientes");

        // --- CARGA 2: Productos ---
        Reader inProductos = new FileReader(CSV_PATH + "productos.csv");
        Iterable<CSVRecord> recordsProductos = CSVFormat.DEFAULT
                .builder()
                .setHeader("idProducto", "nombre", "valor")
                .setSkipHeaderRecord(true)
                .build()
                .parse(inProductos);
        int countProductos = 0;
        for (CSVRecord r : recordsProductos) {
            Producto p = new Producto();
            p.setIdProducto(Integer.parseInt(r.get("idProducto")));
            p.setNombre(r.get("nombre"));
            p.setValor(Double.parseDouble(r.get("valor")));
            productoDAO.create(p);
            countProductos++;
        }
        inProductos.close();
        System.out.println("Cargados " + countProductos + " registros de Productos");

        // --- CARGA 3: Facturas ---
        Reader inFacturas = new FileReader(CSV_PATH + "facturas.csv");
        Iterable<CSVRecord> recordsFacturas = CSVFormat.DEFAULT
                .builder()
                .setHeader("idFactura", "idCliente")
                .setSkipHeaderRecord(true)
                .build()
                .parse(inFacturas);
        int countFacturas = 0;
        for (CSVRecord r : recordsFacturas) {
            Factura f = new Factura();
            f.setIdFactura(Integer.parseInt(r.get("idFactura")));
            f.setIdCliente(Integer.parseInt(r.get("idCliente")));
            facturaDAO.create(f);
            countFacturas++;
        }
        inFacturas.close();
        System.out.println("Cargados " + countFacturas + " registros de Facturas");

        // --- CARGA 4: Factura_Producto ---
        Reader inFP = new FileReader(CSV_PATH + "facturas-productos.csv");
        Iterable<CSVRecord> recordsFP = CSVFormat.DEFAULT
                .builder()
                .setHeader("idFactura", "idProducto", "cantidad")
                .setSkipHeaderRecord(true)
                .build()
                .parse(inFP);
        int countFP = 0;
        for (CSVRecord r : recordsFP) {
            Factura_Producto fp = new Factura_Producto();
            fp.setIdFactura(Integer.parseInt(r.get("idFactura")));
            fp.setIdProducto(Integer.parseInt(r.get("idProducto")));
            fp.setCantidad(Integer.parseInt(r.get("cantidad")));
            fpDAO.create(fp);
            countFP++;
        }
        inFP.close();
        System.out.println("Cargados " + countFP + " registros de Factura_Producto\n");

        // ============================================================
        // ENUNCIADO: Reporte 1 - Clientes ordenados por facturacion total DESC
        // Resultado: Listado posicion/nombre/email/facturacion ordenado de mayor a menor
        // ============================================================
        System.out.println("============================================================");
        System.out.println("REPORTE 1: Clientes ordenados por facturacion total (DESC)");
        System.out.println("============================================================");
        List<ClienteConFacturacion> reporte1 = clienteDAO.fintAllOrdenadoPorFacturacion();
        System.out.printf("%-4s | %-35s | %-45s | %s%n", "POS", "NOMBRE", "EMAIL", "FACTURACION $");
        System.out.println("----------------------------------------------------------------------------------------------");
        int pos = 1;
        for (ClienteConFacturacion c : reporte1) {
            System.out.printf("%-4d | %-35s | %-45s | $%d%n",
                    pos++,
                    c.nombre(),
                    c.email(),
                    c.facturacion());
        }
        System.out.println();

        // ============================================================
        // ENUNCIADO: Reporte 2 - Producto con mayor recaudacion
        // Resultado: Nombre del producto y monto total recaudado
        // ============================================================
        System.out.println("============================================================");
        System.out.println("REPORTE 2: Producto con mayor recaudacion");
        System.out.println("============================================================");
        ProductoMayorRecaudacion reporte2 = productoDAO.getProductoConMasRecaudacion();
        if (reporte2 != null) {
            System.out.printf("Producto    : %s%n", reporte2.nombre());
            System.out.printf("Recaudacion : $%d%n", reporte2.recaudacion());
        } else {
            System.out.println("No hay datos suficientes para calcular el reporte.");
        }
        System.out.println();

        // ============================================================
        // ENUNCIADO: Reporte 3 - Facturas que contienen un producto determinado
        // Producto elegido: idProducto = 1 (existe en el CSV)
        // Resultado: Listado de nro de factura y cliente asociado
        // ============================================================
        final int ID_PRODUCTO_BUSCADO = 1;
        System.out.println("============================================================");
        System.out.println("REPORTE 3: Facturas que contienen el producto id=" + ID_PRODUCTO_BUSCADO);
        Producto prodBuscado = productoDAO.findById(ID_PRODUCTO_BUSCADO);
        if (prodBuscado != null) {
            System.out.println("(Nombre del producto: " + prodBuscado.getNombre() + ")");
        }
        System.out.println("============================================================");
        List<Factura> reporte3 = facturaDAO.fintAllFacturasDeProducto(ID_PRODUCTO_BUSCADO);
        if (reporte3 == null || reporte3.isEmpty()) {
            System.out.println("No se encontraron facturas con ese producto.");
        } else {
            System.out.printf("%-15s | %s%n", "NRO FACTURA", "ID CLIENTE");
            System.out.println("---------------------------------");
            for (Factura f : reporte3) {
                System.out.printf("%-15d | %d%n", f.getIdFactura(), f.getIdCliente());
            }
        }
        System.out.println();

        factory.shutdown();

        System.out.println("Proceso completo - todas las consignas ejecutadas correctamente.");
    }
}
