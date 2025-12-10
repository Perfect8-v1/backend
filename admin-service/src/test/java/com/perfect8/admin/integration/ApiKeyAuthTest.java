package com.perfect8.admin.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for API-Key Authentication (Plain branch)
 * 
 * Tests the critical auth flow:
 * 1. Public endpoints work WITHOUT key
 * 2. Protected endpoints FAIL without key (403)
 * 3. Protected endpoints WORK with valid key
 * 
 * DEBUG VERSION - Full logging enabled
 */
@DisplayName("Admin Service - API Key Authentication Tests")
public class ApiKeyAuthTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int ADMIN_PORT = 8081;
    private static final String ADMIN_API_KEY = "p8admin_7Kx9mN2pL4qR8sT1";
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";

    // Endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String PROTECTED_ENDPOINT = "/api/admin/users";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + ADMIN_PORT;
        RestAssured.baseURI = fullUrl;
        
        // Enable logging when tests fail
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

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
                .addHeader("X-API-Key", ADMIN_API_KEY)
                .build();

        System.out.println("🚀 REST Assured configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + ADMIN_API_KEY.substring(0, 10) + "...");
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @DisplayName("Health endpoint should work WITHOUT API key")
    public void testHealthEndpoint_NoApiKey_Returns200() {
        System.out.println("\n🧪 Testing: Health endpoint without API key");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        logTestResult("Health endpoint (no key)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("Protected endpoint WITHOUT API key should return 403")
    public void testProtectedEndpoint_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Protected endpoint WITHOUT API key → 403");

        given()
                .spec(requestSpec)  // No API key
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Protected GET without key → 403", true);
    }

    @Test
    @DisplayName("POST to protected endpoint WITHOUT API key should return 403")
    public void testProtectedPost_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST without API key → 403");

        String body = """
                {
                    "username": "testuser",
                    "email": "test@test.com",
                    "password": "test123"
                }
                """;

        given()
                .spec(requestSpec)  // No API key
                .body(body)
        .when()
                .post(PROTECTED_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Protected POST without key → 403", true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH KEY ====================

    @Test
    @DisplayName("Protected endpoint WITH valid API key should NOT return 403")
    public void testProtectedEndpoint_WithApiKey_NotForbidden() {
        System.out.println("\n🧪 Testing: Protected endpoint WITH API key → NOT 403");
        System.out.println("🔍 DEBUG: Logging full request and response...\n");

        int statusCode = given()
                .spec(authenticatedSpec)  // WITH API key
                .log().all()  // LOG REQUEST
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .log().all()  // LOG RESPONSE
                .statusCode(not(403))  // Anything but 403 means auth passed
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("Protected GET with key → NOT 403", true);
    }

    @Test
    @DisplayName("POST with valid API key should pass authentication")
    public void testProtectedPost_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: POST with API key → auth passes");
        System.out.println("🔍 DEBUG: Logging full request and response...\n");

        // Note: This might return 400/500 due to missing/invalid body
        // but NOT 403 - that proves auth worked
        String body = """
                {
                    "test": "data"
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)  // WITH API key
                .body(body)
                .log().all()  // LOG REQUEST
        .when()
                .post(PROTECTED_ENDPOINT)
        .then()
                .log().all()  // LOG RESPONSE
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, body might be invalid)");
        logTestResult("Protected POST with key → auth passes", true);
    }

    // ==================== INVALID KEY ====================

    @Test
    @DisplayName("Invalid API key should return 403")
    public void testProtectedEndpoint_InvalidApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Invalid API key → 403");

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "invalid_key_12345")
        .when()
                .get(BASE_URL + ":" + ADMIN_PORT + PROTECTED_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Invalid API key → 403", true);
    }

    @Test
    @DisplayName("Empty API key should return 403")
    public void testProtectedEndpoint_EmptyApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Empty API key → 403");

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "")
        .when()
                .get(BASE_URL + ":" + ADMIN_PORT + PROTECTED_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Empty API key → 403", true);
    }

    // ==================== WRONG SERVICE KEY ====================

    @Test
    @DisplayName("Blog API key on Admin service should return 403")
    public void testProtectedEndpoint_WrongServiceKey_Returns403() {
        System.out.println("\n🧪 Testing: Wrong service API key → 403");

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", BLOG_API_KEY)  // Wrong key!
        .when()
                .get(BASE_URL + ":" + ADMIN_PORT + PROTECTED_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Wrong service key → 403", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
