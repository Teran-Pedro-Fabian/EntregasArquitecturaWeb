package com.microservicioticket.service;

import com.microservicioticket.dto.ColaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Service
public class ColaService {

    private static final String PREFIJO = "fila:evento:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ticketya.cola.tanda-size:10}")
    private int tandaSize;

    @Value("${ticketya.cola.ttl-minutos:60}")
    private long ttlMinutos;

    public ColaResponse entrar(String idEvento, String idCliente) {
        validarDatos(idEvento, idCliente);

        Map<String, Object> evento = restTemplate.getForObject(
                "http://localhost:8082/evento/" + idEvento, Map.class);
        if (evento == null || !"PUBLICADO".equals(evento.get("estado"))) {
            throw new IllegalStateException("El evento no esta publicado");
        }
        if (!"ALTA".equalsIgnoreCase(String.valueOf(evento.get("demanda")))) {
            throw new IllegalStateException("El evento no requiere fila virtual");
        }

        String clave = claveFila(idEvento);
        ZSetOperations<String, String> operaciones = redisTemplate.opsForZSet();

        // La secuencia de Redis permite ordenar clientes entre varias instancias
        Long orden = redisTemplate.opsForValue().increment(claveSecuencia(idEvento));
        if (orden == null) {
            throw new IllegalStateException("No se pudo asignar un turno");
        }
        operaciones.addIfAbsent(clave, idCliente, orden.doubleValue());

        redisTemplate.expire(clave, Duration.ofMinutes(ttlMinutos));
        redisTemplate.expire(claveSecuencia(idEvento), Duration.ofMinutes(ttlMinutos));
        return respuesta(idEvento, idCliente);
    }

    public ColaResponse consultarPosicion(String idEvento, String idCliente) {
        validarDatos(idEvento, idCliente);
        if (redisTemplate.opsForZSet().score(claveFila(idEvento), idCliente) == null) {
            throw new IllegalArgumentException("El cliente no esta en la fila del evento");
        }
        return respuesta(idEvento, idCliente);
    }

    // La compra solo necesita fila cuando demanda es ALTA
    public void validarTurno(String idEvento, String idCliente, String demanda) {
        if (!"ALTA".equalsIgnoreCase(demanda)) {
            return;
        }
        validarDatos(idEvento, idCliente);
        Long posicion = redisTemplate.opsForZSet().rank(claveFila(idEvento), idCliente);
        if (posicion == null) {
            throw new IllegalStateException("Debes ingresar a la fila virtual antes de comprar");
        }
        if (posicion >= tandaSize) {
            throw new IllegalStateException("Todavia no estas habilitado para comprar");
        }
    }

    // Se retira al comprador confirmado para que avance la siguiente tanda
    public void finalizarCompra(String idEvento, String idCliente) {
        validarDatos(idEvento, idCliente);
        redisTemplate.opsForZSet().remove(claveFila(idEvento), idCliente);
    }

    private ColaResponse respuesta(String idEvento, String idCliente) {
        ZSetOperations<String, String> operaciones = redisTemplate.opsForZSet();
        String clave = claveFila(idEvento);
        Long posicion = operaciones.rank(clave, idCliente);
        Long total = operaciones.zCard(clave);
        int numero = posicion == null ? 0 : Math.toIntExact(posicion + 1);
        int cantidad = total == null ? 0 : Math.toIntExact(total);
        boolean habilitado = posicion != null && posicion < tandaSize;
        return new ColaResponse(idEvento, idCliente, numero, cantidad, habilitado);
    }

    private void validarDatos(String idEvento, String idCliente) {
        if (idEvento == null || idEvento.isBlank() || idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Evento y cliente son obligatorios");
        }
    }

    private String claveFila(String idEvento) {
        return PREFIJO + idEvento;
    }

    private String claveSecuencia(String idEvento) {
        return claveFila(idEvento) + ":secuencia";
    }
}
