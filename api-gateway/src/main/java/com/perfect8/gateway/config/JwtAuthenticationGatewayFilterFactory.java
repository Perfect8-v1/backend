package com.perfect8.gateway.config;

import com.perfect8.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtret registreras automatiskt som "JwtAuthentication" i Spring Cloud Gateway.
 */
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {}

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars/"
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            log.debug("Processing request: {}", path);

            if (isPublic(path)) {
                log.debug("Public endpoint, skipping JWT validation: {}", path);
                return chain.filter(exchange);
            }

            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for: {}", path);
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for: {}", path);
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("Expired token for: {}", path);
                    return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtil.extractAllClaims(token);
                String username = claims.getSubject();
                String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";

                // Extrahera roller från claims
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                String rolesHeader = roles != null ? String.join(",", roles) : "";

                log.info("JWT validated - user: {}, userId: {}, roles: {}", username, userId, rolesHeader);

                // Skicka vidare användarinformation till mikrotjänsterna
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-User-Id", userId)
                        .header("X-Auth-Roles", rolesHeader)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("JWT validation error for {}: {}", path, e.getMessage());
                return onError(exchange, "Invalid JWT", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::contains);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        log.debug("Returning {} - {}", status, err);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}