package com.perfect8.gateway.filter;

import com.perfect8.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {}

    // Lista på publika endpoints som inte kräver JWT
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/salt",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui"
    );

    private final Predicate<ServerHttpRequest> isPublic = request ->
            PUBLIC_ENDPOINTS.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // LOGGA ALLA INKOMMANDE ANROP (Isolera IPv4/IPv6 problem)
            log.info("DEBUG: Inkommande anrop till Gateway - Path: {}, Method: {}, Remote: {}", 
                    path, request.getMethod(), request.getRemoteAddress());

            if (isPublic.test(request)) {
                log.info("DEBUG: Public endpoint identifierad, släpper igenom: {}", path);
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("DEBUG: Saknar Authorization header för skyddad endpoint: {}", path);
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("DEBUG: Felaktigt format på Authorization header för: {}", path);
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.error("DEBUG: JWT Token har löpt ut för: {}", path);
                    return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtil.extractAllClaims(token);
                String username = claims.getSubject();
                String roles = claims.get("roles", String.class);
                String userId = String.valueOf(claims.get("userId"));

                log.info("DEBUG: JWT validerad för user: {}, roles: {}, userId: {}", username, roles, userId);

                // Sätt headers för downstream services (FAS 2)
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Roles", roles)
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("DEBUG: JWT validering misslyckades: {}", e.getMessage());
                return onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("DEBUG: Returnerar fel till klient: {} - Status: {}", err, httpStatus);
        return response.setComplete();
    }
}