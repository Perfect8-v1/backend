package com.perfect8.admin.util;

import com.perfect8.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Utility for admin-service (Central Auth)
 * 
 * UPDATED (2025-11-20):
 * - Added userId to JWT claims for cross-service identification
 * - Added extractUserId() method
 * - All services can now get userId from token
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:900000}")  // Default 15 minutes in milliseconds
    private long expirationTime;

    /**
     * Get expiration time in seconds
     */
    public long getExpirationTime() {
        return expirationTime / 1000;
    }

    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate token for user with userId, email and roles
     * PRIMARY METHOD - Use this for central auth
     */
    public String generateToken(Long userId, String email, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add userId to claims
        claims.put("userId", userId);
        
        // Add roles to claims
        claims.put("roles", roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList()));

        return createToken(claims, email);
    }

    /**
     * Generate token for user with email and roles (backward compatibility)
     * Note: userId will be null in token
     */
    public String generateToken(String email, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add roles to claims
        claims.put("roles", roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList()));

        return createToken(claims, email);
    }

    /**
     * Generate token from UserDetails (backward compatibility)
     * Note: userId will be null in token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add authorities/roles to claims
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList()));

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create JWT token - JJWT 0.12.3 API
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract userId from token
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
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
     * Extract email (subject) from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract username from token (alias for extractEmail)
     */
    public String extractUsername(String token) {
        return extractEmail(token);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token - JJWT 0.12.3 API
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token against user details
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate token (check expiration only)
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
