package com.perfect8.shop.security;

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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for Shop Service
 * 
 * Dev branch - JWT authentication enabled
 * 
 * @version 1.0-jwt
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwtToken = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateToken(jwtToken)) {
                String userEmail = jwtTokenProvider.getUsernameFromToken(jwtToken);
                String authoritiesString = jwtTokenProvider.getAuthoritiesFromToken(jwtToken);
                List<SimpleGrantedAuthority> authorities = parseAuthorities(authoritiesString);
                Long customerId = jwtTokenProvider.getCustomerIdFromToken(jwtToken);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                if (customerId != null) {
                    request.setAttribute("customerId", customerId);
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Successfully authenticated user: {} for request: {}",
                        userEmail, request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    private List<SimpleGrantedAuthority> parseAuthorities(String authoritiesString) {
        if (!StringUtils.hasText(authoritiesString)) {
            return Collections.emptyList();
        }

        return Arrays.stream(authoritiesString.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/api/public/") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/products/public") ||
                path.startsWith("/api/categories/public") ||
                path.equals("/api/health") ||
                path.equals("/actuator/health");
    }

    public static Long getCurrentCustomerId(HttpServletRequest request) {
        Object customerIdObj = request.getAttribute("customerId");
        if (customerIdObj != null) {
            if (customerIdObj instanceof Long) {
                return (Long) customerIdObj;
            } else if (customerIdObj instanceof String) {
                try {
                    return Long.parseLong((String) customerIdObj);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }
}
