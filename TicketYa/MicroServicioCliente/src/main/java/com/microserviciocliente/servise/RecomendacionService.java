package com.microserviciocliente.servise;

import com.microserviciocliente.client.ApiService;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LugarEntityDTO;
import com.microserviciocliente.dto.ParticipanteEntityDTO;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class RecomendacionService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PreferenciasRepository preferenciasRepository;
    @Autowired private ApiService apiService;

    // Combina preferencias propias con eventos guardados por otros clientes similares
    public List<EventoEntityDTO> obtenerRecomendaciones(String idCliente) {
        if (vacio(idCliente)) {
            throw new IllegalArgumentException("Falta el idCliente");
        }
        if (!clienteRepository.existsById(idCliente)) {
            throw new NoSuchElementException("Cliente no encontrado");
        }

        List<PreferenciasEntity> propias = preferenciasRepository.findByIdCliente(idCliente);
        if (propias == null || propias.isEmpty()) {
            return List.of();
        }

        List<EventoEntityDTO> eventos = apiService.buscarEventos();
        if (eventos == null || eventos.isEmpty()) {
            return List.of();
        }

        // No se recomiendan eventos que no se puedan comprar o que ya pasaron
        Map<String, EventoEntityDTO> candidatos = new LinkedHashMap<>();
        for (EventoEntityDTO evento : eventos) {
            if (evento != null && !vacio(evento.getId()) && disponible(evento)) {
                candidatos.putIfAbsent(evento.getId(), evento);
            }
        }
        if (candidatos.isEmpty()) {
            return List.of();
        }

        Map<Long, String> ciudadesPorLugar = new HashMap<>();
        List<LugarEntityDTO> lugares = apiService.listarLugares();
        if (lugares != null) {
            for (LugarEntityDTO lugar : lugares) {
                if (lugar != null && lugar.getId() != null) {
                    ciudadesPorLugar.put(lugar.getId(), normalizar(lugar.getCiudad()));
                }
            }
        }

        Set<String> idsEventos = new HashSet<>();
        Set<String> idsParticipantes = new HashSet<>();
        Set<String> idsLugares = new HashSet<>();
        Set<String> ciudades = new HashSet<>();
        Set<String> tipos = new HashSet<>();
        Set<String> organizadores = new HashSet<>();
        for (PreferenciasEntity pref : propias) {
            if (pref == null) continue;
            agregar(idsEventos, pref.getIdEvento());
            agregar(idsParticipantes, pref.getIdParticipante());
            agregar(idsLugares, pref.getIdLugar());
            agregarNormalizado(ciudades, pref.getCiudad());
            agregarNormalizado(tipos, pref.getTipo());
        }

        // Si guardo un evento tambien le interesan otros del mismo tipo u organizador
        for (String idEvento : idsEventos) {
            EventoEntityDTO favorito = candidatos.get(idEvento);
            if (favorito != null) {
                agregarNormalizado(tipos, favorito.getTipo());
                agregarNormalizado(organizadores, favorito.getOrganizador());
            }
        }

        Map<String, Integer> puntos = new HashMap<>();
        for (EventoEntityDTO evento : candidatos.values()) {
            int puntaje = 0;
            if (idsEventos.contains(evento.getId())) puntaje += 8;
            if (tipos.contains(normalizar(evento.getTipo()))) puntaje += 2;
            if (organizadores.contains(normalizar(evento.getOrganizador()))) puntaje += 1;
            if (evento.getIdLugar() != null) {
                if (idsLugares.contains(String.valueOf(evento.getIdLugar()))) puntaje += 4;
                if (ciudades.contains(ciudadesPorLugar.get(evento.getIdLugar()))) puntaje += 3;
            }
            if (puntaje > 0) puntos.put(evento.getId(), puntaje);
        }

        // Convertimos los IDs de artistas de Mongo a nombres para usar el filtro de Evento
        if (!idsParticipantes.isEmpty()) {
            List<ParticipanteEntityDTO> participantes = apiService.listarParticipantes();
            if (participantes != null) {
                for (ParticipanteEntityDTO participante : participantes) {
                    if (participante != null && idsParticipantes.contains(participante.getId())
                            && !vacio(participante.getNombre())) {
                        List<EventoEntityDTO> encontrados = apiService.buscarPorParticipante(participante.getNombre());
                        if (encontrados == null) continue;
                        for (EventoEntityDTO encontrado : encontrados) {
                            if (encontrado != null && candidatos.containsKey(encontrado.getId())) {
                                puntos.merge(encontrado.getId(), 5, Integer::sum);
                            }
                        }
                    }
                }
            }
        }

        // Colaborativo: otros clientes con alguna preferencia igual pueden sugerir sus eventos
        List<PreferenciasEntity> todas = preferenciasRepository.findAll();
        Map<String, List<PreferenciasEntity>> porCliente = new HashMap<>();
        if (todas != null) {
            for (PreferenciasEntity pref : todas) {
                if (pref != null && !vacio(pref.getIdCliente()) && !idCliente.equals(pref.getIdCliente())) {
                    porCliente.computeIfAbsent(pref.getIdCliente(), k -> new ArrayList<>()).add(pref);
                }
            }
        }
        for (List<PreferenciasEntity> preferenciasAjena : porCliente.values()) {
            boolean similares = preferenciasAjena.stream().anyMatch(pref ->
                    coincide(idsEventos, pref.getIdEvento())
                    || coincide(idsParticipantes, pref.getIdParticipante())
                    || coincide(idsLugares, pref.getIdLugar())
                    || coincideNormalizado(ciudades, pref.getCiudad())
                    || coincideNormalizado(tipos, pref.getTipo()));
            if (!similares) continue;

            // Cada cliente similar aporta el puntaje una sola vez por evento
            Set<String> recomendadosPorCliente = new HashSet<>();
            for (PreferenciasEntity pref : preferenciasAjena) {
                if (!vacio(pref.getIdEvento()) && candidatos.containsKey(pref.getIdEvento())) {
                    recomendadosPorCliente.add(pref.getIdEvento());
                }
            }
            for (String idEvento : recomendadosPorCliente) {
                puntos.merge(idEvento, 4, Integer::sum);
            }
        }

        List<EventoEntityDTO> resultado = new ArrayList<>();
        for (EventoEntityDTO evento : candidatos.values()) {
            if (puntos.getOrDefault(evento.getId(), 0) > 0) resultado.add(evento);
        }
        resultado.sort(Comparator
                .comparingInt((EventoEntityDTO e) -> puntos.get(e.getId())).reversed()
                .thenComparing(EventoEntityDTO::getFechaHora)
                .thenComparing(EventoEntityDTO::getId));
        return resultado.stream().limit(20).toList();
    }

    private boolean disponible(EventoEntityDTO evento) {
        if (!"PUBLICADO".equalsIgnoreCase(evento.getEstado()) || vacio(evento.getFechaHora())) {
            return false;
        }
        try {
            return LocalDateTime.parse(evento.getFechaHora()).isAfter(LocalDateTime.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String normalizar(String texto) {
        return vacio(texto) ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    private void agregar(Set<String> destino, String valor) {
        if (!vacio(valor)) destino.add(valor.trim());
    }

    private void agregarNormalizado(Set<String> destino, String valor) {
        if (!vacio(valor)) destino.add(normalizar(valor));
    }

    private boolean coincide(Set<String> valores, String valor) {
        return !vacio(valor) && valores.contains(valor.trim());
    }

    private boolean coincideNormalizado(Set<String> valores, String valor) {
        return !vacio(valor) && valores.contains(normalizar(valor));
    }
}
