package com.perfect8.shop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Header Authentication Filter for Shop Service
 * 
 * This filter reads authentication info from headers set by API Gateway
 * and creates a Spring Security Principal (Authentication object).
 * 
 * Flow:
 * 1. API Gateway validates JWT token
 * 2. Gateway adds headers: X-Auth-User, X-Auth-Roles, X-User-Id
 * 3. This filter reads headers
 * 4. Creates UsernamePasswordAuthenticationToken
 * 5. Sets in SecurityContext
 * 6. Controllers can now use Principal parameter
 * 
 * Example usage in controller:
 * <pre>
 * @GetMapping("/cart")
 * public ResponseEntity<?> getCart(Principal principal) {
 *     String email = principal.getName();  // From X-Auth-User header
 *     // Use email to look up customer...
 * }
 * </pre>
 * 
 * @author Perfect8 Team
 * @version 1.0.0
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_ROLES = "X-Auth-Roles";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Extract auth headers set by API Gateway
        String username = request.getHeader(HEADER_AUTH_USER);
        String rolesHeader = request.getHeader(HEADER_AUTH_ROLES);
        String userId = request.getHeader(HEADER_USER_ID);

        logger.debug("HeaderAuthenticationFilter - Path: {}, User: {}, Roles: {}, UserId: {}", 
                request.getRequestURI(), username, rolesHeader, userId);

        // If no auth headers, skip (public endpoint or direct access)
        if (username == null || username.isEmpty()) {
            logger.debug("No X-Auth-User header found, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Parse roles from comma-separated string
            List<SimpleGrantedAuthority> authorities;
            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                authorities = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                // Default role if none provided
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // Create authentication token
            // Principal name = username (email from JWT)
            // Credentials = null (already authenticated by Gateway)
            // Authorities = roles from JWT
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,    // Principal.getName() will return this
                            null,        // No credentials needed (already authenticated)
                            authorities  // Roles
                    );

            // Set in SecurityContext so controllers can access Principal
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Authentication set in SecurityContext for user: {} with roles: {}", 
                    username, authorities);

        } catch (Exception e) {
            logger.error("Error creating authentication from headers", e);
            // Continue without authentication (will fail at controller if @PreAuthorize used)
        } finally {
            // Clear context after request to prevent authentication leak
            try {
                filterChain.doFilter(request, response);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
