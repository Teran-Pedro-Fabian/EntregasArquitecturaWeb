package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CorreoNotificacionService correoNotificacionService;

    /**
     * Guarda un cliente en la base de datos
     *
     * @param clienteEntity cliente que queremos guardar
     * @return cliente guardado
     */
    public ClienteEntity save(ClienteEntity clienteEntity) {
        ClienteEntity guardado = clienteRepository.save(clienteEntity);
        if (guardado.getEmail() != null && !guardado.getEmail().isBlank()) {
            correoNotificacionService.reactivarSinEmail(guardado.getId());
        }
        return guardado;
    }

    public ClienteEntity actualizarEmail(String id, String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length() > 254) {
            throw new IllegalArgumentException("Email no valido");
        }
        ClienteEntity cliente = clienteRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Cliente no encontrado"));
        cliente.setEmail(email.trim());
        ClienteEntity guardado = clienteRepository.save(cliente);
        correoNotificacionService.reactivarSinEmail(id);
        return guardado;
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
}