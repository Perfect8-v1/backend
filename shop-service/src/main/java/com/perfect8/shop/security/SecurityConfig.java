package com.perfect8.shop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration for Shop Service
 * Version 1.0 - Core security setup with versioned API endpoints
 * FIXED: Added @Qualifier for UserDetailsService (located in service package)
 * FIXED: Public endpoints FIRST in chain to prevent 403
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Qualifier("customerUserDetailsService")
    private final UserDetailsService userDetailsService;

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
                        // CRITICAL: Public endpoints MUST BE FIRST!
                        // Order matters in Spring Security filter chain

                        // Health check endpoints
                        .requestMatchers(
                                "/api/health",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // Public product endpoints - v1 API (GET only, specific paths)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products",                          // List all products
                                "/api/v1/products/{productId}",              // Get by ID
                                "/api/v1/products/search",                   // Search products
                                "/api/v1/products/category/{categoryId}",    // By category
                                "/api/v1/products/featured",                 // Featured products
                                "/api/v1/products/new"                       // New products
                        ).permitAll()

                        // Public category endpoints - v1 API (GET only, specific paths)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/categories",                        // List all categories
                                "/api/v1/categories/{categoryId}",           // Get by ID
                                "/api/v1/categories/tree",                   // Category tree
                                "/api/v1/categories/{categoryId}/products"   // Products in category
                        ).permitAll()

                        // Public auth endpoints - no authentication required
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // Cart endpoints - v1 API - authenticated users only
                        .requestMatchers("/api/v1/cart/**").authenticated()

                        // Customer endpoints - v1 API - authenticated users only
                        .requestMatchers("/api/v1/customers/**").authenticated()

                        // Order endpoints - v1 API - authenticated users only
                        .requestMatchers("/api/v1/orders/**").authenticated()

                        // Payment endpoints - v1 API - authenticated users only
                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // Shipment endpoints - v1 API - authenticated users only
                        .requestMatchers("/api/v1/shipments/**").authenticated()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Product management - v1 API - require ADMIN role
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")

                        // Category management - v1 API - require ADMIN role
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")

                        // Inventory management - v1 API - require ADMIN role
                        .requestMatchers("/api/v1/inventory/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authentication provider
                .authenticationProvider(authenticationProvider());

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
                "http://localhost:5500",      // Flutter web dev
                "http://cmagnusb.org",        // Production
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

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Authentication provider bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Version 2.0 - Commented out for future implementation
    /*
    // Analytics endpoints - to be added in version 2.0
    .requestMatchers("/api/v1/analytics/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/metrics/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/dashboard/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/reports/**").hasRole("ADMIN")

    // Coupon endpoints - to be added in version 2.0
    .requestMatchers("/api/v1/coupons/**").authenticated()

    // Performance monitoring endpoints - to be added in version 2.0
    .requestMatchers("/actuator/**").hasRole("ADMIN")
    */
}
