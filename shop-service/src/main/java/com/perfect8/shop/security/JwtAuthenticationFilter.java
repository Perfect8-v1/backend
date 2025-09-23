package com.perfect8.shop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for Shop Service
 * Version 1.0 - Core authentication filtering only
 * Validates JWT tokens and sets up Spring Security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from request
            String jwtToken = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateToken(jwtToken)) {
                // Get user email from token
                String userEmail = jwtTokenProvider.getUsernameFromToken(jwtToken);

                // Get authorities from token
                String authoritiesString = jwtTokenProvider.getAuthoritiesFromToken(jwtToken);
                List<SimpleGrantedAuthority> authorities = parseAuthorities(authoritiesString);

                // Get customer ID if present - FIXED: Changed from String to Long
                Long customerIdLong = jwtTokenProvider.getCustomerIdFromToken(jwtToken);

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

                // Add additional details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store customer ID in authentication details if present - FIXED: Now stores Long
                if (customerIdLong != null) {
                    request.setAttribute("customerId", customerIdLong);
                }

                // Set authentication in Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Successfully authenticated user: {} for request: {}",
                        userEmail, request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            // Clear the context to ensure no partial authentication
            SecurityContextHolder.clearContext();
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwtToken = bearerToken.substring(7);
            log.debug("Extracted JWT token from Authorization header");
            return jwtToken;
        }

        // Check for token in query parameters (for WebSocket connections)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            log.debug("Extracted JWT token from query parameter");
            return tokenParam;
        }

        return null;
    }

    /**
     * Parse authorities string into list of SimpleGrantedAuthority
     */
    private List<SimpleGrantedAuthority> parseAuthorities(String authoritiesString) {
        if (!StringUtils.hasText(authoritiesString)) {
            return Collections.emptyList();
        }

        return Arrays.stream(authoritiesString.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Check if this filter should not run for the request
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Skip filter for public endpoints
        return path.startsWith("/api/public/") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/products/public") ||
                path.startsWith("/api/categories/public") ||
                path.equals("/api/health") ||
                path.equals("/actuator/health");
    }

    /**
     * Get customer ID from current request
     * Utility method for controllers to retrieve customer ID
     */
    public static Long getCurrentCustomerId(HttpServletRequest request) {
        Object customerIdObj = request.getAttribute("customerId");
        if (customerIdObj != null) {
            if (customerIdObj instanceof Long) {
                return (Long) customerIdObj;
            } else if (customerIdObj instanceof String) {
                try {
                    return Long.parseLong((String) customerIdObj);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Get current authenticated user email
     * Utility method for controllers
     */
    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    // Version 2.0 - Commented out for future implementation
    /*
    private void trackAuthenticationMetrics(String userEmail, boolean success) {
        // Authentication metrics tracking
        // To be implemented in version 2.0
    }

    private void analyzeTokenUsage(String token) {
        // Token usage analytics
        // To be implemented in version 2.0
    }

    private void performanceMonitoring(long startTime, long endTime) {
        // Performance monitoring
        // To be implemented in version 2.0
    }
    */
}