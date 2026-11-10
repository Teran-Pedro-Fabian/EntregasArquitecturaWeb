package com.microServicioEvento.service;

import com.microServicioEvento.dto.BusquedaEventoFiltro;
import com.microServicioEvento.dto.EventoBusquedaDTO;
import com.microServicioEvento.entity.EventoEntity;
import com.microServicioEvento.entity.EventoParticipanteEntity;
import com.microServicioEvento.entity.ParticipanteEntity;
import com.microServicioEvento.repository.EventoRepository;
import com.microServicioEvento.repository.EventoParticipanteRepositoy;
import com.microServicioEvento.repository.ParticipanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private EventoParticipanteRepositoy eventoParticipanteRepository;
    @Autowired
    private ParticipanteRepository participanteRepository;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Crear un evento siempre en BORRADOR
     * El cliente no puede elegir el ID ni el estado
     */
    public EventoEntity crearEvento(EventoEntity evento) {
        validarDatos(evento);
        evento.setId(null);
        evento.setEstado("BORRADOR");
        normalizarDemanda(evento);
        return eventoRepository.save(evento);
    }

    /**
     * Editar un borrador
     * Solo lo puede hacer el organizador que creo el evento
     */
    public EventoEntity actualizarBorrador(String id, String idOrganizador, EventoEntity datos) {
        EventoEntity original = buscarObligatorio(id);
        verificarOrganizador(original, idOrganizador);
        exigirEstado(original, "BORRADOR");
        validarDatos(datos);
        if (!original.getOrganizador().equals(datos.getOrganizador())) {
            throw new IllegalArgumentException("No se puede cambiar el organizador del evento");
        }
        original.setNombre(datos.getNombre());
        original.setDescripcion(datos.getDescripcion());
        original.setTipo(datos.getTipo());
        original.setFechaHora(datos.getFechaHora());
        original.setIdLugar(datos.getIdLugar());
        original.setDemanda(datos.getDemanda());
        normalizarDemanda(original);
        return eventoRepository.save(original);
    }

    /**
     * Publicar un borrador solo cuando tiene los datos y sectores configurados
     */
    public EventoEntity publicarEvento(String id, String idOrganizador) {
        EventoEntity evento = buscarObligatorio(id);
        verificarOrganizador(evento, idOrganizador);
        exigirEstado(evento, "BORRADOR");
        validarDatos(evento);
        validarConfiguracionPublicacion(evento);
        evento.setEstado("PUBLICADO");
        return eventoRepository.save(evento);
    }

    // Un administrador puede detener temporalmente la venta
    public EventoEntity suspenderEvento(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        if (!"PUBLICADO".equals(evento.getEstado()) && !"AGOTADO".equals(evento.getEstado())) {
            throw new IllegalStateException("Solo se puede suspender un evento publicado o agotado");
        }
        evento.setEstado("SUSPENDIDO");
        return eventoRepository.save(evento);
    }

    // Solo un administrador reactiva un evento suspendido
    public EventoEntity reactivarEvento(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        exigirEstado(evento, "SUSPENDIDO");
        validarConfiguracionPublicacion(evento);
        evento.setEstado("PUBLICADO");
        return eventoRepository.save(evento);
    }

    /**
     * Cancelar un evento y procesar las devoluciones de todas sus entradas
     * Primero guardar CANCELADO para impedir nuevas compras
     * Si Ticket no responde, el evento queda cancelado y se puede reintentar
     */
    public EventoEntity cancelarEvento(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        if ("FINALIZADO".equals(evento.getEstado())) {
            throw new IllegalStateException("No se puede cancelar un evento finalizado");
        }
        // 1) Ticket espera que terminen las compras en curso y bloquea otras nuevas
        // Si Ticket no esta disponible NO se cambia el estado de Evento
        bloquearVentasAntesDeCancelar(id);
        if (!"CANCELADO".equals(evento.getEstado())) {
            evento.setEstado("CANCELADO");
            evento = eventoRepository.save(evento);
        }
        // 2) Procesar pendientes & Repetir esta llamada no duplica reintegros
        procesarReintegrosCancelacion(id, idAdministrador);
        return evento;
    }

    /**
     * Reintentar reintegros de un evento ya cancelado
     * Se usa si Ticket o Lugar fallaron durante la primera solicitud
     */
    public Map<String, Object> reintentarCancelacion(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        exigirEstado(evento, "CANCELADO");
        bloquearVentasAntesDeCancelar(id);
        return procesarReintegrosCancelacion(id, idAdministrador);
    }

    private void bloquearVentasAntesDeCancelar(String id) {
        String url = "http://localhost:8084/ticket/interno/eventos/" + id + "/bloquear-ventas";
        try {
            restTemplate.postForEntity(url, null, String.class);
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException("No se pudo bloquear la venta del evento en Ticket; no cancelar todavia", ex);
        }
    }

    private Map<String, Object> procesarReintegrosCancelacion(String id, String idAdministrador) {
        String url = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8084/ticket/interno/eventos/" + id + "/procesar-cancelacion")
                .queryParam("idAdministrador", idAdministrador)
                .toUriString();
        Map<String, Object> resultado;
        try {
            resultado = restTemplate.postForObject(url, null, Map.class);
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException("Evento CANCELADO, pero no se completaron los reintegros. "
                    + "Usar /evento/" + id + "/reintentar-cancelacion", ex);
        }
        if (resultado == null || !(resultado.get("pendientes") instanceof Number)) {
            throw new IllegalStateException("Evento CANCELADO, pero Ticket no devolvio el resumen de reintegros");
        }
        int pendientes = ((Number) resultado.get("pendientes")).intValue();
        if (pendientes > 0) {
            throw new IllegalStateException("Evento CANCELADO. Quedan " + pendientes
                    + " reintegros pendientes. Reintentar la cancelacion cuando se resuelva el error");
        }
        if (resultado.get("errores") instanceof List<?> errores && !errores.isEmpty()) {
            throw new IllegalStateException("Evento CANCELADO. Hubo " + errores.size()
                    + " errores al procesar devoluciones o avisos. Reintentar la cancelacion");
        }
        return resultado;
    }

    // Estado manual mientras no exista la sincronizacion automatica de disponibilidad
    public EventoEntity marcarAgotado(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        exigirEstado(evento, "PUBLICADO");
        evento.setEstado("AGOTADO");
        return eventoRepository.save(evento);
    }

    // Finalizar un evento que ya ocurrio
    public EventoEntity finalizarEvento(String id, String idAdministrador) {
        verificarAdministrador(idAdministrador);
        EventoEntity evento = buscarObligatorio(id);
        if (!"PUBLICADO".equals(evento.getEstado()) && !"AGOTADO".equals(evento.getEstado())) {
            throw new IllegalStateException("Solo se puede finalizar un evento publicado o agotado");
        }
        if (LocalDateTime.parse(evento.getFechaHora()).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("El evento todavia no comenzo");
        }
        evento.setEstado("FINALIZADO");
        return eventoRepository.save(evento);
    }

    public EventoEntity obtenerEvento(String id) {
        return eventoRepository.findById(id).orElse(null);
    }

    /**
     * Listar publicados y agotados para que sigan apareciendo en el catalogo
     * Los compradores solo pueden adquirir tickets de PUBLICADO
     */
    public List<EventoEntity> buscarEventosPublicados() {
        return eventoRepository.findByEstadoIn(List.of("PUBLICADO", "AGOTADO"));
    }

    public ParticipanteEntity crearParticipante(ParticipanteEntity participante) {
        if (participante == null || vacio(participante.getNombre())) {
            throw new IllegalArgumentException("El participante necesita un nombre");
        }
        participante.setId(null);
        participante.setNombre(participante.getNombre().trim());
        return participanteRepository.save(participante);
    }

    public List<ParticipanteEntity> listarParticipantes() {
        return participanteRepository.findAll();
    }

    /**
     * Asociar un participante existente con un evento borrador
     * Se evita duplicar la misma asociacion
     */
    public EventoParticipanteEntity agregarParticipante(String idEvento, String idParticipante,
                                                         String idOrganizador) {
        EventoEntity evento = buscarObligatorio(idEvento);
        verificarOrganizador(evento, idOrganizador);
        exigirEstado(evento, "BORRADOR");
        if (!participanteRepository.existsById(idParticipante)) {
            throw new NoSuchElementException("Participante no encontrado");
        }
        return eventoParticipanteRepository
                .findByIdEventoAndIdParticipante(idEvento, idParticipante)
                .orElseGet(() -> {
                    EventoParticipanteEntity relacion = new EventoParticipanteEntity();
                    relacion.setIdEvento(idEvento);
                    relacion.setIdParticipante(idParticipante);
                    return eventoParticipanteRepository.save(relacion);
                });
    }

    public void quitarParticipante(String idEvento, String idParticipante, String idOrganizador) {
        EventoEntity evento = buscarObligatorio(idEvento);
        verificarOrganizador(evento, idOrganizador);
        exigirEstado(evento, "BORRADOR");
        EventoParticipanteEntity relacion = eventoParticipanteRepository
                .findByIdEventoAndIdParticipante(idEvento, idParticipante)
                .orElseThrow(() -> new NoSuchElementException("El participante no esta asociado al evento"));
        eventoParticipanteRepository.delete(relacion);
    }

    public List<ParticipanteEntity> participantesDelEvento(String idEvento) {
        buscarObligatorio(idEvento);
        List<EventoParticipanteEntity> relaciones = eventoParticipanteRepository.findByIdEvento(idEvento);
        List<ParticipanteEntity> participantes = new ArrayList<>();
        for (EventoParticipanteEntity relacion : relaciones) {
            participanteRepository.findById(relacion.getIdParticipante()).ifPresent(participantes::add);
        }
        return participantes;
    }

    // Comprobar los requisitos antes de publicar o reactivar
    private void validarConfiguracionPublicacion(EventoEntity evento) {
        if (eventoParticipanteRepository.findByIdEvento(evento.getId()).isEmpty()) {
            throw new IllegalStateException("El evento necesita al menos un participante");
        }
        String url = UriComponentsBuilder
                .fromHttpUrl("http://localhost:8083/lugares/interno/eventos/" + evento.getId() + "/configuracion")
                .queryParam("idLugar", evento.getIdLugar())
                .toUriString();
        Map<String, Object> datos = restTemplate.getForObject(url, Map.class);
        if (datos == null || !Boolean.TRUE.equals(datos.get("listo"))) {
            String detalle = datos == null ? "Lugar no respondio" : String.valueOf(datos.get("errores"));
            throw new IllegalStateException("No se puede publicar: " + detalle);
        }
    }

    private EventoEntity buscarObligatorio(String id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evento no encontrado"));
    }

    private void verificarOrganizador(EventoEntity evento, String idOrganizador) {
        if (vacio(idOrganizador) || !evento.getOrganizador().equals(idOrganizador)) {
            throw new SecurityException("El organizador no corresponde a este evento");
        }
    }

    private void verificarAdministrador(String idAdministrador) {
        if (vacio(idAdministrador)) {
            throw new IllegalArgumentException("Administrador no valido");
        }
    }

    private void exigirEstado(EventoEntity evento, String estado) {
        if (!estado.equals(evento.getEstado())) {
            throw new IllegalStateException("El evento debe estar en estado " + estado);
        }
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void validarDatos(EventoEntity evento) {
        if (evento == null || vacio(evento.getNombre()) || vacio(evento.getDescripcion()) ||
                vacio(evento.getTipo()) || vacio(evento.getOrganizador()) ||
                vacio(evento.getFechaHora()) || evento.getIdLugar() == null || evento.getIdLugar() <= 0) {
            throw new IllegalArgumentException("Faltan datos obligatorios del evento");
        }
        try {
            LocalDateTime.parse(evento.getFechaHora());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("fechaHora debe tener formato yyyy-MM-ddTHH:mm:ss");
        }
    }

    private void normalizarDemanda(EventoEntity evento) {
        if (vacio(evento.getDemanda())) {
            evento.setDemanda("BAJA");
        } else {
            evento.setDemanda(evento.getDemanda().trim().toUpperCase());
            if (!"BAJA".equals(evento.getDemanda()) && !"ALTA".equals(evento.getDemanda())) {
                throw new IllegalArgumentException("Demanda debe ser BAJA o ALTA");
            }
        }
    }

    /**
     * 9) Buscar solo eventos publicos y combinar todos los criterios
     * Los participantes se filtran por sus relaciones reales, no por descripcion
     */
    public List<EventoBusquedaDTO> buscarEventos(BusquedaEventoFiltro filtro) {
        if (filtro == null) {
            filtro = new BusquedaEventoFiltro();
        }
        String orden = limpiar(filtro.getOrdenarPor());
        if (orden == null) orden = "fecha";
        orden = orden.toLowerCase(Locale.ROOT);
        if (!Set.of("fecha", "precio", "relevancia").contains(orden)) {
            throw new IllegalArgumentException("ordenarPor debe ser fecha, precio o relevancia");
        }
        if ((filtro.getLat() == null) != (filtro.getLon() == null)) {
            throw new IllegalArgumentException("Se deben indicar lat y lon juntos");
        }

        LocalDate desde = leerFecha(filtro.getFechaDesde(), "fechaDesde");
        LocalDate hasta = leerFecha(filtro.getFechaHasta(), "fechaHasta");
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("fechaHasta no puede ser anterior a fechaDesde");
        }
        String texto = limpiar(filtro.getTexto());
        if (texto == null) texto = limpiar(filtro.getQ());
        String tipo = limpiar(filtro.getTipo());
        String organizador = limpiar(filtro.getOrganizador());
        String participante = limpiar(filtro.getParticipante());

        List<String> idsPorParticipante = null;
        if (participante != null) {
            idsPorParticipante = idsEventosPorParticipante(participante);
            if (idsPorParticipante.isEmpty()) return List.of();
        }

        // Lugar conoce las ciudades y coordenadas, se mantiene la consulta REST
        Set<Long> idsLugares = null;
        String ciudad = limpiar(filtro.getCiudad());
        if (ciudad != null) {
            java.net.URI url = UriComponentsBuilder.fromHttpUrl("http://localhost:8083/lugares/buscarPorCiudad")
                    .queryParam("ciudad", ciudad).build().encode().toUri();
            Long[] ids = restTemplate.getForObject(url, Long[].class);
            idsLugares = new HashSet<>();
            if (ids != null) idsLugares.addAll(List.of(ids));
        }
        if (filtro.getLat() != null) {
            String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8083/lugares/buscarPorUbicacion")
                    .queryParam("lat", filtro.getLat())
                    .queryParam("lon", filtro.getLon()).toUriString();
            Long[] ids = restTemplate.getForObject(url, Long[].class);
            Set<Long> encontrados = new HashSet<>();
            if (ids != null) encontrados.addAll(List.of(ids));
            if (idsLugares == null) idsLugares = encontrados;
            else idsLugares.retainAll(encontrados);
        }
        if (idsLugares != null && idsLugares.isEmpty()) return List.of();

        List<String> idsPorTextoParticipante = texto == null ? List.of() : idsEventosPorParticipante(texto);
        final String textoFinal = texto;
        final Set<Long> lugaresFinal = idsLugares;
        final List<String> participantesFinal = idsPorParticipante;

        // Migracion de MongoTemplate/Criteria a JPA Specification (solo dios y el creado de java entendio esto 👌)
        Specification<EventoEntity> spec = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(root.get("estado").in("PUBLICADO", "AGOTADO"));
            if (tipo != null) condiciones.add(cb.equal(cb.lower(root.get("tipo")), tipo.toLowerCase(Locale.ROOT)));
            if (organizador != null) condiciones.add(cb.like(cb.lower(root.get("organizador")),
                    "%" + escaparLike(organizador.toLowerCase(Locale.ROOT)) + "%", '\\'));
            if (participantesFinal != null) condiciones.add(root.get("id").in(participantesFinal));
            if (desde != null) condiciones.add(cb.greaterThanOrEqualTo(root.<String>get("fechaHora"),
                    desde.atStartOfDay().toString()));
            if (hasta != null) condiciones.add(cb.lessThan(root.<String>get("fechaHora"),
                    hasta.plusDays(1).atStartOfDay().toString()));
            if (lugaresFinal != null) condiciones.add(root.get("idLugar").in(lugaresFinal));
            if (textoFinal != null) {
                String patron = "%" + escaparLike(textoFinal.toLowerCase(Locale.ROOT)) + "%";
                List<Predicate> alternativas = new ArrayList<>();
                alternativas.add(cb.like(cb.lower(root.get("nombre")), patron, '\\'));
                alternativas.add(cb.like(cb.lower(root.get("descripcion")), patron, '\\'));
                alternativas.add(cb.like(cb.lower(root.get("tipo")), patron, '\\'));
                alternativas.add(cb.like(cb.lower(root.get("organizador")), patron, '\\'));
                if (!idsPorTextoParticipante.isEmpty()) alternativas.add(root.get("id").in(idsPorTextoParticipante));
                condiciones.add(cb.or(alternativas.toArray(new Predicate[0])));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        List<EventoEntity> eventos = new ArrayList<>(eventoRepository.findAll(spec));
        if (eventos.isEmpty()) return List.of();

        List<String> idsEventos = eventos.stream().map(EventoEntity::getId).toList();
        Map<String, Double> precios = obtenerPreciosMinimos(idsEventos);
        Comparator<EventoEntity> porFecha = Comparator
                .comparing(EventoEntity::getFechaHora, Comparator.nullsLast(String::compareTo))
                .thenComparing(EventoEntity::getId);

        if ("precio".equals(orden)) {
            eventos.sort(Comparator.comparing((EventoEntity e) -> precios.get(e.getId()),
                    Comparator.nullsLast(Double::compareTo)).thenComparing(porFecha));
        } else if ("relevancia".equals(orden) && texto != null) {
            final String busqueda = texto;
            Map<String, List<String>> nombres = nombresParticipantesPorEvento(idsEventos);
            eventos.sort(Comparator.comparingInt(
                    (EventoEntity e) -> -puntajeRelevancia(e, busqueda, nombres.get(e.getId())))
                    .thenComparing(porFecha));
        } else {
            eventos.sort(porFecha);
        }
        List<EventoBusquedaDTO> resultado = new ArrayList<>();
        for (EventoEntity evento : eventos) {
            resultado.add(new EventoBusquedaDTO(evento, precios.get(evento.getId())));
        }
        return resultado;
    }

    private String escaparLike(String valor) {
        return valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private LocalDate leerFecha(String valor, String campo) {
        if (limpiar(valor) == null) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(campo + " debe tener formato yyyy-MM-dd");
        }
    }

    private List<String> idsEventosPorParticipante(String nombre) {
        List<ParticipanteEntity> participantes = participanteRepository.findByNombreContainingIgnoreCase(nombre);
        if (participantes.isEmpty()) {
            return List.of();
        }
        List<String> ids = participantes.stream().map(ParticipanteEntity::getId).toList();
        return eventoParticipanteRepository.findByIdParticipanteIn(ids).stream()
                .map(EventoParticipanteEntity::getIdEvento).distinct().toList();
    }

    private Map<String, Double> obtenerPreciosMinimos(List<String> idsEventos) {
        Map<?, ?> respuesta = restTemplate.postForObject(
                "http://localhost:8083/lugares/interno/eventos/precios-minimos", idsEventos, Map.class);
        if (respuesta == null) {
            throw new IllegalStateException("Lugar no devolvio los precios de los eventos");
        }
        Map<String, Double> precios = new HashMap<>();
        for (Map.Entry<?, ?> dato : respuesta.entrySet()) {
            if (dato.getKey() instanceof String && dato.getValue() instanceof Number) {
                precios.put((String) dato.getKey(), ((Number) dato.getValue()).doubleValue());
            }
        }
        return precios;
    }

    private Map<String, List<String>> nombresParticipantesPorEvento(List<String> idsEventos) {
        List<EventoParticipanteEntity> relaciones = eventoParticipanteRepository.findByIdEventoIn(idsEventos);
        Set<String> idsParticipantes = new LinkedHashSet<>();
        for (EventoParticipanteEntity relacion : relaciones) {
            idsParticipantes.add(relacion.getIdParticipante());
        }
        Map<String, String> nombrePorId = new HashMap<>();
        participanteRepository.findAllById(idsParticipantes)
                .forEach(p -> nombrePorId.put(p.getId(), p.getNombre()));
        Map<String, List<String>> nombresPorEvento = new HashMap<>();
        for (EventoParticipanteEntity relacion : relaciones) {
            String nombre = nombrePorId.get(relacion.getIdParticipante());
            if (nombre != null) {
                nombresPorEvento.computeIfAbsent(relacion.getIdEvento(), k -> new ArrayList<>()).add(nombre);
            }
        }
        return nombresPorEvento;
    }

    // Relevancia sencilla y determinista, pesa mas el titulo que la descripcion
    private int puntajeRelevancia(EventoEntity evento, String texto, List<String> participantes) {
        String buscado = texto.toLowerCase(Locale.ROOT);
        int puntos = 0;
        if (contiene(evento.getNombre(), buscado)) puntos += 5;
        if (contiene(evento.getOrganizador(), buscado)) puntos += 3;
        if (contiene(evento.getTipo(), buscado)) puntos += 2;
        if (contiene(evento.getDescripcion(), buscado)) puntos += 1;
        if (participantes != null) {
            for (String nombre : participantes) {
                if (contiene(nombre, buscado)) {
                    puntos += 4;
                    break;
                }
            }
        }
        return puntos;
    }

    private boolean contiene(String valor, String buscado) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(buscado);
    }
}
