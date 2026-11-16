package com.microserviciocliente.client;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.springframework.stereotype.Component;


import com.microserviciocliente.dto.BusquedaEventoFiltro;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LugarEntityDTO;
import com.microserviciocliente.dto.ParticipanteEntityDTO;

@Service
public class ApiService {

    private final RestClient restClientEvento;
    private final RestClient restClientLugar;
    

    // Inyectamos la URL base desde el application.properties / application.yml
    public ApiService(RestClient.Builder builder,   
                      @Value("${microservices.eventos.url}") String baseUrlEvento,
                      @Value("${microservices.lugar.url}") String baseUrlLugar) {
        this.restClientEvento = builder
                .baseUrl(baseUrlEvento)
                .build();
        this.restClientLugar = builder
                .baseUrl(baseUrlLugar)
                .build();
    }

    public List<EventoEntityDTO> buscarEventos(BusquedaEventoFiltro filtro) {
    return restClientEvento.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/evento/buscar")
                    .queryParam("tipo", filtro.getTipo())
                    .queryParam("organizador", filtro.getOrganizador())
                    .queryParam("participante", filtro.getParticipante())
                    .queryParam("ciudad", filtro.getCiudad())
                    .queryParam("fechaDesde", filtro.getFechaDesde())
                    .queryParam("fechaHasta", filtro.getFechaHasta())
                    .queryParam("texto", filtro.getTexto())
                    .queryParam("q", filtro.getQ())
                    .queryParam("lat", filtro.getLat())
                    .queryParam("lon", filtro.getLon())
                    .build())
            .retrieve()
            .body(new ParameterizedTypeReference<List<EventoEntityDTO>>() {});
}

    public EventoEntityDTO obtenerEventoPorId(String idEvento) {
        return restClientEvento.get()
                .uri("/evento/{id}", idEvento)
                .retrieve()
                .body(EventoEntityDTO.class);
    }

    public ParticipanteEntityDTO obtenerParticipantePorId(String idParticipante) {
        return restClientEvento.get()
                .uri("/participante/{id}", idParticipante)
                .retrieve()
                .body(ParticipanteEntityDTO.class);
    }

    public LugarEntityDTO obtenerLugarPorId(String idLugar) {
        return restClientLugar.get()
                .uri("/lugar/{id}", idLugar)
                .retrieve()
                .body(LugarEntityDTO.class);
    }


     







}



