import com.microserviciolugar.LugarServiceApplication;
import com.microserviciolugar.entity.SectorEntity;
import com.microserviciolugar.repository.ReservaGeneralRepository;
import com.microserviciolugar.repository.SectorRepository;
import com.microserviciolugar.service.ReservaGeneralService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = LugarServiceApplication.class,
        properties = {
                "spring.datasource.url=jdbc:postgresql://localhost:5436/ticketya_lugares_test",
                "spring.datasource.username=postgres",
                "spring.datasource.password=admin",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "spring.devtools.restart.enabled=false",
                "ticketya.reservas.limpieza-ms=3600000"
        }
)
public class LugarMultiInstanciaTest {

    @Autowired
    private ReservaGeneralService servicioA;

    @Autowired
    private SectorRepository sectorRepositoryA;

    @Autowired
    private ReservaGeneralRepository reservaRepositoryA;

    @Autowired
    private EntityManagerFactory entityManagerFactoryA;

    @Autowired
    private DataSource dataSourceA;

    @Test
    void dosInstanciasNoReservanDosVecesElUltimoCupo() throws Exception {

        ConfigurableApplicationContext contextoB = null;

        try {
            contextoB = new SpringApplicationBuilder(LugarServiceApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(
                            "--spring.application.name=microservicio-lugar-instancia-b",
                            "--spring.datasource.url=jdbc:postgresql://localhost:5436/ticketya_lugares_test",
                            "--spring.datasource.username=postgres",
                            "--spring.datasource.password=admin",
                            "--spring.datasource.driver-class-name=org.postgresql.Driver",
                            "--spring.jpa.hibernate.ddl-auto=none",
                            "--spring.jpa.show-sql=false",
                            "--spring.devtools.restart.enabled=false",
                            "--ticketya.reservas.limpieza-ms=3600000"
                    );

            ReservaGeneralService servicioB =
                    contextoB.getBean(ReservaGeneralService.class);

            SectorRepository sectorRepositoryB =
                    contextoB.getBean(SectorRepository.class);

            EntityManagerFactory entityManagerFactoryB =
                    contextoB.getBean(EntityManagerFactory.class);

            DataSource dataSourceB =
                    contextoB.getBean(DataSource.class);

            // instancias Spring/JPA distintas
            assertNotSame(servicioA, servicioB);
            assertNotSame(entityManagerFactoryA, entityManagerFactoryB);
            assertNotSame(dataSourceA, dataSourceB);

            // Verificar que ambas usan base de prueba
            String urlA;
            String urlB;

            try (Connection connectionA = dataSourceA.getConnection();
                 Connection connectionB = dataSourceB.getConnection()) {

                urlA = connectionA.getMetaData().getURL();
                urlB = connectionB.getMetaData().getURL();
            }

            System.out.println();
            System.out.println("BASES DE DATOS");
            System.out.println("Instancia A " + urlA);
            System.out.println("Instancia B " + urlB);

            assertEquals(
                    "jdbc:postgresql://localhost:5436/ticketya_lugares_test",
                    urlA
            );

            assertEquals(
                    urlA,
                    urlB,
                    "Las dos instancias deben compartir la misma PostgreSQL"
            );

            // Crear un sector con un cupo
            SectorEntity sector = new SectorEntity();
            sector.setNombre("Campo general multi-instancia");
            sector.setTipo("NO_NUMERADO");
            sector.setCupos(1);
            sector.setCuposReservados(0);
            sector.setCuposVendidos(0);
            sector.setIdEvento("evento-multi-instancia");

            Long idSector = sectorRepositoryA.saveAndFlush(sector).getId();

            /*
             * Verificar antes de empezar que ambas instancias ven
             * exactamente el mismo registro como NO_NUMERADO
             * Esto evita que el test pueda aprobar por conectarse
             * accidentalmente a otra base de datos
             */
            SectorEntity sectorVistoPorA =
                    sectorRepositoryA.findById(idSector).orElseThrow();

            SectorEntity sectorVistoPorB =
                    sectorRepositoryB.findById(idSector).orElseThrow();

            assertEquals("NO_NUMERADO", sectorVistoPorA.getTipo());
            assertEquals("NO_NUMERADO", sectorVistoPorB.getTipo());
            assertEquals(1, sectorVistoPorA.getCupos());
            assertEquals(1, sectorVistoPorB.getCupos());

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch preparados = new CountDownLatch(2);
            CountDownLatch inicio = new CountDownLatch(1);

            AtomicInteger exitos = new AtomicInteger();
            AtomicInteger rechazos = new AtomicInteger();
            AtomicReference<String> mensajeRechazo = new AtomicReference<>();

            executor.execute(() ->
                    intentarReserva(
                            servicioA,
                            idSector,
                            "cliente-instancia-a",
                            preparados,
                            inicio,
                            exitos,
                            rechazos,
                            mensajeRechazo
                    )
            );

            executor.execute(() ->
                    intentarReserva(
                            servicioB,
                            idSector,
                            "cliente-instancia-b",
                            preparados,
                            inicio,
                            exitos,
                            rechazos,
                            mensajeRechazo
                    )
            );

            assertTrue(
                    preparados.await(10, TimeUnit.SECONDS),
                    "Las dos instancias deben estar listas"
            );

            // Disparar las dos transacciones al mismo tiempo.
            inicio.countDown();

            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(30, TimeUnit.SECONDS),
                    "Las operaciones deben finalizar"
            );

            SectorEntity estadoFinal =
                    sectorRepositoryA.findById(idSector).orElseThrow();

            System.out.println();
            System.out.println("Exitos: " + exitos.get());
            System.out.println("Rechazos: " + rechazos.get());
            System.out.println("Motivo del rechazo: " + mensajeRechazo.get());
            System.out.println("Cupos reservados: " + estadoFinal.getCuposReservados());
            System.out.println("Cupos vendidos: " + estadoFinal.getCuposVendidos());
            System.out.println("Reservas en PostgreSQL: " + reservaRepositoryA.count());

            /*
             * Resultado correcto
             * 1) una sola reserva gana
             * 2) la otra falla POR FALTA DE CUPOS
             * 3) la base queda con una sola reserva
             */
            assertEquals(
                    1,
                    exitos.get(),
                    "Solo una instancia puede reservar el ultimo cupo"
            );

            assertEquals(
                    1,
                    rechazos.get(),
                    "La segunda instancia debe ser rechazada"
            );

            assertNotNull(
                    mensajeRechazo.get(),
                    "Debe registrarse el motivo del rechazo"
            );

            assertTrue(
                    mensajeRechazo.get().startsWith("No hay cupos suficientes"),
                    "El primer proceso consumio el ultimo cupo"
            );

            assertEquals(1, estadoFinal.getCuposReservados());
            assertEquals(0, estadoFinal.getCuposVendidos());

            assertEquals(
                    1,
                    reservaRepositoryA.count(),
                    "Debe haber una sola reserva en PostgreSQL"
            );

            System.out.println();
            System.out.println("Dos instancias compartieron la DB");
            System.out.println("Una obtuvo el ultimo cupo y la otra fue rechazada por falta de cupos");
            System.out.println("No hubo doble venta");
            System.out.println();

        } finally {
            if (contextoB != null) {
                contextoB.close();
            }
        }
    }

    private void intentarReserva(
            ReservaGeneralService servicio,
            Long idSector,
            String cliente,
            CountDownLatch preparados,
            CountDownLatch inicio,
            AtomicInteger exitos,
            AtomicInteger rechazos,
            AtomicReference<String> mensajeRechazo) {

        try {
            preparados.countDown();
            inicio.await();

            servicio.reservar(idSector, cliente, 1);
            exitos.incrementAndGet();

        } catch (Exception e) {
            rechazos.incrementAndGet();
            mensajeRechazo.compareAndSet(null, e.getMessage());

            System.out.println(
                    cliente + " rechazado: " + e.getMessage()
            );
        }
    }
}
