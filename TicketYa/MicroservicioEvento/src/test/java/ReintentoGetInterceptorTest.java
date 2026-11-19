import com.microServicioEvento.config.ReintentoGetInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReintentoGetInterceptorTest {
    private final HttpRequest request = mock(HttpRequest.class);
    private final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
    private final byte[] body = new byte[0];

    @Test
    void getReintenta503YRecupera() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        ClientHttpResponse error = mock(ClientHttpResponse.class);
        ClientHttpResponse exito = mock(ClientHttpResponse.class);
        when(error.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(exito.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(request, body)).thenReturn(error, exito);

        ClientHttpResponse resultado = new ReintentoGetInterceptor(1, 0)
                .intercept(request, body, execution);
        assertSame(exito, resultado);
        verify(error).close();
        verify(execution, times(2)).execute(request, body);
    }

    @Test
    void getReintentaCuandoLaConexionSeInterrumpe() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        ClientHttpResponse exito = mock(ClientHttpResponse.class);
        when(exito.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(request, body))
                .thenThrow(new IOException("conexion cerrada"))
                .thenReturn(exito);

        assertSame(exito, new ReintentoGetInterceptor(1, 0).intercept(request, body, execution));
        verify(execution, times(2)).execute(request, body);
    }

    @Test
    void getNoReintentaErroresDelCliente() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        ClientHttpResponse rechazo = mock(ClientHttpResponse.class);
        when(rechazo.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(execution.execute(request, body)).thenReturn(rechazo);

        assertSame(rechazo, new ReintentoGetInterceptor(1, 0).intercept(request, body, execution));
        verify(execution, times(1)).execute(request, body);
        verify(rechazo, never()).close();
    }

    @Test
    void postDeConfirmacionNoSeReintenta() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(execution.execute(request, body)).thenThrow(new IOException("respuesta perdida"));

        assertThrows(IOException.class, () -> new ReintentoGetInterceptor(2, 0)
                .intercept(request, body, execution));
        verify(execution, times(1)).execute(request, body);
    }

    @Test
    void deleteDeCompensacionNoSeReintenta() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.DELETE);
        when(execution.execute(request, body)).thenThrow(new IOException("respuesta perdida"));

        assertThrows(IOException.class, () -> new ReintentoGetInterceptor(2, 0)
                .intercept(request, body, execution));
        verify(execution, times(1)).execute(request, body);
    }

    @Test
    void getDetieneReintentosAlAgotarLimite() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        ClientHttpResponse error1 = mock(ClientHttpResponse.class);
        ClientHttpResponse error2 = mock(ClientHttpResponse.class);
        when(error1.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);
        when(error2.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(execution.execute(request, body)).thenReturn(error1, error2);

        assertSame(error2, new ReintentoGetInterceptor(1, 0).intercept(request, body, execution));
        verify(error1).close();
        verify(error2, never()).close();
        verify(execution, times(2)).execute(request, body);
    }

    @Test
    void rechazaConfiguracionSinLimites() {
        assertThrows(IllegalArgumentException.class, () -> new ReintentoGetInterceptor(4, 0));
        assertThrows(IllegalArgumentException.class, () -> new ReintentoGetInterceptor(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ReintentoGetInterceptor(1, 6000));
    }
}
