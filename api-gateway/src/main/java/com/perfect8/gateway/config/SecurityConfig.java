package com.perfect8.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway Security Config
 * Hanterar routing-säkerhet.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        // MAGNUM OPUS FIX: Explicita tillåtelser i Gatewayen
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/salt", // <--- VIKTIGT
                                "/actuator/**"
                        ).permitAll()

                        // Wildcards
                        .pathMatchers("/api/auth/**").permitAll()

                        // För utveckling tillåter vi allt annat också just nu,
                        // men ovanstående rader garanterar att saltet släpps igenom först.
                        .anyExchange().permitAll()
                )
                .build();
    }
}