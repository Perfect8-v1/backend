package com.perfect8.shop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Shop Service
 * 
 * This configuration expects requests to come through API Gateway.
 * Gateway validates JWT and sets headers (X-Auth-User, X-Auth-Roles, X-User-Id).
 * HeaderAuthenticationFilter reads these headers and creates Principal.
 * 
 * Public endpoints (no authentication required):
 * - GET /api/products/** (browse products)
 * - GET /api/categories/** (browse categories)
 * - Health checks
 * 
 * Protected endpoints (authentication required):
 * - POST /api/products/** (admin only)
 * - /api/cart/** (user's cart)
 * - /api/orders/** (user's orders)
 * - /api/customers/** (user profile)
 * 
 * @author Perfect8 Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless REST API)
            .csrf(csrf -> csrf.disable())
            
            // Stateless session (no HttpSession)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                
                // Health checks (for monitoring)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Public product browsing (GET only)
                .requestMatchers("GET", "/api/products/**").permitAll()
                .requestMatchers("GET", "/api/categories/**").permitAll()
                
                // Swagger UI (optional - remove in production)
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Protected endpoints - authentication required
                
                // Product management (admin only - use @PreAuthorize in controller)
                .requestMatchers("POST", "/api/products/**").authenticated()
                .requestMatchers("PUT", "/api/products/**").authenticated()
                .requestMatchers("DELETE", "/api/products/**").authenticated()
                
                // Cart operations (requires authentication)
                .requestMatchers("/api/cart/**").authenticated()
                
                // Order operations (requires authentication)
                .requestMatchers("/api/orders/**").authenticated()
                
                // Customer profile (requires authentication)
                .requestMatchers("/api/customers/**").authenticated()
                
                // Payment operations (requires authentication)
                .requestMatchers("/api/payments/**").authenticated()
                
                // Default: all other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Add HeaderAuthenticationFilter before UsernamePasswordAuthenticationFilter
            // This filter reads X-Auth-User header from Gateway and creates Principal
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
