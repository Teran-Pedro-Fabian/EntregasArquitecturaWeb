import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class TimeoutLentoTest {
    @Test
    void cortaUnaRespuestaQueTardaMasQueElTimeout() throws Exception {
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/lento", intercambio -> {
            try {
                Thread.sleep(350);
                intercambio.sendResponseHeaders(200, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (java.io.IOException ignored) {
            } finally {
                intercambio.close();
            }
        });
        servidor.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "timeout-test");
            t.setDaemon(true);
            return t;
        }));
        servidor.start();
        try {
            var factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(500);
            factory.setReadTimeout(100);
            var rest = new RestTemplate(factory);
            assertThrows(ResourceAccessException.class, () ->
                    rest.getForObject("http://127.0.0.1:" + servidor.getAddress().getPort() + "/lento", String.class));
        } finally {
            servidor.stop(0);
        }
    }
}
