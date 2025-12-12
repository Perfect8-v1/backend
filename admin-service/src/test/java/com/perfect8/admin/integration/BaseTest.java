package com.perfect8.admin.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base test class for all REST Assured integration tests
 * Supports both Plain (API-key) and AuthMan (JWT) branches
 * 
 * Plain branch: Uses X-API-Key header
 * AuthMan branch: Uses Bearer token
 */
public abstract class BaseTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Base URLs for different environments
    protected static final String PROD_BASE_URL = "http://127.0.0.1";
    protected static final String LOCAL_BASE_URL = "http://127.0.0.1";

    // Choose which environment to test
    protected static final String BASE_URL = LOCAL_BASE_URL;

    // Service ports
    protected static final int ADMIN_PORT = 8081;
    protected static final int BLOG_PORT = 8082;
    protected static final int EMAIL_PORT = 8083;
    protected static final int IMAGE_PORT = 8084;
    protected static final int SHOP_PORT = 8085;

    // API Keys for Plain branch (from .env)
    protected static final String ADMIN_API_KEY = "p8admin_7Kx9mN2pL4qR8sT1";
    protected static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";
    protected static final String EMAIL_API_KEY = "p8email_9Bz4cD7gJ1kQ6wY3";
    protected static final String IMAGE_API_KEY = "p8image_5Nt8rU2xA4eI7oS0";
    protected static final String SHOP_API_KEY = "p8shop_1Lm3pV6bC9fK2hW4";

    // Default port (override in subclasses)
    protected static int servicePort = ADMIN_PORT;
    protected static String apiKey = ADMIN_API_KEY;

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + servicePort;
        RestAssured.baseURI = fullUrl;

        // Request spec WITHOUT authentication (for public endpoints)
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        // Request spec WITH API-key authentication (for protected endpoints)
        authenticatedSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .addHeader("X-API-Key", apiKey)
                .build();

        System.out.println("🚀 REST Assured configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + apiKey.substring(0, 10) + "...");
    }

    /**
     * Helper method to print test results
     */
    protected void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }

    /**
     * Helper to get service URL
     */
    protected static String getServiceUrl(int port) {
        return BASE_URL + ":" + port;
    }
}
