package com.perfect8.imageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Image Service
 * 
 * IMPORTANT: This service now trusts Spring Cloud Gateway for authentication.
 * Gateway validates JWT tokens and adds user info to request headers:
 * - X-Auth-User: username
 * - X-Auth-Role: ROLE_ADMIN / ROLE_USER
 * 
 * This service does NOT validate JWT tokens - Gateway handles that.
 * All requests reaching this service have already been authenticated by Gateway.
 * 
 * @author Perfect8 Team
 * @version 2.0.0 (FAS 3 - Gateway Integration)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security Filter Chain - Trust Gateway
     * 
     * Gateway handles:
     * - JWT validation
     * - Public/protected endpoint rules
     * - Authentication
     * 
     * This service:
     * - Trusts Gateway headers (X-Auth-User, X-Auth-Role)
     * - Allows all requests (Gateway has already authenticated)
     * - Maintains stateless session policy
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF - stateless API with JWT (validated by Gateway)
            .csrf(csrf -> csrf.disable())
            
            // Authorization rules - trust Gateway
            .authorizeHttpRequests(auth -> auth
                // Allow all requests - Gateway has already authenticated
                .anyRequest().permitAll()
            )
            
            // Session management - stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .build();
    }
}
