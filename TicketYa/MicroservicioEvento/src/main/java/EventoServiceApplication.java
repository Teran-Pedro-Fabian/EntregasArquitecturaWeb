import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventoServiceApplication {

    public static void main(String[] args) {
        // Arranca el microservicio de Eventos
        SpringApplication.run(EventoServiceApplication.class, args);

        // Mensaje de confirmación en la consola
        System.out.println("📅 Microservicio de Eventos corriendo en el puerto 8082...");
    }
}