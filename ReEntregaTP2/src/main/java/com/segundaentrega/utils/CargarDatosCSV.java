package com.segundaentrega.utils;

import com.segundaentrega.Entitys.CarreraEntity;
import com.segundaentrega.Entitys.EstudianteCarrera;
import com.segundaentrega.Entitys.EstudianteCarreraId;
import com.segundaentrega.Entitys.EstudianteEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class CargarDatosCSV {

    private static final String DATOS_CLASSPATH = "DB/";
    private final EntityManager em;

    public CargarDatosCSV(EntityManager em) {
        this.em = em;
    }

    // Cargar los tres archivos CSV
    public void cargarTodo() {
        System.out.println("[CargarDatosCSV] Iniciando carga de CSV...");
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            cargarCarreras();
            cargarEstudiantes();
            cargarEstudianteCarrera();
            tx.commit();
            System.out.println("[CargarDatosCSV] Carga finalizada correctamente.");
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // Vaciar las tablas respetando las relaciones
    public void vaciarDB() {
        System.out.println("[CargarDatosCSV] Iniciando vaciado de tablas...");
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM EstudianteCarrera").executeUpdate();
            em.createQuery("DELETE FROM EstudianteEntity").executeUpdate();
            em.createQuery("DELETE FROM CarreraEntity").executeUpdate();
            tx.commit();
            em.clear();
            System.out.println("[CargarDatosCSV] Vaciado finalizado.");

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // Cargar carreras
    private void cargarCarreras() {
        cargar("carreras.csv", record -> {
            CarreraEntity c = new CarreraEntity(
                    entero(record, "id_carrera"),
                    record.get("carrera"),
                    entero(record, "duracion"),
                    null
            );
            em.persist(c);
            return true;
        });
    }

    // Cargar estudiantes
    private void cargarEstudiantes() {
        cargar("estudiantes.csv", record -> {
            EstudianteEntity e = new EstudianteEntity(
                    entero(record, "DNI"),
                    record.get("nombre"),
                    record.get("apellido"),
                    entero(record, "edad"),
                    record.get("genero"),
                    record.get("ciudad"),
                    entero(record, "LU"),
                    null
            );
            em.persist(e);
            return true;
        });
    }

    // Cargar inscripciones con clave primaria compuesta
    private void cargarEstudianteCarrera() {
        cargar("estudianteCarrera.csv", record -> {
            int idEstudiante = entero(record, "id_estudiante");
            int idCarrera = entero(record, "id_carrera");
            EstudianteEntity estudiante = em.find(EstudianteEntity.class, idEstudiante);
            CarreraEntity carrera = em.find(CarreraEntity.class, idCarrera);

            if (estudiante == null) {
                System.out.println("[CargarDatosCSV] Estudiante no encontrado: "
                        + idEstudiante);
                return false;
            }
            if (carrera == null) {
                System.out.println("[CargarDatosCSV] Carrera no encontrada: " + idCarrera);
                return false;
            }
            EstudianteCarreraId id = new EstudianteCarreraId(idEstudiante, idCarrera);
            EstudianteCarrera ec = new EstudianteCarrera(
                    id,
                    estudiante,
                    carrera,
                    entero(record, "inscripcion"),
                    entero(record, "graduacion"),
                    entero(record, "antiguedad")
            );
            em.persist(ec);
            return true;
        });
    }

    // Leer registros de un archivo CSV
    private void cargar(String nombreArchivo, RecordConsumer consumer) {
        final byte[] bytes;
        try {
            bytes = leerBytes(nombreArchivo);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar " + nombreArchivo, ex);
        }
        List<Charset> charsets = Arrays.asList(
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1
        );
        IOException ultimo = null;

        for (Charset cs : charsets) {
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(bytes), cs));
                 CSVParser parser = CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setTrim(true)
                         .build()
                         .parse(reader)) {

                int cargados = 0;
                for (CSVRecord record : parser) {
                    try {
                        if (consumer.accept(record)) {
                            cargados++;
                        }
                    } catch (RuntimeException ex) {
                        throw new IllegalStateException(
                                "Error en " + nombreArchivo +
                                        ", registro " + record.getRecordNumber(), ex);
                    }
                }
                System.out.println("[CargarDatosCSV] " + nombreArchivo + " -> " + cargados + " registros cargados.");
                return;
            } catch (IOException ex) {
                ultimo = ex;
            }
        }
        throw new IllegalStateException("No se pudo cargar " + nombreArchivo, ultimo);
    }

    // Buscar archivos CSV
    private byte[] leerBytes(String nombreArchivo) throws IOException {
        InputStream recurso = CargarDatosCSV.class.getClassLoader().getResourceAsStream(DATOS_CLASSPATH + nombreArchivo);

        if (recurso != null) {
            try (recurso) {
                return recurso.readAllBytes();
            }
        }

        List<Path> candidatos = Arrays.asList(
                Paths.get("DB", nombreArchivo),
                Paths.get("ReEntregaTP2", "DB", nombreArchivo));
        for (Path candidato : candidatos) {
            if (Files.isRegularFile(candidato)) {
                return Files.readAllBytes(candidato);
            }
        }
        throw new IOException("Archivo no encontrado: " + nombreArchivo);
    }

    // Convertir un campo a entero
    private int entero(CSVRecord record, String columna) {
        return Integer.parseInt(record.get(columna));
    }

    @FunctionalInterface
    private interface RecordConsumer {
        boolean accept(CSVRecord record);
    }
}
