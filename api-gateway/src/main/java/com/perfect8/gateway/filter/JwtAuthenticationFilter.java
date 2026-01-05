package com.perfect8.gateway.filter;

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

    // Publika endpoints som inte kräver JWT
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/salt",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/api/products",      // Publika produkter
            "/api/categories",    // Publika kategorier
            "/api/posts"          // Publika bloggposter
    );

    private final Predicate<ServerHttpRequest> isPublic = request -> {
        String path = request.getURI().getPath();
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::contains);
    };

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("JWT Filter: {} {}", request.getMethod(), path);

            // Publika endpoints - släpp igenom utan validering
            if (isPublic.test(request)) {
                log.debug("Public endpoint, skipping JWT validation: {}", path);
                return chain.filter(exchange);
            }

            // Kolla Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for: {}", path);
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for: {}", path);
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("Token expired for: {}", path);
                    return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtil.extractAllClaims(token);
                String username = claims.getSubject();
                String userId = String.valueOf(claims.get("userId"));

                // Hantera roles som kan vara antingen List eller String
                String roles = extractRoles(claims);

                log.info("JWT validated - user: {}, userId: {}, roles: {}", username, userId, roles);

                // Lägg till headers för downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Roles", roles)
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Extrahera roles från claims - hanterar både List och String format
     */
    @SuppressWarnings("unchecked")
    private String extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            return "";
        }
        if (rolesObj instanceof List) {
            List<String> rolesList = (List<String>) rolesObj;
            return String.join(",", rolesList);
        }
        return rolesObj.toString();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Returning error: {} - Status: {}", err, httpStatus);
        return response.setComplete();
    }
}