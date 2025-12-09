package com.perfect8.image.security;

import com.perfect8.common.auth.AuthProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key Authentication Filter for Image Service
 * 
 * Plain branch implementation - validates API key from X-API-Key header.
 * 
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String SERVICE_NAME = "image";

    private final AuthProvider authProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null && !apiKey.isBlank()) {
            if (authProvider.authenticate(apiKey, SERVICE_NAME)) {
                String username = authProvider.extractUsername(apiKey);
                boolean isAdmin = authProvider.isAdmin(apiKey);

                List<SimpleGrantedAuthority> authorities = isAdmin
                        ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {} with roles: {}", username, authorities);
            } else {
                log.warn("Invalid API key provided for service: {}", SERVICE_NAME);
            }
        }

        filterChain.doFilter(request, response);
    }
}
