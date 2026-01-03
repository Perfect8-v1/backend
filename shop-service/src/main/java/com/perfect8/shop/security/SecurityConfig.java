package com.perfect8.shop.config;

import com.perfect8.shop.security.HeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Gör att @PreAuthorize("hasRole('ADMIN')") fungerar i din Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Inaktivera CSRF då vi kör stateless API
                .csrf(csrf -> csrf.disable())

                // Hantera CORS (Gateway sköter detta egentligen, men bra att ha som backup)
                .cors(cors -> cors.disable())

                // Sätt session management till stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Auktoriseringsregler
                .authorizeHttpRequests(auth -> auth
                        // Publika endpoints (t.ex. hämta produktlista)
                        .requestMatchers("/api/products/**").permitAll()
                        // Health checks och dokumentation
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Allt annat kräver att man är autentiserad via Gateway-headers
                        .anyRequest().authenticated()
                )

                // Lägg till vårt filter som läser headers (X-User-Id etc) från Gateway
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}