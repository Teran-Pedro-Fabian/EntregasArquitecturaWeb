package com.ticketya.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// 1. Anotación @Component obligatoria para que Spring lo registre
@Component
// 2. Extender AbstractGatewayFilterFactory simplifica la configuración
public class JwtPropagationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final ReactiveJwtDecoder jwtDecoder;

    // 3. Inyectar ReactiveJwtDecoder (el bean reactivo que crea Spring Security)
    public JwtPropagationGatewayFilterFactory(ReactiveJwtDecoder jwtDecoder) {
        super(Object.class);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new JwtPropagationFilter();
    }

    private class JwtPropagationFilter implements GatewayFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring("Bearer ".length()).trim();

                // 4. Flujo reactivo no bloqueante con flatMap
                return jwtDecoder.decode(token)
                        .flatMap(jwt -> {
                            // 5. Extracción segura de roles (soporta lista o texto)
                            Object rolesClaim = jwt.getClaim("roles");
                            String rolesHeader = "";
                            if (rolesClaim instanceof List<?> list) {
                                rolesHeader = String.join(",", list.stream().map(String::valueOf).toList());
                            } else if (rolesClaim != null) {
                                rolesHeader = rolesClaim.toString();
                            }

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", jwt.getSubject())
                                    .header("X-User-Roles", rolesHeader)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        })
                        // Si falla la decodificación, continúa sin agregar headers
                        .onErrorResume(e -> chain.filter(exchange));
            }

            return chain.filter(exchange);
        }

        @Override
        public int getOrder() {
            // Prioridad alta, pero respetando los filtros internos críticos del Gateway
            return -1;
        }
    }
}
