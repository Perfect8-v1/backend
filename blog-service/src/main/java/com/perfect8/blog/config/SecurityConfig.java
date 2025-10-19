package com.perfect8.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration for Blog Service
 * Version 1.0 - Simplified security without JWT filter
 * UPDATED: Fixed URL patterns to match /api/v1/posts
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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

                        // Public blog post reading - FIXED: Added /v1 to all patterns
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/posts",
                                "/api/v1/posts/**",
                                "/api/v1/posts/search",
                                "/api/v1/posts/category/**",
                                "/api/v1/posts/author/**",
                                "/api/v1/posts/published"
                        ).permitAll()

                        // All other requests permitted for v1.0
                        .anyRequest().permitAll()
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
                "http://localhost:3000",      // React development
                "http://localhost:4200",      // Angular development
                "http://localhost:8080",      // Local testing
                "http://localhost:8082",      // Blog service port
                "http://localhost:5500",      // Flutter web dev
                "http://cmagnusb.org",        // Production domain
                "http://perfect8alpine.rantila.com"  // Production server
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

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Version 2.0 - JWT Authentication and authorization will be added
    /*
    // Add JwtAuthenticationFilter
    // Add authentication endpoints
    // Add role-based access control

    // Analytics endpoints - to be added in version 2.0
    .requestMatchers("/api/v1/analytics/**").hasRole("ADMIN")

    // Content metrics - to be added in version 2.0
    .requestMatchers("/api/v1/metrics/**").hasRole("ADMIN")
    */
}
