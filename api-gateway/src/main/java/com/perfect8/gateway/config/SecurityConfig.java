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
                // I Gateway (WebFlux) inaktiverar vi CSRF så här
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Hantera CORS (Cross-Origin Resource Sharing)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Auktoriseringsregler
                .authorizeExchange(exchange -> exchange
                        // 1. Auth & Salt & Actuator (Måste vara öppna)
                        .pathMatchers(
                                "/api/auth/**",
                                "/actuator/**"
                        ).permitAll()

                        // 2. Swagger / OpenAPI Documentation
                        // Vi måste tillåta åtkomst till JSON-docs och UI-resurser
                        .pathMatchers(
                                "/**/v3/api-docs/**",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 3. Tillåt OPTIONS anrop (CORS pre-flight)
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()

                        //TODO kolla upp detta noga.
                        // 4. Utvecklingsläge: Släpp igenom allt
                        // (Eftersom vi hanterar säkerheten i respektive mikrotjänst också)
                        .anyExchange().permitAll()
                )
                .build();
    }

    /**
     * Konfigurera CORS för Reactive Stack (WebFlux)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // I prod: Byt "*" mot "https://p8.rantila.com"
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}