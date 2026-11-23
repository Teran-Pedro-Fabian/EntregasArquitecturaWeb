package com.microserviciolugar.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component("internoAuth")
public class InternoAuth {
    @Value("${ticketya.internal.key}") private String claveInterna;

    public boolean esInterna() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servlet)) return false;
        String recibida = servlet.getRequest().getHeader("X-TicketYa-Internal");
        return claveInterna != null && !claveInterna.isBlank() && recibida != null
                && MessageDigest.isEqual(claveInterna.getBytes(StandardCharsets.UTF_8),
                                         recibida.getBytes(StandardCharsets.UTF_8));
    }
}
