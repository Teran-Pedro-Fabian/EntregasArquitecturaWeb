package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CorreoNotificacionService correoNotificacionService;

    /**
     * Guarda un cliente en la base de datos
     *
     * @param clienteEntity cliente que queremos guardar
     * @return cliente guardado
     */
    public ClienteEntity save(ClienteEntity clienteEntity) {
        // Las cuentas anteriores siguen existiendo, pero solo ingresan al login si tienen clave
        if (clienteEntity.getPassword() != null && !clienteEntity.getPassword().isBlank()) {
            if (clienteEntity.getPassword().length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            clienteEntity.setPasswordHash(passwordEncoder.encode(clienteEntity.getPassword()));
            clienteEntity.setPassword(null);
        }
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