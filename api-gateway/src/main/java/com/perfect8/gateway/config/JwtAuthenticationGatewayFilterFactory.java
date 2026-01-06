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
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {}

    // Magnum Opus: Komplett lista för att tillåta hälsa, auth och swagger-resurser
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/",           // Inloggning/Register
            "/actuator/health", // Docker Healthcheck
            "/v3/api-docs",     // Swagger JSON docs
            "/swagger-ui",      // Swagger HTML
            "/webjars/"         // Swagger JS/CSS (LÖSER 500-FELET)
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // 1. Släpp igenom publika endpoints
            if (isPublic(path)) {
                return chain.filter(exchange);
            }

            // 2. Validera JWT för resten
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isTokenExpired(token)) {
                    return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtil.extractAllClaims(token);

                // Extrahera data för mikrotjänsterna
                String username = claims.getSubject();
                String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";

                // Berika requesten med headers
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT Error: {}", e.getMessage());
                return onError(exchange, "Invalid JWT", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::contains);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}