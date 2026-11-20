package com.microserviciocliente.servise;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Service
public class LoginService {
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Value("${ticketya.auth.admin.id:}") private String idAdmin;
    @Value("${ticketya.auth.admin.password:}") private String claveAdmin;
    @Value("${ticketya.auth.organizador.id:}") private String idOrganizador;
    @Value("${ticketya.auth.organizador.password:}") private String claveOrganizador;
    @Value("${ticketya.auth.acceso.id:}") private String idAcceso;
    @Value("${ticketya.auth.acceso.password:}") private String claveAcceso;

    public LoginService(ClienteRepository clientes, PasswordEncoder encoder, JwtService jwt) {
        this.clientes = clientes;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // El rol se decide en el servidor, no lo puede elegir el cliente
    public Map<String, String> ingresar(String id, String password) {
        if (id == null || id.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }
        String rol = null;
        if (coincide(id, password, idAdmin, claveAdmin)) rol = "ADMINISTRADOR";
        else if (coincide(id, password, idOrganizador, claveOrganizador)) rol = "ORGANIZADOR";
        else if (coincide(id, password, idAcceso, claveAcceso)) rol = "PERSONAL_ACCESO";
        else {
            ClienteEntity cliente = clientes.findById(id).orElse(null);
            if (cliente != null && cliente.getPasswordHash() != null
                    && encoder.matches(password, cliente.getPasswordHash())) rol = "CLIENTE";
        }
        if (rol == null) throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        return Map.of("token", jwt.generarToken(id, rol), "idUsuario", id, "rol", rol);
    }

    private boolean coincide(String id, String password, String configurado, String clave) {
        if (configurado == null || configurado.isBlank() || clave == null || clave.isBlank()) return false;
        return MessageDigest.isEqual(id.getBytes(StandardCharsets.UTF_8), configurado.getBytes(StandardCharsets.UTF_8))
                && MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), clave.getBytes(StandardCharsets.UTF_8));
    }
}
