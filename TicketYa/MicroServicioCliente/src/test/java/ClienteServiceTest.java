import com.microserviciocliente.entity.ClienteEntity;
import com.microserviciocliente.repository.ClienteRepository;
import com.microserviciocliente.servise.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private ClienteService clienteService;

    @Test
    void buscarClienteExistente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId("cliente-1");
        cliente.setNombre("Prueba");
        when(clienteRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        assertEquals("Prueba", clienteService.findById("cliente-1").getNombre());
    }

    @Test
    void buscarClienteInexistente() {
        when(clienteRepository.findById("ausente")).thenReturn(Optional.empty());
        assertNull(clienteService.findById("ausente"));
    }
}
