package com.microserviciocliente.config;

import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.entity.PreferenciasEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.repository.PreferenciasRepository;
import com.microserviciocliente.servise.ClienteService;
import com.microserviciocliente.servise.NotificacionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Datos de demostracion para una instalacion nueva.
 * Se ejecuta solo cuando TICKETYA_DEMO_SEED=true.
 */
@Component
@ConditionalOnProperty(name = "ticketya.demo.seed", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    public static final String CLIENTE_DEMO_ID = "cliente-demo";

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final PreferenciasRepository preferenciasRepository;
    private final NotificacionService notificacionService;

    public DemoDataInitializer(ClienteRepository clienteRepository,
                               ClienteService clienteService,
                               PreferenciasRepository preferenciasRepository,
                               NotificacionService notificacionService) {
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.preferenciasRepository = preferenciasRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearClienteDemo();
        crearPreferenciasDemo();
        crearNotificacionesDemo();
    }

    private void crearClienteDemo() {
        if (clienteRepository.existsById(CLIENTE_DEMO_ID)) return;

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(CLIENTE_DEMO_ID);
        cliente.setNombre("Manuel Demo");
        cliente.setEmail("cliente.demo@ticketya.local");
        cliente.setPassword("ClienteDemo2026!");
        clienteService.save(cliente);
    }

    private void crearPreferenciasDemo() {
        guardarPreferencia("pref-demo-rata-blanca", "evento-rata-blanca",
                "participante-rata-blanca", "1001", "Tandil", "RECITAL");
        guardarPreferencia("pref-demo-festival", null,
                null, null, "Tandil", "FESTIVAL");
        guardarPreferencia("pref-demo-sierra", "evento-festival-sierra",
                "participante-yamila-cafrune", "1004", "Tandil", "FESTIVAL");
    }

    private void crearNotificacionesDemo() {
        notificacionService.registrar("COMPRA", "compra-8001", "evento-rata-blanca", CLIENTE_DEMO_ID,
                "Compra confirmada: Rata Blanca en Tandil - Platea Baja, Fila B, Asiento 4. Importe: $45.000.");
        notificacionService.registrar("COMPRA", "compra-8002", "evento-santamarina-mitre", CLIENTE_DEMO_ID,
                "Compra confirmada: Santamarina vs Mitre de Santiago del Estero - Popular Local. Importe: $18.000.");
        notificacionService.registrar("ARTISTA", "evento-demo-rata", "evento-rata-blanca", CLIENTE_DEMO_ID,
                "Rata Blanca, uno de tus artistas favoritos, tiene un nuevo evento en Tandil.");
    }

    private void guardarPreferencia(String id, String idEvento, String idParticipante,
                                    String idLugar, String ciudad, String tipo) {
        if (preferenciasRepository.existsById(id)) return;

        PreferenciasEntity preferencia = new PreferenciasEntity();
        preferencia.setId(id);
        preferencia.setIdCliente(CLIENTE_DEMO_ID);
        preferencia.setIdEvento(idEvento);
        preferencia.setIdParticipante(idParticipante);
        preferencia.setIdLugar(idLugar);
        preferencia.setCiudad(ciudad);
        preferencia.setTipo(tipo);
        preferenciasRepository.save(preferencia);
    }
}
