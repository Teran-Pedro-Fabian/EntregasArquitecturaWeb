package  com.microserviciocliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClienteServiceApplication {

    public static void main(String[] args) {
        // Este método arranca todo el ecosistema de Spring Boot
        SpringApplication.run(ClienteServiceApplication.class, args);
        System.out.println("🚀 Microservicio de Clientes corriendo en el puerto 8081...");
    }
}
