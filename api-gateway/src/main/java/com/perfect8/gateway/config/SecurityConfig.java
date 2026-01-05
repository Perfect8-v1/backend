package com.perfect8.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        // 1. Auth & Salt & Actuator
                        .pathMatchers(
                                "/api/auth/**",
                                "/actuator/**"
                        ).permitAll()

                        // 2. Swagger / OpenAPI Documentation
                        // MAGNUM OPUS FIX: Vi måste lista dem explicit eftersom
                        // "/**/v3/api-docs/**" är ogiltigt i nyare Spring.
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                // Specifika tjänster
                                "/admin-service/v3/api-docs/**",
                                "/shop-service/v3/api-docs/**",
                                "/blog-service/v3/api-docs/**",
                                "/image-service/v3/api-docs/**",
                                "/email-service/v3/api-docs/**"
                        ).permitAll()

                        // 3. Tillåt OPTIONS (CORS)
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()

                        // 4. Utvecklingsläge
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}