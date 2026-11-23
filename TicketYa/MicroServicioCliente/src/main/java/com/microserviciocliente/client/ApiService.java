package com.microserviciocliente.client;

import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LugarEntityDTO;
import com.microserviciocliente.dto.ParticipanteEntityDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ApiService {

    private final RestClient restClientEvento;
    private final RestClient restClientLugar;

    public ApiService(RestClient.Builder builder,
                      @Value("${microservices.eventos.url}") String urlEvento,
                      @Value("${microservices.lugar.url}") String urlLugar) {
        this.restClientEvento = builder.clone().requestInterceptor((request, body, execution) -> {
            String trace = org.slf4j.MDC.get("traceId");
            if (trace != null) request.getHeaders().set("X-Trace-Id", trace);
            return execution.execute(request, body);
        }).baseUrl(urlEvento).build();
        this.restClientLugar = builder.clone().requestInterceptor((request, body, execution) -> {
            String trace = org.slf4j.MDC.get("traceId");
            if (trace != null) request.getHeaders().set("X-Trace-Id", trace);
            return execution.execute(request, body);
        }).baseUrl(urlLugar).build();
    }

    // Busca los eventos publicados con su precio desde el servicio Evento
    public List<EventoEntityDTO> buscarEventos() {
        return restClientEvento.get()
                .uri("/evento/buscar")
                .retrieve()
                .body(new ParameterizedTypeReference<List<EventoEntityDTO>>() {});
    }

    // El filtro por participante ya existe en Evento
    public List<EventoEntityDTO> buscarPorParticipante(String nombre) {
        return restClientEvento.get()
                .uri(uri -> uri.path("/evento/buscar")
                        .queryParam("participante", nombre)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<EventoEntityDTO>>() {});
    }

    public List<ParticipanteEntityDTO> listarParticipantes() {
        return restClientEvento.get()
                .uri("/evento/participantes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ParticipanteEntityDTO>>() {});
    }

    public List<LugarEntityDTO> listarLugares() {
        return restClientLugar.get()
                .uri("/lugares")
                .retrieve()
                .body(new ParameterizedTypeReference<List<LugarEntityDTO>>() {});
    }
}
