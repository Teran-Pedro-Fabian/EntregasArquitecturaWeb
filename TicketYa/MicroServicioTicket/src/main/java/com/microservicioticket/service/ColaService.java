package com.microservicioticket.service;

import com.microservicioticket.dto.ColaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ColaService {
    // Namespace propio de la fila virtual, distinto de las reservas de asientos
    private static final String PREFIXO_FILA = "fila:evento:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Cantidad de personas habilitadas por tanda en la fila virtual
    @Value("${ticketya.cola.tanda-size:10}")
    private int tandaSize;

    /**
     * Ingresar a la fila virtual de un evento
     * Se usa cuando el evento esta marcado como de alta demanda
     * Los clientes compran en orden de llegada
     */
    public ColaResponse entrar(String idEvento, String idCliente) {
        validar(idEvento, idCliente);
        String clave = claveFila(idEvento);
        ZSetOperations<String, String> operaciones = redisTemplate.opsForZSet();
        if (operaciones.score(clave, idCliente) == null) {
            Map<String, Double> puntuaciones = new HashMap<>();
            puntuaciones.put(idCliente, (double) Instant.now().toEpochMilli());
            operaciones.add(clave, (Set<TypedTuple<String>>) puntuaciones);
        }
        return respuesta(idEvento, idCliente);
    }

    /**
     * Consultar la posicion de un cliente dentro de la fila virtual de un evento
     * habilitado indica si el cliente tiene el primer lugar y puede comprar
     */
    public ColaResponse consultarPosicion(String idEvento, String idCliente) {
        validar(idEvento, idCliente);
        String clave = claveFila(idEvento);
        ZSetOperations<String, String> operaciones = redisTemplate.opsForZSet();
        if (operaciones.score(clave, idCliente) == null) {
            throw new IllegalArgumentException("El cliente no esta en la fila del evento");
        }
        return respuesta(idEvento, idCliente);
    }

    /**
     * Armar la respuesta con la posicion (1 es el primero en la fila)
     * y si el cliente tiene habilitado el turno de compra
     */
    private ColaResponse respuesta(String idEvento, String idCliente) {
        String clave = claveFila(idEvento);
        ZSetOperations<String, String> operaciones = redisTemplate.opsForZSet();
        Long posicion = operaciones.rank(clave, idCliente);
        // Tanda: habilita a los primeros 'tandaSize' lugares hasta que la lista se vacie
        boolean habilitado = posicion != null && posicion < tandaSize;
        long total = operaciones.zCard(clave);
        int posicionFinal = posicion == null ? 0 : posicion.intValue() + 1;
        return new ColaResponse(idEvento, idCliente, posicionFinal, (int) total, habilitado);
    }

    private void validar(String idEvento, String idCliente) {
        if (idEvento == null || idEvento.isBlank() || idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Fila virtual: evento y cliente no validos");
        }
    }

    private String claveFila(String idEvento) {
        return PREFIXO_FILA + idEvento;
    }
}
