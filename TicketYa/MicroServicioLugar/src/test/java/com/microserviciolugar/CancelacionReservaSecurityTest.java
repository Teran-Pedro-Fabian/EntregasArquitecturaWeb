package com.microserviciolugar;

import com.microserviciolugar.config.SecurityConfig;
import com.microserviciolugar.controller.ReservaGeneralController;
import com.microserviciolugar.security.InternoAuth;
import com.microserviciolugar.service.ReservaGeneralService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaGeneralController.class)
@Import({SecurityConfig.class, InternoAuth.class})
@TestPropertySource(properties = {
        "ticketya.jwt.secret=clave-jwt-para-pruebas-que-supera-32-bytes-2026",
        "ticketya.internal.key=clave-interna-prueba-2026"
})
class CancelacionReservaSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ReservaGeneralService reservaGeneralService;

    @Test
    void ticketPuedeCancelarReservaConClaveInternaSinJwt() throws Exception {
        mockMvc.perform(delete("/lugares/sectores/generales/reservas/13")
                        .param("idSector", "11")
                        .param("idCliente", "cliente-test")
                        .header("X-TicketYa-Internal", "clave-interna-prueba-2026"))
                .andExpect(status().isOk());

        verify(reservaGeneralService).cancelar(11L, 13L, "cliente-test");
    }

    @Test
    void noPermiteCancelarReservaSinAutenticacion() throws Exception {
        mockMvc.perform(delete("/lugares/sectores/generales/reservas/13")
                        .param("idSector", "11")
                        .param("idCliente", "cliente-test"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservaGeneralService);
    }

    @Test
    void noPermiteCancelarReservaConClaveIncorrecta() throws Exception {
        mockMvc.perform(delete("/lugares/sectores/generales/reservas/13")
                        .param("idSector", "11")
                        .param("idCliente", "cliente-test")
                        .header("X-TicketYa-Internal", "clave-incorrecta"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservaGeneralService);
    }
}
