import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LugarServiceApplication {

    public static void main(String[] args) {
        // Arranca el microservicio de Eventos
        SpringApplication.run(LugarServiceApplication.class, args);

        // Mensaje de confirmación en la consola
        System.out.println("📅 Microservicio de Eventos corriendo en el puerto 8082...");
    }
}