package com.perfect8.email.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication filter for email-service
 * Version 1.1 - With shouldNotFilter for public endpoints
 * 
 * Supports two authentication methods:
 * 1. API Key (X-Api-Key header) - for service-to-service calls (shop-service)
 * 2. Gateway headers (X-User-Id, X-Auth-User, X-User-Roles) - for admin via Gateway
 */
@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Value("${api.key.email:${EMAIL_API_KEY:}}")
    private String emailApiKey;

    @Value("${api.key.shop:${SHOP_API_KEY:}}")
    private String shopApiKey;

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";

    /**
     * Skip filter for public endpoints
     * These endpoints don't require authentication
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/api/email/test");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Method 1: Check API Key (service-to-service)
        String apiKey = request.getHeader(API_KEY_HEADER);
        String serviceName = request.getHeader(SERVICE_NAME_HEADER);

        if (apiKey != null && !apiKey.isEmpty()) {
            if (isValidApiKey(apiKey, serviceName)) {
                log.debug("API Key authentication successful for service: {}", serviceName);
                
                // Create authentication with SERVICE role
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_SERVICE"),
                        new SimpleGrantedAuthority("ROLE_INTERNAL")
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                serviceName != null ? serviceName : "internal-service",
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
                return;
            } else {
                log.warn("Invalid API Key received from service: {}", serviceName);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid API Key\"}");
                return;
            }
        }

        // Method 2: Check Gateway headers (admin via Gateway)
        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-Auth-User");
        String rolesHeader = request.getHeader("X-User-Roles");

        if (userId != null && userEmail != null && rolesHeader != null) {
            log.debug("Gateway authentication for user: {}", userEmail);
            
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(role -> {
                        // Ensure ROLE_ prefix
                        if (!role.startsWith("ROLE_")) {
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        }
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validate API key against known service keys
     */
    private boolean isValidApiKey(String apiKey, String serviceName) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        // Accept email-service's own key (for testing)
        if (emailApiKey != null && !emailApiKey.isEmpty() && apiKey.equals(emailApiKey)) {
            return true;
        }

        // Accept shop-service key
        if (shopApiKey != null && !shopApiKey.isEmpty() && apiKey.equals(shopApiKey)) {
            return true;
        }

        return false;
    }
}
