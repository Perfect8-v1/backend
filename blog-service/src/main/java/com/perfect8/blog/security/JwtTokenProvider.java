package com.perfect8.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider for blog-service
 * 
 * UPDATED (2025-11-20):
 * - Added resolveToken() for extracting token from request
 * - Added getUserIdFromToken() for central auth support
 * - Compatible with admin-service JWT tokens
 * - JJWT 0.12.3 API
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        // For JJWT 0.12.3, we need at least 256-bit key for HS256
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Extract JWT token from HTTP request Authorization header
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Get userId from JWT token
     * Returns null if userId claim is not present
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userIdClaim = claims.get("userId");
            if (userIdClaim != null) {
                if (userIdClaim instanceof Number) {
                    return ((Number) userIdClaim).longValue();
                }
                return Long.parseLong(userIdClaim.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException |
                 io.jsonwebtoken.MalformedJwtException ex) {
            log.error("Invalid JWT signature");
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
}
