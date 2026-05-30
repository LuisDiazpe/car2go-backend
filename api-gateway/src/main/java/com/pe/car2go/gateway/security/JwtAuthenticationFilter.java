package com.pe.car2go.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Filtro global del Gateway que valida el JWT en el punto de entrada.
 *
 * Estrategia de seguridad distribuida:
 *  - El Gateway valida el token UNA sola vez.
 *  - Extrae userId y role del token.
 *  - Los inyecta como headers (X-User-Id, X-User-Role) hacia los microservicios.
 *  - Los microservicios confian en esos headers (solo el Gateway puede alcanzarlos).
 *
 * Rutas publicas (sign-up, sign-in, catalogo, swagger) no requieren token.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;

    // Rutas que NO requieren autenticacion
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/",
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs"
    );

    public JwtAuthenticationFilter() {
        // El mismo secret que usa ms-iam para firmar (compartido via env var)
        String secret = System.getenv().getOrDefault("JWT_SECRET",
                "ChangeThisSecretInProductionWithAtLeast512BitsForHS512AlgorithmABCDEF1234567890");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Rutas publicas: GET de vehiculos tambien es publico
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::contains)
                || (path.startsWith("/api/v1/vehicles") && request.getMethod().name().equals("GET"));

        if (isPublic) {
            return chain.filter(exchange);
        }

        // Extraer token del header Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Inyectar identidad en headers hacia los microservicios
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Name", claims.getSubject())
                    .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = "{\"error\":\"Token invalido o ausente\"}".getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Se ejecuta antes que el enrutamiento
    }
}
