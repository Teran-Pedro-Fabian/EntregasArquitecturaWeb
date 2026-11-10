package com.segundaentrega;

import com.segundaentrega.Repository.CarreraRepository;
import com.segundaentrega.Repository.EstudianteCarreraRepository;
import com.segundaentrega.Repository.EstudianteRepository;
import com.segundaentrega.Entitys.CarreraEntity;
import com.segundaentrega.Entitys.EstudianteCarrera;
import com.segundaentrega.Entitys.EstudianteCarreraId;
import com.segundaentrega.Entitys.EstudianteEntity;
import com.segundaentrega.dto.ConteoCarreraAnualDTO;
import com.segundaentrega.dto.ReporteCarreraAnualDTO;
import com.segundaentrega.utils.CargarDatosCSV;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final int DNI_PRUEBA = 99999999;

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SegundaEntrega");
        EntityManager em = emf.createEntityManager();

        try {
            // Crear repositorios
            EstudianteRepository estudianteRepo = new EstudianteRepository(em);
            CarreraRepository carreraRepo = new CarreraRepository(em);
            EstudianteCarreraRepository estudianteCarreraRepo = new EstudianteCarreraRepository(em);

            // Cargar CSV solo cuando la BD este preparada

            boolean cargarCSV = em.createQuery(
                    "SELECT COUNT(e) FROM EstudianteEntity e", Long.class).
                    getSingleResult() == 0L;

            if (cargarCSV) {
                CargarDatosCSV cargarDatosCSV = new CargarDatosCSV(em);
                cargarDatosCSV.cargarTodo();
            }
            separador();
            System.out.println("Ejecucion del tp2");
            separador();
            punto2a(em);
            try {
                punto2b(em, estudianteCarreraRepo);
            } finally {
                limpiarDatosPrueba(em);
            }
            punto2c(estudianteRepo);
            punto2d(estudianteRepo);
            punto2e(estudianteRepo);
            punto2f(carreraRepo);
            punto2g(estudianteCarreraRepo, carreraRepo);
            punto3(estudianteCarreraRepo);
            separador();
            System.out.println("Fin de la ejecucion.");
            separador();

        } finally {
            em.close();
            emf.close();
        }
    }

    // Punto 2a - Dar de alta un estudiante
    private static void punto2a(EntityManager em) {
        encabezado("2a) Dar de alta un estudiante");

        EstudianteEntity estudiante = em.find(EstudianteEntity.class, DNI_PRUEBA);
        if (estudiante == null) {
            estudiante = new EstudianteEntity(
                    DNI_PRUEBA,
                    "TestFake",
                    "Estudiante",
                    25,
                    "M",
                    "Tandil",
                    9999,
                    null
            );
            EstudianteEntity nuevo = estudiante;
            ejecutarTransaccion(em, () -> em.persist(nuevo));
        }

        EstudianteEntity guardado = em.find(EstudianteEntity.class, DNI_PRUEBA);
        System.out.printf(
                "OK: Estudiante guardado -> DNI=%d, LU=%d, %s %s, ciudad=%s%n",
                guardado.getDNI(),
                guardado.getLU(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getCiudad()
        );
    }

    // Punto 2b - Matricular estudiante en una carrera
    private static void punto2b(EntityManager em, EstudianteCarreraRepository ecRepo) {

        encabezado("2b) Matricular un estudiante en una carrera");
        EstudianteEntity estudiante = em.find(EstudianteEntity.class, DNI_PRUEBA);

        if (estudiante == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }

        List<CarreraEntity> carreras = em.createQuery(
                "SELECT c FROM CarreraEntity c ORDER BY c.id",
                CarreraEntity.class).setMaxResults(1).getResultList();

        if (carreras.isEmpty()) {
            System.out.println("No hay carreras disponibles.");
            return;
        }

        CarreraEntity carrera = carreras.get(0);
        EstudianteCarreraId id = new EstudianteCarreraId(estudiante.getDNI(), carrera.getId());
        EstudianteCarrera ec = em.find(EstudianteCarrera.class, id);

        if (ec == null) {
            ec = new EstudianteCarrera(
                    id,
                    estudiante,
                    carrera,
                    2024,
                    0,
                    1
            );
            ecRepo.Insert(ec);
        }

        System.out.printf(
                "OK: Matriculacion -> Estudiante=%d, Carrera=%d%n",
                ec.getId().getIdEstudiante(),
                ec.getId().getIdCarrera()
        );

        System.out.printf(
                "Estudiante: %s %s%n",
                estudiante.getNombre(),
                estudiante.getApellido()
        );
        System.out.println("Carrera: " + carrera.getCarrera());
        System.out.printf(
                "Inscripcion=%d, Graduacion=%d, Antiguedad=%d%n",
                ec.getInscripcion(),
                ec.getGraduacion(),
                ec.getAntiguedad()
        );
    }


    // Limpiar los datos de prueba
    private static void limpiarDatosPrueba(EntityManager em) {
        em.clear();
        ejecutarTransaccion(em, () -> {
            // Eliminar las inscripciones del estudiante
            int inscripciones = em.createQuery(
                            "DELETE FROM EstudianteCarrera ec " +
                                    "WHERE ec.estudiante.DNI = :dni"
                    ).setParameter("dni", DNI_PRUEBA).executeUpdate();
            // Eliminar el estudiante de prueba
            int estudiantes = em.createQuery(
                            "DELETE FROM EstudianteEntity e " +
                                    "WHERE e.DNI = :dni"
                    ).setParameter("dni", DNI_PRUEBA).executeUpdate();
            System.out.println("Inscripciones eliminadas: " + inscripciones);
            System.out.println("Estudiantes eliminados: " + estudiantes);
        });
        em.clear();
    }

    // Punto 2c - Estudiantes ordenados por DNI
    private static void punto2c(EstudianteRepository repo) {
        encabezado("2c) Recuperar estudiantes ordenados por DNI");
        List<EstudianteEntity> estudiantes = repo.FindAllOrderByDNI();
        System.out.println("Total estudiantes: " + estudiantes.size());
        estudiantes.forEach(e -> System.out.printf(
                "DNI=%d LU=%d %s %s%n",
                e.getDNI(),
                e.getLU(),
                e.getNombre(),
                e.getApellido()
        ));
    }

    // Punto 2d - Buscar estudiante por libreta
    private static void punto2d(EstudianteRepository repo) {
        encabezado("2d) Buscar estudiante por LU");
        List<EstudianteEntity> todos = repo.FindAllOrderByDNI();
        if (todos.isEmpty()) {
            System.out.println("Sin estudiantes.");
            return;
        }
        int lu = todos.get(0).getLU();
        EstudianteEntity estudiante = repo.FindByNumeroDeLibreta(lu);
        if (estudiante != null) {
            System.out.printf(
                    "LU=%d DNI=%d %s %s%n",
                    estudiante.getLU(),
                    estudiante.getDNI(),
                    estudiante.getNombre(),
                    estudiante.getApellido()
            );
        }
    }

    // Punto 2e - Recuperar estudiantes por genero
    private static void punto2e(EstudianteRepository repo) {
        encabezado("2e) Recuperar estudiantes por genero");
        List<EstudianteEntity> estudiantes = repo.FindAllOrderByDNI();
        Set<String> generos = estudiantes.stream()
                .map(EstudianteEntity::getGenero)
                .collect(Collectors.toSet());
        for (String genero : generos) {
            List<EstudianteEntity> filtrados = repo.FindByGenero(genero);
            System.out.println("Genero " + genero + ": " + filtrados.size() + " estudiantes");
        }
    }

    // Punto 2f - Carreras ordenadas por inscriptos
    private static void punto2f(CarreraRepository repo) {
        encabezado("2f) Carreras ordenadas por inscriptos");
        List<CarreraEntity> carreras = repo.FindCarrerasOrderByInscriptos();
        for (CarreraEntity carrera : carreras) {
            int inscriptos = carrera.getEstudiantes() == null ? 0: carrera.getEstudiantes().size();
            System.out.printf(
                    "Carrera=%s | Inscriptos=%d%n",
                    carrera.getCarrera(),
                    inscriptos
            );
        }
    }

    // Punto 2g - Estudiantes por carrera y ciudad
    private static void punto2g(EstudianteCarreraRepository ecRepo, CarreraRepository carreraRepo) {
        encabezado("2g) Estudiantes por carrera y ciudad");
        List<CarreraEntity> carreras = carreraRepo.FindCarrerasOrderByInscriptos();
        if (carreras.isEmpty()) {
            System.out.println("Sin carreras con inscriptos.");
            return;
        }
        CarreraEntity carrera = carreras.get(0);
        String ciudad = "Rauch";
        List<EstudianteEntity> estudiantes = ecRepo.FindEstudiantesByCarreraAndCiudad(carrera.getId(), ciudad);
        System.out.println("Carrera: " + carrera.getCarrera());
        System.out.println("Ciudad: " + ciudad);
        System.out.println("Total: " + estudiantes.size());

        for (EstudianteEntity e : estudiantes) {
            System.out.println(
                    e.getDNI() + " - " +
                            e.getNombre() + " " + e.getApellido()
            );
        }
    }

    // Punto 3 - Reporte anual de carreras
    private static void punto3(EstudianteCarreraRepository repo) {
        encabezado("3) Reporte de carreras");
        List<ConteoCarreraAnualDTO> inscriptos = repo.countInscriptosPorCarreraYAnio();
        List<ConteoCarreraAnualDTO> egresados = repo.countEgresadosPorCarreraYAnio();
        Map<String, ReporteCarreraAnualDTO> merged = new LinkedHashMap<>();
        for (ConteoCarreraAnualDTO dto : inscriptos) {
            String key = dto.getCarrera() + "_" + dto.getAnio();
            merged.put(
                    key,
                    new ReporteCarreraAnualDTO(
                            dto.getCarrera(),
                            dto.getAnio(),
                            dto.getCantidad(),
                            0L
                    )
            );
        }

        for (ConteoCarreraAnualDTO dto : egresados) {
            String key = dto.getCarrera() + "_" + dto.getAnio();
            if (merged.containsKey(key)) {
                merged.get(key).setEgresados(dto.getCantidad());
            } else {
                merged.put(
                        key,
                        new ReporteCarreraAnualDTO(
                                dto.getCarrera(),
                                dto.getAnio(),
                                0L,
                                dto.getCantidad()
                        )
                );
            }
        }

        List<ReporteCarreraAnualDTO> reporte = new ArrayList<>(merged.values());
        reporte.sort(Comparator.comparing(ReporteCarreraAnualDTO::getCarrera)
                        .thenComparingInt(ReporteCarreraAnualDTO::getAnio));
        System.out.printf(
                "%-40s | %-6s | %-12s | %-12s%n",
                "CARRERA", "ANIO", "INSCRIPTOS", "EGRESADOS");
        System.out.println("-".repeat(80));
        for (ReporteCarreraAnualDTO r : reporte) {
            System.out.printf(
                    "%-40s | %-6d | %-12d | %-12d%n",
                    r.getCarrera(),
                    r.getAnio(),
                    r.getInscriptos(),
                    r.getEgresados()
            );
        }
    }

    // Ejecutar una operacion dentro de una transaccion
    private static void ejecutarTransaccion(EntityManager em, Runnable operacion) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            operacion.run();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    private static void encabezado(String titulo) {
        System.out.println();
        System.out.println("===== " + titulo + " =====");
    }
    private static void separador() {
        System.out.println("=".repeat(80));
    }
}
