package com.perfect8.image.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health check endpoint - NO authentication required
                .requestMatchers("/actuator/health").permitAll()
                // Public image viewing endpoints
                .requestMatchers("/api/v1/images/view/**").permitAll()
                .requestMatchers("/api/v1/images/public/**").permitAll()
                // Image upload/management requires authentication
                .requestMatchers("/api/v1/images/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
