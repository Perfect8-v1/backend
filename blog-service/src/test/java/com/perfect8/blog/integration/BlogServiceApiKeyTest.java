package com.perfect8.blog.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for API-Key Authentication on Blog Service (Plain branch)
 * 
 * Blog Service endpoints:
 * - GET /api/v1/posts         → PUBLIC (list posts)
 * - GET /api/v1/posts/{slug}  → PUBLIC (get post by slug)
 * - POST /api/v1/posts        → PROTECTED (create post)
 * - PUT /api/v1/posts/{id}    → PROTECTED (update post)
 * - DELETE /api/v1/posts/{id} → PROTECTED (delete post)
 */
@DisplayName("Blog Service - API Key Authentication Tests")
public class BlogServiceApiKeyTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int BLOG_PORT = 8082;
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";
    private static final String ADMIN_API_KEY = "p8admin_7Kx9mN2pL4qR8sT1";

    // Endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String POSTS_ENDPOINT = "/api/v1/posts";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + BLOG_PORT;
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
                .addHeader("X-API-Key", BLOG_API_KEY)
                .build();

        System.out.println("🚀 Blog Service tests configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + BLOG_API_KEY.substring(0, 10) + "...");
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

    @Test
    @DisplayName("GET /api/v1/posts should work WITHOUT API key (public)")
    public void testGetPosts_NoApiKey_Returns200() {
        System.out.println("\n🧪 Testing: GET posts without API key (public endpoint)");

        given()
                .spec(requestSpec)
        .when()
                .get(POSTS_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET posts (no key) → 200", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("POST /api/v1/posts WITHOUT API key should return 403")
    public void testCreatePost_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST posts WITHOUT API key → 403");

        String postBody = """
                {
                    "title": "Test Post",
                    "content": "This is test content",
                    "slug": "test-post"
                }
                """;

        given()
                .spec(requestSpec)
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("POST without key → 403", true);
    }

    @Test
    @DisplayName("PUT /api/v1/posts/{id} WITHOUT API key should return 403")
    public void testUpdatePost_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: PUT posts WITHOUT API key → 403");

        String updateBody = """
                {
                    "title": "Updated Title",
                    "content": "Updated content"
                }
                """;

        given()
                .spec(requestSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/1")
        .then()
                .statusCode(403);

        logTestResult("PUT without key → 403", true);
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/{id} WITHOUT API key should return 403")
    public void testDeletePost_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: DELETE posts WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .delete(POSTS_ENDPOINT + "/1")
        .then()
                .statusCode(403);

        logTestResult("DELETE without key → 403", true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH KEY ====================

    @Test
    @DisplayName("POST /api/v1/posts WITH valid API key should pass auth")
    public void testCreatePost_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: POST posts WITH API key → auth passes");

        String postBody = """
                {
                    "title": "Test Post from REST Assured",
                    "content": "This is test content created by integration test",
                    "slug": "rest-assured-test-post"
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST with key → auth passes", true);
    }

    @Test
    @DisplayName("PUT /api/v1/posts/{id} WITH valid API key should pass auth")
    public void testUpdatePost_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: PUT posts WITH API key → auth passes");

        String updateBody = """
                {
                    "title": "Updated by REST Assured",
                    "content": "Updated content"
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/1")
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, post might not exist)");
        logTestResult("PUT with key → auth passes", true);
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/{id} WITH valid API key should pass auth")
    public void testDeletePost_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE posts WITH API key → auth passes");

        // Use a high ID that probably doesn't exist
        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .delete(POSTS_ENDPOINT + "/99999")
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, post probably doesn't exist)");
        logTestResult("DELETE with key → auth passes", true);
    }

    // ==================== WRONG SERVICE KEY ====================

    @Test
    @DisplayName("Admin API key on Blog service should return 403")
    public void testPost_WrongServiceKey_Returns403() {
        System.out.println("\n🧪 Testing: Admin key on Blog service → 403");

        String postBody = """
                {
                    "title": "Test",
                    "content": "Test"
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", ADMIN_API_KEY)  // Wrong key!
                .body(postBody)
        .when()
                .post(BASE_URL + ":" + BLOG_PORT + POSTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Wrong service key → 403", true);
    }

    @Test
    @DisplayName("Invalid API key should return 403")
    public void testPost_InvalidApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Invalid API key → 403");

        String postBody = """
                {
                    "title": "Test",
                    "content": "Test"
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "invalid_key_xyz")
                .body(postBody)
        .when()
                .post(BASE_URL + ":" + BLOG_PORT + POSTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Invalid API key → 403", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
