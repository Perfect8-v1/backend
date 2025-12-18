package com.perfect8.image.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for JWT Authentication via nginx (p8.rantila.com)
 * 
 * Nginx routing:
 * - /api/v1/images/ → image-service /api/images/
 * - /api/v1/auth/   → admin-service /api/auth/ (for login)
 * 
 * Image Service endpoints:
 * - GET /api/v1/images/{id}              → PUBLIC
 * - GET /api/v1/images/{id}/thumbnail/*  → PUBLIC
 * - POST /api/v1/images/upload           → PROTECTED
 * - DELETE /api/v1/images/{id}           → PROTECTED
 */
@DisplayName("Image Service - JWT Authentication Tests (Remote)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    // Configuration - via nginx
    private static final String BASE_URL = "https://p8.rantila.com";

    // Test credentials (admin-service login)
    private static final String TEST_EMAIL = "admin@perfect8.com";
    private static final String TEST_PASSWORD = "password";

    // Endpoints (nginx mapped)
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String IMAGES_ENDPOINT = "/api/v1/images";
    private static final String UPLOAD_ENDPOINT = "/api/v1/images/upload";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        System.out.println("🚀 Image Service tests configured for: " + BASE_URL);
    }

    // ==================== LOGIN (Get JWT Token from Admin Service) ====================

    @Test
    @Order(1)
    @DisplayName("Get JWT token from admin-service")
    public void testGetToken_FromAdminService() {
        System.out.println("\n🧪 Testing: Get JWT token from admin-service");

        String loginBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_EMAIL, TEST_PASSWORD);

        Response response = given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .response();

        jwtToken = response.jsonPath().getString("accessToken");
        System.out.println("   ✅ Received JWT token: " + jwtToken.substring(0, 20) + "...");
        
        logTestResult("Got JWT from admin-service", true);
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/images/{id} should work WITHOUT token (public)")
    public void testGetImage_NoToken_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET image without token (public endpoint)");

        // Will return 404 if image doesn't exist, but NOT 401/403
        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(IMAGES_ENDPOINT + "/1")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET image (public, no token)", true);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/images/{id}/thumbnail/small should work WITHOUT token")
    public void testGetThumbnail_NoToken_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET thumbnail without token (public endpoint)");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(IMAGES_ENDPOINT + "/1/thumbnail/small")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET thumbnail (public, no token)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/images/upload WITHOUT token should return 401 or 403")
    public void testUploadImage_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: POST upload WITHOUT token → 401/403");

        int statusCode = given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .multiPart("altText", "Test image")
        .when()
                .post(BASE_URL + UPLOAD_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("POST upload without token → " + statusCode, true);
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/v1/images/{id} WITHOUT token should return 401 or 403")
    public void testDeleteImage_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: DELETE image WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .delete(IMAGES_ENDPOINT + "/1")
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("DELETE without token → " + statusCode, true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH VALID TOKEN ====================

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/images/upload WITH valid JWT token should pass auth")
    public void testUploadImage_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: POST upload WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        // Will likely fail due to missing file, but should NOT return 401/403
        int statusCode = given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .multiPart("altText", "Test image from REST Assured")
        .when()
                .post(BASE_URL + UPLOAD_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, file might be missing)");
        logTestResult("POST upload with token → auth passes", true);
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/v1/images/{id} WITH valid JWT token should pass auth")
    public void testDeleteImage_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE image WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        // Delete non-existent image - should return 404, not 401/403
        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .delete(IMAGES_ENDPOINT + "/99999")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, image probably doesn't exist)");
        logTestResult("DELETE with token → auth passes", true);
    }

    // ==================== INVALID TOKENS ====================

    @Test
    @Order(8)
    @DisplayName("Invalid JWT token should return 401 or 403")
    public void testUpload_InvalidToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Invalid JWT token → 401/403");

        int statusCode = given()
                .contentType("multipart/form-data")
                .accept("application/json")
                .header("Authorization", "Bearer invalid.token.here")
                .multiPart("altText", "Test")
        .when()
                .post(BASE_URL + UPLOAD_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Invalid token → " + statusCode, true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
