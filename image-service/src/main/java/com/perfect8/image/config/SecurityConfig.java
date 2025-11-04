package com.perfect8.image.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration for Image Service
 * Version 1.0 - Core security setup for image upload and management
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configure Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Set session management to stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Health check endpoints
                        .requestMatchers(
                                "/api/health",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // Public image viewing (GET)
                        .requestMatchers(HttpMethod.GET,
                                "/api/images/{imageId}",
                                "/api/images/*/thumbnail",
                                "/api/images/*/preview"
                        ).permitAll()

                        // Image upload/management - require authentication
                        .requestMatchers(HttpMethod.POST, "/api/images/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/images/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/images/**").authenticated()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Configure CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow origins - configure based on environment
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React development
                "http://localhost:4200",  // Angular development
                "http://localhost:8080",  // Local testing
                "http://localhost:8081",  // admin-service
                "http://localhost:8082",  // blog-service
                "http://localhost:8084",  // image-service
                "http://localhost:8085"   // shop-service
                // Add production URLs here
        ));

        // Allow methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Version 2.0 - Commented out for future implementation
    /*
    // Image analytics - to be added in version 2.0
    .requestMatchers("/api/images/analytics/**").hasRole("ADMIN")

    // Image metrics - to be added in version 2.0
    .requestMatchers("/api/images/metrics/**").hasRole("ADMIN")

    // Advanced image processing - to be added in version 2.0
    .requestMatchers("/api/images/batch/**").hasRole("ADMIN")
    */
}