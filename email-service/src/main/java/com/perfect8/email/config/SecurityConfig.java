package com.perfect8.email.config;

import com.perfect8.email.security.HeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for email-service
 * 
 * Authentication methods:
 * 1. API Key (X-Api-Key) - for internal service-to-service calls
 * 2. Gateway headers - for admin users via API Gateway
 * 
 * Access control:
 * - Health endpoints: Public (for monitoring)
 * - Order emails: SERVICE role (shop-service) or ADMIN role
 * - Marketing emails: ADMIN role only
 * - Email logs: ADMIN role only
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health checks - public for monitoring/Gateway
                        .requestMatchers("/health/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // OpenAPI docs - public
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        
                        // Test endpoint - public
                        .requestMatchers(HttpMethod.GET, "/api/email/test").permitAll()
                        
                        // All other endpoints require authentication
                        // Fine-grained control via @PreAuthorize in controller
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
