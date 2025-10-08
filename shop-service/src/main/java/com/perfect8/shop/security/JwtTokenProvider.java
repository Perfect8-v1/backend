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
 * JWT Token Provider - Version 1.0
 * Handles JWT token generation and validation
 * MAGNUM OPUS COMPLIANT: No alias methods
 * UPDATED: JWT 0.12.3 API
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:ThisIsAVeryLongSecretKeyForJWTTokenGenerationAndItShouldBeAtLeast256BitsLong}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours in milliseconds
    private long jwtExpirationMs;

    @Value("${jwt.refresh.expiration:604800000}") // Default 7 days for refresh token
    private long refreshExpirationMs;

    @Value("${jwt.issuer:Perfect8Shop}")
    private String issuer;

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token from Authentication
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();

        // Add authorities to claims
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("authorities", authorities);

        // Extract role from authorities
        String role = extractRoleFromAuthorities(authentication);
        if (role != null) {
            claims.put("role", role);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate JWT token with custom user details
     */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("authorities", "ROLE_" + role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .id(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate JWT token with full customer details
     */
    public String generateTokenForCustomer(Long customerId, String email, String firstName,
                                           String lastName, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", customerId);
        claims.put("customerId", customerId);
        claims.put("email", email);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("role", role);
        claims.put("authorities", "ROLE_" + role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .id(String.valueOf(customerId))
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get username (email) from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Get customer ID from JWT token
     * FIXED: Renamed from getUserIdFromToken - Magnum Opus principle (customerId not userId)
     * This method handles both userId and customerId claims for token compatibility
     */
    public Long getCustomerIdFromToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Try different possible claim names (tokens may use either)
            if (claims.containsKey("customerId")) {
                return Long.valueOf(claims.get("customerId").toString());
            }
            if (claims.containsKey("userId")) {
                return Long.valueOf(claims.get("userId").toString());
            }
            if (claims.getId() != null) {
                return Long.valueOf(claims.getId());
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting customer ID from token", e);
            return null;
        }
    }

    /**
     * Get role from JWT token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Try to get role from claims
            if (claims.containsKey("role")) {
                return claims.get("role", String.class);
            }

            // Try to extract from authorities
            if (claims.containsKey("authorities")) {
                String authorities = claims.get("authorities", String.class);
                if (authorities != null && authorities.contains("ROLE_")) {
                    // Extract the role from authorities string
                    String[] parts = authorities.split(",");
                    for (String part : parts) {
                        if (part.startsWith("ROLE_")) {
                            return part.substring(5); // Remove "ROLE_" prefix
                        }
                    }
                }
            }

            return "CUSTOMER"; // Default role
        } catch (Exception e) {
            log.error("Error extracting role from token", e);
            return "CUSTOMER";
        }
    }

    /**
     * Get email from JWT token
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaims(token);

            // Try email claim first
            if (claims.containsKey("email")) {
                return claims.get("email", String.class);
            }

            // Fall back to subject (which is usually email)
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting email from token", e);
            return null;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get all claims from token
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get specific claim from token
     */
    public Object getClaim(String token, String claimName) {
        Claims claims = getClaims(token);
        return claims.get(claimName);
    }

    /**
     * Extract role from authorities
     */
    private String extractRoleFromAuthorities(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                // Return the first role found, without the ROLE_ prefix
                return auth.substring(5);
            }
        }
        return null;
    }

    /**
     * Get token from Authorization header
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Generate token with expiration time
     */
    public String generateTokenWithExpiration(String email, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get remaining validity time in milliseconds
     */
    public long getRemainingValidity(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long now = System.currentTimeMillis();
        return expiration.getTime() - now;
    }

    /**
     * Get authorities from token
     */
    public String getAuthoritiesFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("authorities", String.class);
        } catch (Exception e) {
            log.error("Error extracting authorities from token", e);
            return "ROLE_CUSTOMER";
        }
    }

    /**
     * Resolve token from HTTP request
     */
    public String resolveToken(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // Also check for token in query parameter (for websocket connections)
        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return null;
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // Version 2.0 features - commented out
    /*
    // Token blacklisting for logout
    private Set<String> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // Token rotation
    public String rotateToken(String oldToken) {
        if (validateToken(oldToken)) {
            String username = getUsernameFromToken(oldToken);
            Long userId = getUserIdFromToken(oldToken);
            String role = getRoleFromToken(oldToken);

            // Blacklist old token
            blacklistToken(oldToken);

            // Generate new token
            return generateToken(userId, username, role);
        }
        return null;
    }

    // Multi-tenant support
    public String generateTokenWithTenant(String email, String tenantId) {
        // Implementation for multi-tenant JWT
    }

    // Permissions in token
    public String generateTokenWithPermissions(String email, List<String> permissions) {
        // Implementation for permission-based JWT
    }
    */
}
