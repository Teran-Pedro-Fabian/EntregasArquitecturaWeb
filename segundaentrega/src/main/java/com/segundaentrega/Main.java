package com.segundaentrega;

import com.segundaentrega.Repository.CarreraRepository;
import com.segundaentrega.Repository.EstudianteCarreraRepository;
import com.segundaentrega.Repository.EstudianteRepository;
import com.segundaentrega.dto.ConteoCarreraAnualDTO;
import com.segundaentrega.dto.ReporteCarreraAnualDTO;
import com.segundaentrega.Entitys.CarreraEntity;
import com.segundaentrega.Entitys.EstudianteCarrera;
import com.segundaentrega.Entitys.EstudianteEntity;
import com.segundaentrega.utils.CargarDatosCSV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Main.class, args);

        CargarDatosCSV cargarDatosCSV = ctx.getBean(CargarDatosCSV.class);
        cargarDatosCSV.cargarTodo();

        EstudianteRepository estudianteRepo = ctx.getBean(EstudianteRepository.class);
        CarreraRepository carreraRepo = ctx.getBean(CarreraRepository.class);
        EstudianteCarreraRepository estudianteCarreraRepo = ctx.getBean(EstudianteCarreraRepository.class);

        separador();
        System.out.println("TP2 - Arquitectura Web - Ejecucion del enunciado");
        separador();

        punto2a(estudianteRepo);
        punto2b(estudianteCarreraRepo, estudianteRepo, carreraRepo);
        punto2c(estudianteRepo);
        punto2d(estudianteRepo);
        punto2e(estudianteRepo);
        punto2f(carreraRepo);
        punto2g(estudianteCarreraRepo, carreraRepo);
        punto3(estudianteCarreraRepo);

        separador();
        System.out.println("Fin de la ejecucion.");
        separador();

        cargarDatosCSV.vaciarDB();

        ctx.close();
    }

    private static void punto2a(EstudianteRepository repo) {
        encabezado("2a) Dar de alta un estudiante");
        if (repo.existsById(99999999)) {
            repo.deleteById(99999999);
            System.out.println("  (Limpieza previa: Estudiante DNI=99999999 eliminado)");
        }
        EstudianteEntity e = new EstudianteEntity(99999999, "TestFake", "Estudiante", 25, "M", "Tandil", 9999, null);
        repo.save(e);
        Optional<EstudianteEntity> opt = repo.findById(99999999);
        if (opt.isPresent()) {
            EstudianteEntity guardado = opt.get();
            System.out.printf("  OK: Estudiante guardado -> DNI=%d, LU=%d, %s %s, ciudad=%s%n",
                    guardado.getDNI(), guardado.getLU(), guardado.getNombre(),
                    guardado.getApellido(), guardado.getCiudad());
            //borrar estudiante fake para limpiar datos posteriores
            repo.deleteById(99999999);
        } else {
            System.out.println("  ERROR: No se encontro el estudiante despues de guardar.");
        }
    }

    private static void punto2b(EstudianteCarreraRepository ecRepo,
                                EstudianteRepository estudianteRepo,
                                CarreraRepository carreraRepo) {
        encabezado("2b) Matricular un estudiante en una carrera");
        Optional<EstudianteEntity> optE = estudianteRepo.findById(99999999);
        if (optE.isEmpty()) {
            System.out.println("  ERROR: Estudiante fake (DNI=99999999) no encontrado.");
            return;
        }
        List<CarreraEntity> carreras = carreraRepo.findAll();
        if (carreras.isEmpty()) {
            System.out.println("  ERROR: No hay carreras cargadas para matricular.");
            return;
        }
        if (ecRepo.existsById(99999)) {
            ecRepo.deleteById(99999);
            System.out.println("  (Limpieza previa: Matriculacion id=99999 eliminada)");
        }
        CarreraEntity carrera = carreras.get(0);
        EstudianteEntity estudiante = optE.get();
        EstudianteCarrera ec = new EstudianteCarrera(99999, estudiante, carrera, 2024, 0, 1);
        ecRepo.save(ec);

        Optional<EstudianteCarrera> opt = ecRepo.findById(99999);
        if (opt.isEmpty()) {
            System.out.println("  ERROR: Matriculacion id=99999 no encontrada despues de guardar.");
            return;
        }
        ec = opt.get();
        System.out.printf("  OK: Matriculacion guardada (id=%d)%n", ec.getId());
        System.out.printf("     -> Estudiante: DNI=%d (%s %s)%n",
                estudiante.getDNI(), estudiante.getNombre(), estudiante.getApellido());
        System.out.printf("     -> Carrera: id=%d (%s, duracion=%d anios)%n",
                carrera.getId(), carrera.getCarrera(), carrera.getDuracion());
        System.out.printf("     -> Inscripcion=%d, Graduacion=%d, Antiguedad=%d%n",
                ec.getInscripcion(), ec.getGraduacion(), ec.getAntiguedad());

        ecRepo.deleteById(99999);
    }

    private static void punto2c(EstudianteRepository repo) {
        encabezado("2c) Recuperar todos los estudiantes (orden simple por DNI ASC)");
        List<EstudianteEntity> estudiantes = repo.FindAllOrderByDNI();
        System.out.println("Total estudiantes: " + estudiantes.size());
        estudiantes.forEach(e -> System.out.printf("  DNI=%-10d LU=%-6d %s %s%n", 
                            e.getDNI(), e.getLU(), e.getNombre(), e.getApellido()));
    }

    private static void punto2d(EstudianteRepository repo) {
        encabezado("2d) Recuperar estudiante por numero de libreta universitaria");
        List<EstudianteEntity> todos = repo.FindAllOrderByDNI();
        if (todos.isEmpty()) {
            System.out.println("  Sin datos para probar.");
            return;
        }
        int luEjemplo = todos.get(0).getLU();
        EstudianteEntity e = repo.FindByNumeroDeLibreta(luEjemplo);
        if (e == null) {
            System.out.println("  No se encontro estudiante con LU=" + luEjemplo);
            return;
        }
        System.out.printf("  Encontrado: LU=%d -> DNI=%d %s %s (ciudad: %s)%n",
        e.getLU(), e.getDNI(), e.getNombre(), e.getApellido(), e.getCiudad());
        
        
    }

    private static void punto2e(EstudianteRepository repo) {
        encabezado("2e) Recuperar estudiantes por genero");
        List<EstudianteEntity> todos = repo.FindAllOrderByDNI();
        if (todos.isEmpty()) {
            System.out.println("  Sin datos para probar.");
            return;
        }
        Set<String> generos = todos.stream()
                .map(EstudianteEntity::getGenero)
                .collect(Collectors.toSet());
        System.out.println("  Generos encontrados: " + generos);
        generos.forEach(g -> System.out.println("  Genero '" + g + "': " + repo.FindByGenero(g).size() + " estudiantes"));
    }

    private static void punto2f(CarreraRepository repo) {
        encabezado("2f) Carreras con estudiantes inscriptos, ordenadas por cantidad de inscriptos DESC");
        List<CarreraEntity> carreras = repo.FindCarrerasOrderByInscriptos();
        System.out.println("Total carreras con inscriptos: " + carreras.size());
        carreras.forEach(c -> System.out.printf("  ID=%-4d %-40s duracion=%d anios | inscriptos=%d%n",
                c.getId(), c.getCarrera(), c.getDuracion(),
                (c.getEstudiantes() != null) ? c.getEstudiantes().size() : 0));
    }

    private static void punto2g(EstudianteCarreraRepository ecRepo, CarreraRepository carreraRepo) {
        encabezado("2g) Estudiantes de una carrera filtrados por ciudad de residencia");
        List<CarreraEntity> carreras = carreraRepo.FindCarrerasOrderByInscriptos();
        if (carreras.isEmpty()) {
            System.out.println("  Sin datos de carreras para probar.");
            return;
        }
        CarreraEntity carrera = carreras.get(0);
        String ciudadPrueba = "Rauch";
        List<EstudianteEntity> estudiantes = ecRepo.FindEstudiantesByCarreraAndCiudad(carrera.getId(), ciudadPrueba);
        System.out.printf("  Carrera: id=%d -> %s%n", carrera.getId(), carrera.getCarrera());
        System.out.printf("  Ciudad filtrada: '%s'%n", ciudadPrueba);
        System.out.printf("  Total estudiantes encontrados: %d%n", estudiantes.size());
        if (estudiantes.isEmpty()) {
            System.out.println("  (No hay estudiantes de esa ciudad en esta carrera)");
        } else {
            estudiantes.forEach(e -> System.out.printf("    -> DNI=%-10d LU=%-6d %s %s (ciudad=%s)%n",
                    e.getDNI(), e.getLU(), e.getNombre(), e.getApellido(), e.getCiudad()));
        }
    }

    /* private static void limpiarDatosFake(EstudianteRepository estudianteRepo,
                                         EstudianteCarreraRepository ecRepo) {
        encabezado("Limpieza de datos fake insertados durante la prueba");
        if (ecRepo.existsById(99999)) {
            ecRepo.deleteById(99999);
            System.out.println("  OK: EstudianteCarrera fake (id=99999) eliminado.");
        } else {
            System.out.println("  INFO: No existia EstudianteCarrera con id=99999 para borrar.");
        }
        if (estudianteRepo.existsById(99999999)) {
            estudianteRepo.deleteById(99999999);
            System.out.println("  OK: Estudiante fake (DNI=99999999) eliminado.");
        } else {
            System.out.println("  INFO: No existia Estudiante con DNI=99999999 para borrar.");
        }
        if (!estudianteRepo.existsById(99999999) && !ecRepo.existsById(99999)) {
            System.out.println("  Verificacion final: No quedan residuos de datos fake en la base.");
        }
    } */

    private static void punto3(EstudianteCarreraRepository repo) {
        encabezado("3) Reporte de carreras: inscriptos y egresados por anio (orden alfabetico + cronologico)");
        System.out.println();

        List<ConteoCarreraAnualDTO> inscriptos = repo.countInscriptosPorCarreraYAnio();
        List<ConteoCarreraAnualDTO> egresados  = repo.countEgresadosPorCarreraYAnio();

        Map<String, ReporteCarreraAnualDTO> merged = new LinkedHashMap<>();

        for (ConteoCarreraAnualDTO dto : inscriptos) {
            String key = dto.getCarrera() + "_" + dto.getAnio();
            merged.put(key, new ReporteCarreraAnualDTO(dto.getCarrera(), dto.getAnio(), dto.getCantidad(), 0L));
        }

        for (ConteoCarreraAnualDTO dto : egresados) {
            String key = dto.getCarrera() + "_" + dto.getAnio();
            if (merged.containsKey(key)) {
                merged.get(key).setEgresados(dto.getCantidad());
            } else {
                merged.put(key, new ReporteCarreraAnualDTO(dto.getCarrera(), dto.getAnio(), 0L, dto.getCantidad()));
            }
        }

        List<ReporteCarreraAnualDTO> reporte = new ArrayList<>(merged.values());
        reporte.sort(Comparator
                .comparing(ReporteCarreraAnualDTO::getCarrera)
                .thenComparingInt(ReporteCarreraAnualDTO::getAnio));

        System.out.println(String.format("%-40s | %-6s | %-16s | %-16s",
                "CARRERA", "ANIO", "INSCRIPTOS", "EGRESADOS"));
        System.out.println("-".repeat(95));
        reporte.forEach(r -> System.out.printf("%-40s | %-6d | inscriptos: %-6d | egresados: %-6d%n",
                r.getCarrera(), r.getAnio(), r.getInscriptos(), r.getEgresados()));
    }

    private static void encabezado(String titulo) {
        System.out.println();
        System.out.println("===== " + titulo + " =====");
    }

    private static void separador() {
        System.out.println("=".repeat(95));
    }
}
