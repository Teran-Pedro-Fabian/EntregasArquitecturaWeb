import com.microServicioEvento.config.CircuitBreakerGetInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CircuitBreakerGetInterceptorTest {
    private HttpRequest peticion(HttpMethod metodo) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(metodo);
        when(request.getURI()).thenReturn(URI.create("http://lugar:8083/lugares"));
        return request;
    }
    private ClientHttpResponse respuesta(HttpStatus estado) {
        return new MockClientHttpResponse(new byte[0], estado);
    }

    @Test
    void abreCircuitoTrasTresFallosYDevuelve503SinLlamarDestino() throws Exception {
        var breaker = new CircuitBreakerGetInterceptor(3, 10000);
        var get = peticion(HttpMethod.GET);
        var ejecucion = mock(ClientHttpRequestExecution.class);
        when(ejecucion.execute(any(), any())).thenAnswer(inv -> respuesta(HttpStatus.SERVICE_UNAVAILABLE));
        for (int i = 0; i < 3; i++) assertEquals(503, breaker.intercept(get, new byte[0], ejecucion).getStatusCode().value());
        var fallback = breaker.intercept(get, new byte[0], ejecucion);
        assertEquals(503, fallback.getStatusCode().value());
        assertEquals("circuit-open", fallback.getHeaders().getFirst("X-TicketYa-Fallback"));
        verify(ejecucion, times(3)).execute(any(), any());
    }

    @Test
    void recuperaServicioDespuesDePausa() throws Exception {
        var breaker = new CircuitBreakerGetInterceptor(1, 60);
        var get = peticion(HttpMethod.GET);
        var ejecucion = mock(ClientHttpRequestExecution.class);
        when(ejecucion.execute(any(), any())).thenAnswer(inv -> respuesta(HttpStatus.BAD_GATEWAY))
                .thenAnswer(inv -> respuesta(HttpStatus.OK));
        assertEquals(502, breaker.intercept(get, new byte[0], ejecucion).getStatusCode().value());
        assertEquals("circuit-open", breaker.intercept(get, new byte[0], ejecucion).getHeaders().getFirst("X-TicketYa-Fallback"));
        Thread.sleep(80);
        assertEquals(200, breaker.intercept(get, new byte[0], ejecucion).getStatusCode().value());
        verify(ejecucion, times(2)).execute(any(), any());
    }

    @Test
    void postNuncaEsBloqueadoNiRepetido() throws Exception {
        var breaker = new CircuitBreakerGetInterceptor(1, 10000);
        var post = peticion(HttpMethod.POST);
        var ejecucion = mock(ClientHttpRequestExecution.class);
        when(ejecucion.execute(any(), any())).thenAnswer(inv -> respuesta(HttpStatus.SERVICE_UNAVAILABLE));
        assertEquals(503, breaker.intercept(post, new byte[0], ejecucion).getStatusCode().value());
        assertEquals(503, breaker.intercept(post, new byte[0], ejecucion).getStatusCode().value());
        verify(ejecucion, times(2)).execute(any(), any());
    }
}
