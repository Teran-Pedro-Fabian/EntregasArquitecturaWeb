package com.example.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.example.dao.ClienteDAO;
import com.example.dao.FacturaDAO;
import com.example.dao.Factura_ProductoDAO;
import com.example.dao.ProductoDAO;
import com.example.entity.Cliente;
import com.example.entity.Factura;
import com.example.entity.Factura_Producto;
import com.example.entity.Producto;
import com.example.factory.DAOFactory;
import com.example.factory.DBType;

public class CargarDatosIniciales {

	private static final String DATOS_CLASSPATH = "DB/Datos/";

	public void run() {
		DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
		cargarClientes(factory.createClienteDAO());
		cargarProductos(factory.createProductoDAO());
		cargarFacturas(factory.createFacturaDAO());
		cargarFacturaProductos(factory.createFactura_ProductoDAO());
	}

	private void cargarClientes(ClienteDAO dao) {
		cargar("clientes.csv", record -> dao.create(new Cliente(
				entero(record, "idCliente"), record.get("nombre"), record.get("email"))));
	}

	private void cargarProductos(ProductoDAO dao) {
		cargar("productos.csv", record -> dao.create(new Producto(
				entero(record, "idProducto"), record.get("nombre"), decimal(record, "valor"))));
	}

	private void cargarFacturas(FacturaDAO dao) {
		cargar("facturas.csv", record -> dao.create(new Factura(
				entero(record, "idFactura"), entero(record, "idCliente"))));
	}

	private void cargarFacturaProductos(Factura_ProductoDAO dao) {
		cargar("facturas-productos.csv", record -> dao.create(new Factura_Producto(
				entero(record, "idFactura"), entero(record, "idProducto"), entero(record, "cantidad"))));
	}

	private void cargar(String nombreArchivo, RecordConsumer consumer) {
		try (Reader reader = abrir(nombreArchivo); CSVParser parser = CSVFormat.DEFAULT.builder()
				.setHeader()
				.setSkipHeaderRecord(true)
				.setTrim(true)
				.build()
				.parse(reader)) {
			for (CSVRecord record : parser) {
				try {
					consumer.accept(record);
				} catch (RuntimeException e) {
					throw new IllegalStateException("Error en " + nombreArchivo
							+ ", registro " + record.getRecordNumber(), e);
				}
			}
		} catch (IOException | RuntimeException e) {
			if (e instanceof IllegalStateException && e.getMessage().startsWith("Error en ")) {
				throw (IllegalStateException) e;
			}
			throw new IllegalStateException("No se pudo cargar " + nombreArchivo + ": " + e.getMessage(), e);
		}
	}

	private Reader abrir(String nombreArchivo) throws IOException {
		InputStream recurso = CargarDatosIniciales.class.getClassLoader()
				.getResourceAsStream(DATOS_CLASSPATH + nombreArchivo);
		if (recurso != null) {
			return new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8));
		}

		List<Path> candidatos = Arrays.asList(
				Paths.get("DB", "Datos", nombreArchivo),
				Paths.get("demo", "DB", "Datos", nombreArchivo));
		for (Path candidato : candidatos) {
			if (Files.isRegularFile(candidato)) {
				return Files.newBufferedReader(candidato, StandardCharsets.UTF_8);
			}
		}
		throw new IOException("archivo no encontrado en classpath ni en " + candidatos);
	}

	private int entero(CSVRecord record, String columna) {
		return Integer.parseInt(record.get(columna));
	}

	private double decimal(CSVRecord record, String columna) {
		return Double.parseDouble(record.get(columna));
	}

	@FunctionalInterface
	private interface RecordConsumer {
		void accept(CSVRecord record);
	}
}
