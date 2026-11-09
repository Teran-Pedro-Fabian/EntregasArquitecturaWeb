package  com.microserviciocliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteServiceApplication.class, args);
        System.out.println("🚀 Microservicio de Clientes corriendo en el puerto 8081...");
    }
}
