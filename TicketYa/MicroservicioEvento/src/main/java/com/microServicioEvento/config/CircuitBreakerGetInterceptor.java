package com.microServicioEvento.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/** Corta las consultas GET cuando un servicio falla repetidamente */
public class CircuitBreakerGetInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerGetInterceptor.class);
    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();
    private final int maxFallos;
    private final long pausaMs;

    public CircuitBreakerGetInterceptor(int maxFallos, long pausaMs) {
        if (maxFallos < 1 || pausaMs < 1) throw new IllegalArgumentException("Circuit Breaker invalido");
        this.maxFallos = maxFallos;
        this.pausaMs = pausaMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // Las operaciones que modifican datos nunca pasan por el Circuit Breaker GET.
        if (request.getMethod() != HttpMethod.GET) return execution.execute(request, body);
        String destino = request.getURI().getAuthority();
        if (destino == null) destino = request.getURI().toString();
        Estado estado = estados.computeIfAbsent(destino, clave -> new Estado());
        boolean prueba = false;
        synchronized (estado) {
            long ahora = System.currentTimeMillis();
            if (estado.abiertoHasta > ahora || estado.sondeando) {
                return noDisponible();
            }
            if (estado.abiertoHasta != 0) {
                // Solo una llamada comprueba si el servicio se recupero
                estado.sondeando = true;
                prueba = true;
            }
        }
        try {
            // El reintento GET esta despues de este interceptor: se cuenta el resultado final
            ClientHttpResponse respuesta = execution.execute(request, body);
            int codigo;
            try {
                codigo = respuesta.getStatusCode().value();
            } catch (IOException e) {
                respuesta.close();
                throw e;
            }
            if (codigo >= 500) fallo(estado, destino, prueba);
            else exito(estado);
            return respuesta;
        } catch (IOException e) {
            // El error de red tambien abre el circuito cuando se repite
            // En el caso de estado HTTP ya devuelto no se entra a este catch
            fallo(estado, destino, prueba);
            throw e;
        }
    }

    private void fallo(Estado estado, String destino, boolean prueba) {
        synchronized (estado) {
            estado.sondeando = false;
            if (prueba || ++estado.fallos >= maxFallos) {
                estado.fallos = maxFallos;
                estado.abiertoHasta = System.currentTimeMillis() + pausaMs;
                log.warn("Circuit Breaker ABIERTO para {} por {} ms", destino, pausaMs);
            }
        }
    }

    private void exito(Estado estado) {
        synchronized (estado) {
            estado.fallos = 0;
            estado.abiertoHasta = 0;
            estado.sondeando = false;
        }
    }

    private ClientHttpResponse noDisponible() {
        // Fallback seguro: respuesta 503, sin inventar precios, cupos ni compras
        return new ClientHttpResponse() {
            private final byte[] contenido = "{\"mensaje\":\"Dependencia no disponible (circuito abierto)\"}"
                    .getBytes(StandardCharsets.UTF_8);
            @Override public HttpStatusCode getStatusCode() { return HttpStatusCode.valueOf(503); }
            @Override public String getStatusText() { return "Service Unavailable"; }
            @Override public void close() { }
            @Override public InputStream getBody() { return new ByteArrayInputStream(contenido); }
            @Override public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentLength(contenido.length);
                headers.set("X-TicketYa-Fallback", "circuit-open");
                return headers;
            }
        };
    }

    private static class Estado {
        int fallos;
        long abiertoHasta;
        boolean sondeando;
    }
}
