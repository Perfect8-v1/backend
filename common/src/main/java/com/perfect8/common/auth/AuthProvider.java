package com.perfect8.common.auth;

/**
 * Authentication Provider Interface
 * 
 * Contract for authentication implementations.
 * Plain branch: ApiKeyAuthProvider (checks against .env)
 * AuthMan branch: JwtAuthProvider (JWT validation)
 * 
 * @version 1.0
 */
public interface AuthProvider {

    /**
     * Authenticate a request.
     *
     * @param credentials The credentials from request (API key or JWT token)
     * @param serviceName The service name (admin, blog, email, image, shop)
     * @return true if authenticated, false otherwise
     */
    boolean authenticate(String credentials, String serviceName);

    /**
     * Check if credentials have admin role.
     *
     * @param credentials The credentials from request
     * @return true if admin, false otherwise
     */
    boolean isAdmin(String credentials);

    /**
     * Extract username/identifier from credentials.
     *
     * @param credentials The credentials from request
     * @return Username or identifier, null if invalid
     */
    String extractUsername(String credentials);
}
