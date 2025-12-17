package com.perfect8.shop.security;

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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// @Component - Disabled for dev branch (using JwtAuthenticationFilter instead)
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key Authentication Filter - Plain branch
 * 
 * NOTE: @Component disabled for dev branch.
 * Re-enable when switching back to API Key authentication.
 * 
 * @version 1.0
 */
// @Component - Disabled for dev branch (using JWT)
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String SERVICE_NAME = "shop";
    private final AuthProvider authProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey != null && !apiKey.isEmpty()) {
            if (authProvider.authenticate(apiKey, SERVICE_NAME)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                SERVICE_NAME + "-api-user",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("API Key OK for {}", SERVICE_NAME);
            }
        }

        filterChain.doFilter(request, response);
    }
}
