package com.perfect8.image.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for API-Key Authentication on Image Service (Plain branch)
 * 
 * Image Service endpoints:
 * - GET /actuator/health              → PUBLIC
 * - GET /api/images/{id}              → PUBLIC (get image)
 * - GET /api/images/{id}/thumbnail/*  → PUBLIC (get thumbnail)
 * - POST /api/images/upload           → PROTECTED (upload image)
 * - DELETE /api/images/{id}           → PROTECTED (delete image)
 */
@DisplayName("Image Service - API Key Authentication Tests")
public class ImageServiceApiKeyTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://127.0.0.1";
    private static final int IMAGE_PORT = 8084;
    private static final String IMAGE_API_KEY = "p8image_5Nt8rU2xA4eI7oS0";
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";

    // Endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String IMAGES_ENDPOINT = "/api/images";
    private static final String UPLOAD_ENDPOINT = "/api/images/upload";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + IMAGE_PORT;
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
                .addHeader("X-API-Key", IMAGE_API_KEY)
                .build();

        System.out.println("🚀 Image Service tests configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + IMAGE_API_KEY.substring(0, 10) + "...");
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
    @DisplayName("GET /api/images/{id} should work WITHOUT API key (public)")
    public void testGetImage_NoApiKey_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET image without API key (public endpoint)");

        // This will return 404 if image doesn't exist, but NOT 403
        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(IMAGES_ENDPOINT + "/1")
        .then()
                .statusCode(not(403))  // Public endpoint - no 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET image (public, no key)", true);
    }

    @Test
    @DisplayName("GET /api/images/{id}/thumbnail/small should work WITHOUT API key")
    public void testGetThumbnail_NoApiKey_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET thumbnail without API key (public endpoint)");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(IMAGES_ENDPOINT + "/1/thumbnail/small")
        .then()
                .statusCode(not(403))  // Public endpoint - no 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET thumbnail (public, no key)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("POST /api/images/upload WITHOUT API key should return 403")
    public void testUploadImage_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST upload WITHOUT API key → 403");

        // Multipart request without file (will fail validation but auth first)
        given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .multiPart("altText", "Test image")
        .when()
                .post(BASE_URL + ":" + IMAGE_PORT + UPLOAD_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("POST upload without key → 403", true);
    }

    @Test
    @DisplayName("DELETE /api/images/{id} WITHOUT API key should return 403")
    public void testDeleteImage_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: DELETE image WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .delete(IMAGES_ENDPOINT + "/1")
        .then()
                .statusCode(403);

        logTestResult("DELETE without key → 403", true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH KEY ====================

    @Test
    @DisplayName("POST /api/images/upload WITH valid API key should pass auth")
    public void testUploadImage_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: POST upload WITH API key → auth passes");

        // Multipart request - will likely fail due to missing file
        // but should NOT return 403 (auth passed)
        int statusCode = given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("X-API-Key", IMAGE_API_KEY)
                .multiPart("altText", "Test image from REST Assured")
        .when()
                .post(BASE_URL + ":" + IMAGE_PORT + UPLOAD_ENDPOINT)
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, file might be missing)");
        logTestResult("POST upload with key → auth passes", true);
    }

    @Test
    @DisplayName("DELETE /api/images/{id} WITH valid API key should pass auth")
    public void testDeleteImage_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE image WITH API key → auth passes");

        // Delete non-existent image - should return 404, not 403
        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .delete(IMAGES_ENDPOINT + "/99999")
        .then()
                .statusCode(not(403))  // Auth passed if not 403
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, image probably doesn't exist)");
        logTestResult("DELETE with key → auth passes", true);
    }

    // ==================== WRONG SERVICE KEY ====================

    @Test
    @DisplayName("Blog API key on Image service should return 403")
    public void testUpload_WrongServiceKey_Returns403() {
        System.out.println("\n🧪 Testing: Blog key on Image service → 403");

        given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("X-API-Key", BLOG_API_KEY)  // Wrong key!
                .multiPart("altText", "Test")
        .when()
                .post(BASE_URL + ":" + IMAGE_PORT + UPLOAD_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Wrong service key → 403", true);
    }

    @Test
    @DisplayName("Invalid API key should return 403")
    public void testUpload_InvalidApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Invalid API key → 403");

        given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("X-API-Key", "invalid_key_xyz")
                .multiPart("altText", "Test")
        .when()
                .post(BASE_URL + ":" + IMAGE_PORT + UPLOAD_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Invalid API key → 403", true);
    }

    @Test
    @DisplayName("Empty API key should return 403")
    public void testUpload_EmptyApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Empty API key → 403");

        given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("X-API-Key", "")
                .multiPart("altText", "Test")
        .when()
                .post(BASE_URL + ":" + IMAGE_PORT + UPLOAD_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Empty API key → 403", true);
    }

    // ==================== THUMBNAIL SIZES ====================

    @Test
    @DisplayName("All thumbnail sizes should be accessible (public)")
    public void testThumbnailSizes_AllPublic() {
        System.out.println("\n🧪 Testing: All thumbnail sizes (public endpoints)");

        String[] sizes = {"small", "medium", "large", "xlarge"};

        for (String size : sizes) {
            int statusCode = given()
                    .spec(requestSpec)
            .when()
                    .get(IMAGES_ENDPOINT + "/1/thumbnail/" + size)
            .then()
                    .statusCode(not(403))
                    .extract()
                    .statusCode();

            System.out.println("   Thumbnail '" + size + "': " + statusCode);
        }

        logTestResult("All thumbnail sizes accessible", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
