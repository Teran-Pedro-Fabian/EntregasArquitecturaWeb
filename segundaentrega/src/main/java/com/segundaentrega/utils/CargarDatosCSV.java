package com.segundaentrega.utils;

import com.segundaentrega.Entitys.CarreraEntity;
import com.segundaentrega.Entitys.EstudianteCarrera;
import com.segundaentrega.Entitys.EstudianteEntity;
import com.segundaentrega.Repository.CarreraRepository;
import com.segundaentrega.Repository.EstudianteCarreraRepository;
import com.segundaentrega.Repository.EstudianteRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
public class CargarDatosCSV implements CommandLineRunner {

    private static final String DATOS_CLASSPATH = "DB/";

    @Autowired
    private CarreraRepository carreraRepo;

    @Autowired
    private EstudianteRepository estudianteRepo;

    @Autowired
    private EstudianteCarreraRepository estudianteCarreraRepo;

    @Override
    public void run(String... args) {
        if (carreraRepo.count() > 0 && estudianteRepo.count() > 0 && estudianteCarreraRepo.count() > 0) {
            System.out.println("[CargarDatosCSV] Tablas con datos detectados. Se omite carga de CSV (idempotente).");
            return;
        }

        System.out.println("[CargarDatosCSV] Tablas vacias detectadas. Iniciando carga de CSV...");
        cargarCarreras();
        cargarEstudiantes();
        cargarEstudianteCarrera();
        System.out.println("[CargarDatosCSV] Carga finalizada correctamente.");
    }

    private void cargarCarreras() {
        cargar("carreras.csv", record -> {
            CarreraEntity c = new CarreraEntity(
                    entero(record, "id_carrera"),
                    record.get("carrera"),
                    entero(record, "duracion"),
                    null
            );
            carreraRepo.save(c);
        });
    }

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
            estudianteRepo.save(e);
        });
    }

    private void cargarEstudianteCarrera() {
        cargar("estudianteCarrera.csv", record -> {
            int idEstudiante = entero(record, "id_estudiante");
            int idCarrera = entero(record, "id_carrera");

            java.util.Optional<EstudianteEntity> optE = estudianteRepo.findById(idEstudiante);
            java.util.Optional<CarreraEntity> optC = carreraRepo.findById(idCarrera);

            if (optE.isEmpty()) {
                System.out.println("[CargarDatosCSV]   ADVERTENCIA: id_estudiante=" + idEstudiante
                        + " no existe. Se omite matriculacion (id=" + record.get("id") + ").");
                return;
            }
            if (optC.isEmpty()) {
                System.out.println("[CargarDatosCSV]   ADVERTENCIA: id_carrera=" + idCarrera
                        + " no existe. Se omite matriculacion (id=" + record.get("id") + ").");
                return;
            }

            EstudianteCarrera ec = new EstudianteCarrera(
                    entero(record, "id"),
                    optE.get(),
                    optC.get(),
                    entero(record, "inscripcion"),
                    entero(record, "graduacion"),
                    entero(record, "antiguedad")
            );
            estudianteCarreraRepo.save(ec);
        });
    }

    private void cargar(String nombreArchivo, RecordConsumer consumer) {
        final byte[] bytes;
        try {
            bytes = leerBytes(nombreArchivo);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar " + nombreArchivo + ": " + ex.getMessage(), ex);
        }
        List<Charset> charsets = Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1);
        IOException ultimo = null;

        for (Charset cs : charsets) {
            try (Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), cs));
                 CSVParser parser = CSVFormat.DEFAULT.builder()
                         .setHeader()
                         .setSkipHeaderRecord(true)
                         .setTrim(true)
                         .build()
                         .parse(reader)) {

                int cargados = 0;
                for (CSVRecord record : parser) {
                    try {
                        consumer.accept(record);
                        cargados++;
                    } catch (RuntimeException ex) {
                        throw new IllegalStateException("Error en " + nombreArchivo
                                + ", registro " + record.getRecordNumber(), ex);
                    }
                }
                System.out.println("[CargarDatosCSV]   " + nombreArchivo + " (" + cs.name() + ") -> " + cargados + " registros insertados.");
                return;

            } catch (IOException ex) {
                ultimo = ex;
            }
        }

        throw new IllegalStateException("No se pudo cargar " + nombreArchivo + ": "
                + (ultimo != null ? ultimo.getMessage() : "fallo de encoding"), ultimo);
    }

    private byte[] leerBytes(String nombreArchivo) throws IOException {
        InputStream recurso = CargarDatosCSV.class.getClassLoader()
                .getResourceAsStream(DATOS_CLASSPATH + nombreArchivo);
        if (recurso != null) {
            try {
                return recurso.readAllBytes();
            } finally {
                recurso.close();
            }
        }

        List<Path> candidatos = Arrays.asList(
                Paths.get("DB", nombreArchivo),
                Paths.get("segundaentrega", "DB", nombreArchivo)
        );
        for (Path candidato : candidatos) {
            if (Files.isRegularFile(candidato)) {
                return Files.readAllBytes(candidato);
            }
        }
        throw new IOException("Archivo no encontrado en classpath ni en " + candidatos);
    }

    private int entero(CSVRecord record, String columna) {
        return Integer.parseInt(record.get(columna));
    }

    @FunctionalInterface
    private interface RecordConsumer {
        void accept(CSVRecord record);
    }
}
