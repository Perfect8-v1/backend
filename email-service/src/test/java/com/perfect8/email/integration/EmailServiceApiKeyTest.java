package com.perfect8.email.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for API-Key Authentication on Email Service (Plain branch)
 * 
 * Email Service endpoints:
 * - GET /actuator/health      → PUBLIC
 * - POST /api/email/send      → PROTECTED (send email)
 * - GET /api/email/logs       → PROTECTED (view logs)
 * - GET /api/email/templates  → PROTECTED (list templates)
 */
@DisplayName("Email Service - API Key Authentication Tests")
public class EmailServiceApiKeyTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int EMAIL_PORT = 8083;
    private static final String EMAIL_API_KEY = "p8email_9Bz4cD7gJ1kQ6wY3";
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";

    // Endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String SEND_ENDPOINT = "/api/email/send";
    private static final String LOGS_ENDPOINT = "/api/email/logs";
    private static final String TEMPLATES_ENDPOINT = "/api/email/templates";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + EMAIL_PORT;
        RestAssured.baseURI = fullUrl;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        authenticatedSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .addHeader("X-API-Key", EMAIL_API_KEY)
                .build();

        System.out.println("🚀 Email Service tests configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + EMAIL_API_KEY.substring(0, 10) + "...");
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
    @DisplayName("POST /api/email/send WITHOUT API key should return 403")
    public void testSendEmail_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST send email WITHOUT API key → 403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test Email",
                    "body": "This is a test email"
                }
                """;

        given()
                .spec(requestSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("POST send without key → 403", true);
    }

    @Test
    @DisplayName("GET /api/email/logs WITHOUT API key should return 403")
    public void testGetLogs_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: GET logs WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .get(LOGS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("GET logs without key → 403", true);
    }

    @Test
    @DisplayName("GET /api/email/templates WITHOUT API key should return 403")
    public void testGetTemplates_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: GET templates WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .get(TEMPLATES_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("GET templates without key → 403", true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH KEY ====================

    @Test
    @DisplayName("POST /api/email/send WITH valid API key should pass auth")
    public void testSendEmail_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: POST send email WITH API key → auth passes");

        // Note: We don't actually want to send emails in tests
        // So we use an invalid email body to trigger validation error (400)
        // but NOT auth error (403)
        String emailBody = """
                {
                    "to": "invalid",
                    "subject": "",
                    "body": ""
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST send with key → auth passes", true);
    }

    @Test
    @DisplayName("GET /api/email/logs WITH valid API key should pass auth")
    public void testGetLogs_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: GET logs WITH API key → auth passes");

        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .get(LOGS_ENDPOINT)
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("GET logs with key → auth passes", true);
    }

    @Test
    @DisplayName("GET /api/email/templates WITH valid API key should pass auth")
    public void testGetTemplates_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: GET templates WITH API key → auth passes");

        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .get(TEMPLATES_ENDPOINT)
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("GET templates with key → auth passes", true);
    }

    // ==================== WRONG SERVICE KEY ====================

    @Test
    @DisplayName("Blog API key on Email service should return 403")
    public void testSendEmail_WrongServiceKey_Returns403() {
        System.out.println("\n🧪 Testing: Blog key on Email service → 403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test",
                    "body": "Test"
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", BLOG_API_KEY)  // Wrong key!
                .body(emailBody)
        .when()
                .post(BASE_URL + ":" + EMAIL_PORT + SEND_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Wrong service key → 403", true);
    }

    @Test
    @DisplayName("Invalid API key should return 403")
    public void testSendEmail_InvalidApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Invalid API key → 403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test",
                    "body": "Test"
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "invalid_key_xyz")
                .body(emailBody)
        .when()
                .post(BASE_URL + ":" + EMAIL_PORT + SEND_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Invalid API key → 403", true);
    }

    @Test
    @DisplayName("Empty API key should return 403")
    public void testSendEmail_EmptyApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Empty API key → 403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test",
                    "body": "Test"
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "")
                .body(emailBody)
        .when()
                .post(BASE_URL + ":" + EMAIL_PORT + SEND_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Empty API key → 403", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
