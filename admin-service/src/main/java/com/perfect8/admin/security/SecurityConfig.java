package com.perfect8.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    // Vi använder manuell konstruktor med @Lazy för att undvika cirkulära beroenden
    public SecurityConfig(@Lazy HeaderAuthenticationFilter headerAuthenticationFilter) {
        this.headerAuthenticationFilter = headerAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // MAGNUM OPUS FIX: Explicita undantag för salt och auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/salt" // <--- DEN HÄR MÅSTE VARA HÄR!
                        ).permitAll()

                        // Swagger UI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // Wildcard backup (bra att ha kvar)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allt annat under /api/admin kräver inloggning
                        .requestMatchers("/api/admin/**").authenticated()

                        // Övrigt måste vara autentiserat
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}