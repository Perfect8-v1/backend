// blog-service/src/main/java/com/perfect8/blog/config/SecurityConfig.java
package com.perfect8.blog.config;


import main.java.com.perfect8.blog.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // This is great for enabling @PreAuthorize on methods
public class SecurityConfig {

    // We no longer need the UserDetailsService or TokenProvider here directly.
    // They are used by the filter, which we will inject.

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // We now inject the filter itself, which Spring has already built with its dependencies.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // THE PASSWORD ENCODER IS REMOVED FROM THIS FILE.
    // It now lives in your new SecurityBeansConfig.java, which is much cleaner.

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                        // Here we define which endpoints are public and which are protected.
                        authorize
                                // --- Public Endpoints ---
                                .requestMatchers("/api/auth/**").permitAll() // Login/Register is public
                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll() // Anyone can view products
                                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll() // Anyone can view categories

                                // --- Admin-Only Endpoints for creating/updating/deleting products ---
                                .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // --- Writer/Editor roles for blog posts ---
                                .requestMatchers("/api/posts/create", "/api/posts/update/**", "/api/posts/delete/**").hasRole("WRITER")

                                // --- All other requests must be authenticated ---
                                .anyRequest().authenticated()
                )
                // We are building a stateless REST API, so we don't need sessions.
                .sessionManagement( session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // We add our custom JWT filter to run before the standard username/password filter.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}