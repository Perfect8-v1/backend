package com.perfect8.shop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for authentication and authorization.
 * Version 1.0 - Core JWT functionality
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:MyVeryLongSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456789}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    @Value("${jwt.refresh.expiration:604800000}") // 7 days in milliseconds
    private long refreshExpirationMs;

    @Value("${jwt.password.reset.expiration:3600000}") // 1 hour in milliseconds
    private long passwordResetExpirationMs;

    @Value("${jwt.email.verification.expiration:86400000}") // 24 hours in milliseconds
    private long emailVerificationExpirationMs;

    /**
     * Get the secret key for signing JWTs
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token from Spring Security Authentication
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);

        return createToken(claims, username, jwtExpirationMs);
    }

    /**
     * Generate JWT token with custom parameters (for AuthService compatibility)
     */
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("authorities", role);

        return createToken(claims, email, jwtExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return createToken(claims, email, refreshExpirationMs);
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "password-reset");

        return createToken(claims, userId.toString(), passwordResetExpirationMs);
    }

    /**
     * Generate email verification token
     */
    public String generateEmailVerificationToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "email-verification");

        return createToken(claims, userId.toString(), emailVerificationExpirationMs);
    }

    /**
     * Create a token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse userId from token: {}", userIdObj);
                return null;
            }
        }
        return null;
    }

    /**
     * Get email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object email = claims.get("email");

        if (email != null) {
            return email.toString();
        }

        // Fallback to subject if email not in claims
        return claims.getSubject();
    }

    /**
     * Get role/authorities from JWT token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object role = claims.get("role");

        if (role != null) {
            return role.toString();
        }

        // Fallback to authorities
        Object authorities = claims.get("authorities");
        if (authorities != null) {
            return authorities.toString();
        }

        return "USER"; // Default role
    }

    /**
     * Get token type from JWT token
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        Object type = claims.get("type");
        return type != null ? type.toString() : "access";
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Validate token for specific type
     */
    public boolean validateTokenForType(String token, String expectedType) {
        if (!validateToken(token)) {
            return false;
        }

        String tokenType = getTokenType(token);
        return expectedType.equals(tokenType);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Get expiration time in milliseconds (for AuthService)
     */
    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    /**
     * Get refresh token expiration time in milliseconds
     */
    public long getRefreshExpirationTime() {
        return refreshExpirationMs;
    }

    /**
     * Get password reset token expiration time in milliseconds
     */
    public long getPasswordResetExpirationTime() {
        return passwordResetExpirationMs;
    }

    /**
     * Get email verification token expiration time in milliseconds
     */
    public long getEmailVerificationExpirationTime() {
        return emailVerificationExpirationMs;
    }

    /**
     * Get all claims from token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get remaining time until token expires
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long now = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            return Math.max(0, expirationTime - now);
        } catch (Exception e) {
            log.error("Error getting remaining time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Refresh an existing token (create new token with same claims but new expiration)
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            claims.setIssuedAt(new Date());

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            claims.setExpiration(expiryDate);

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Could not refresh token", e);
        }
    }

    /**
     * Extract token from Bearer string
     */
    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}