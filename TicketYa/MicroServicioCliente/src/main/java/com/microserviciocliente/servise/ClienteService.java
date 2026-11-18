package com.microserviciocliente.servise;

import com.microserviciocliente.client.ApiService;
import com.microserviciocliente.dto.BusquedaEventoFiltro;
import com.microserviciocliente.dto.EventoEntityDTO;
import com.microserviciocliente.dto.LugarEntityDTO;
import com.microserviciocliente.dto.ParticipanteEntityDTO;
import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.ClienteRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collections;

import java.util.List;

@Service
public class ClienteService {



    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PreferenciasService preferenciasService;
    @Autowired 
    private ApiService apiService;

    /**
     * Guarda un cliente en la base de datos
     *
     * @param clienteEntity cliente que queremos guardar
     * @return cliente guardado
     */
    public ClienteEntity save(ClienteEntity clienteEntity) {
        return clienteRepository.save(clienteEntity);
    }

    /**
     * Busca un cliente por su ID
     *
     * @param idCliente ID del cliente
     * @return cliente encontrado o null si no existe
     */
    public ClienteEntity findById(String idCliente) {
        return clienteRepository.findById(idCliente).orElse(null);
    }



    public List<EventoEntityDTO> obtenerRecomendaciones(String idCliente) {
        ClienteEntity cliente = findById(idCliente);
        if (cliente == null) {
            return Collections.emptyList(); // Retorna una lista vacía si el cliente no existe
        }

        List<PreferenciasEntity> preferencias = preferenciasService.findByidCliente(idCliente);
        if (preferencias.isEmpty()) {
            return Collections.emptyList(); // Retorna una lista vacía si no hay preferencias
        }



        List<BusquedaEventoFiltro> filtros = new ArrayList<>();
        
        
        for (PreferenciasEntity pref : preferencias) {
            BusquedaEventoFiltro filtro = new BusquedaEventoFiltro();
            // Configurar el filtro según la preferencia
            EventoEntityDTO evento = apiService.obtenerEventoPorId(pref.getIdEvento());
            LugarEntityDTO lugar = apiService.obtenerLugarPorId(pref.getIdLugar());
            ParticipanteEntityDTO participante = apiService.obtenerParticipantePorId(pref.getIdParticipante());
            if (evento != null) {
                filtro.setTipo(evento.getTipo());
                filtro.setOrganizador(evento.getOrganizador());
                filtro.setFechaDesde(evento.getFechaHora());
                filtro.setFechaHasta(evento.getFechaHora());
                filtro.setCiudad(lugar.getCiudad());
                filtro.setLat(lugar.getLatitud());
                filtro.setLon(lugar.getLongitud());
                filtro.setParticipante(participante.getNombre());

                filtros.add(filtro);
            }
        }

        List<EventoEntityDTO> recomendaciones = new ArrayList<>();

        for (BusquedaEventoFiltro filtro : filtros) {
            
            List<EventoEntityDTO> eventos = apiService.buscarEventos(filtro);
            for (EventoEntityDTO evento : eventos) {
                if (!recomendaciones.contains(evento)) {
                    recomendaciones.add(evento);
                }
            }
        }
        return recomendaciones;
    }


    
}