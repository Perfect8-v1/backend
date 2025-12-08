package com.perfect8.common.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * API Key Authentication Provider
 * 
 * Simple authentication using API keys from .env file.
 * Used in Plain branch for testing without JWT complexity.
 * 
 * Each service has its own API key:
 * - ADMIN_API_KEY
 * - BLOG_API_KEY
 * - EMAIL_API_KEY
 * - IMAGE_API_KEY
 * - SHOP_API_KEY
 * 
 * @version 1.0
 */
@Component
public class ApiKeyAuthProvider implements AuthProvider {

    @Value("${ADMIN_API_KEY:}")
    private String adminApiKey;

    @Value("${BLOG_API_KEY:}")
    private String blogApiKey;

    @Value("${EMAIL_API_KEY:}")
    private String emailApiKey;

    @Value("${IMAGE_API_KEY:}")
    private String imageApiKey;

    @Value("${SHOP_API_KEY:}")
    private String shopApiKey;

    @Override
    public boolean authenticate(String credentials, String serviceName) {
        if (credentials == null || credentials.isBlank()) {
            return false;
        }

        String expectedKey = getKeyForService(serviceName);
        if (expectedKey == null || expectedKey.isBlank()) {
            return false;
        }

        return credentials.equals(expectedKey);
    }

    @Override
    public boolean isAdmin(String credentials) {
        // With API keys, if you have the admin key, you're admin
        if (credentials == null || credentials.isBlank()) {
            return false;
        }
        return credentials.equals(adminApiKey);
    }

    @Override
    public String extractUsername(String credentials) {
        // API keys don't have usernames, return service name or "api-user"
        if (credentials == null || credentials.isBlank()) {
            return null;
        }
        
        if (credentials.equals(adminApiKey)) return "admin-api-user";
        if (credentials.equals(blogApiKey)) return "blog-api-user";
        if (credentials.equals(emailApiKey)) return "email-api-user";
        if (credentials.equals(imageApiKey)) return "image-api-user";
        if (credentials.equals(shopApiKey)) return "shop-api-user";
        
        return null;
    }

    private String getKeyForService(String serviceName) {
        if (serviceName == null) {
            return null;
        }
        
        return switch (serviceName.toLowerCase()) {
            case "admin" -> adminApiKey;
            case "blog" -> blogApiKey;
            case "email" -> emailApiKey;
            case "image" -> imageApiKey;
            case "shop" -> shopApiKey;
            default -> null;
        };
    }
}
