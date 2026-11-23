package com.microServicioEvento.config;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/** Reintentar solo consultas GET cuando la respuesta es transitoria */
public class ReintentoGetInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ReintentoGetInterceptor.class);

    private final int reintentos;
    private final long demoraMs;

    public ReintentoGetInterceptor(int reintentos, long demoraMs) {
        if (reintentos < 0 || reintentos > 3 || demoraMs < 0 || demoraMs > 5000) {
            throw new IllegalArgumentException("Configuracion de reintentos no valida");
        }
        this.reintentos = reintentos;
        this.demoraMs = demoraMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // POST / PUT / DELETE no se repiten automaticamente
        // Las confirmaciones de compras se recuperan con su misma clave
        if (request.getMethod() != HttpMethod.GET) {
            return execution.execute(request, body);
        }

        for (int intento = 0; ; intento++) {
            ClientHttpResponse respuesta;
            try {
                respuesta = execution.execute(request, body);
            } catch (IOException e) {
                if (intento >= reintentos) throw e;
                log.warn("Consulta GET sin respuesta. Reintento {} de {}", intento + 1, reintentos);
                esperar();
                continue;
            }

            int codigo;
            try {
                codigo = respuesta.getStatusCode().value();
            } catch (IOException e) {
                respuesta.close();
                if (intento >= reintentos) throw e;
                log.warn("Consulta GET con respuesta incompleta. Reintento {} de {}",
                        intento + 1, reintentos);
                esperar();
                continue;
            }
            if (intento < reintentos && (codigo == 502 || codigo == 503 || codigo == 504)) {
                respuesta.close();
                log.warn("Consulta GET con HTTP {}. Reintento {} de {}", codigo,
                        intento + 1, reintentos);
                esperar();
                continue;
            }
            return respuesta;
        }
    }

    private void esperar() throws IOException {
        try {
            Thread.sleep(demoraMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Reintento interrumpido", e);
        }
    }
}
