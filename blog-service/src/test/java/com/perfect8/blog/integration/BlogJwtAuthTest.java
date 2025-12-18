package com.perfect8.blog.integration;

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
 * - /api/v1/posts/ → blog-service /api/posts/
 * - /api/v1/auth/  → admin-service /api/auth/ (for login)
 */
@DisplayName("Blog Service - JWT Authentication Tests (Remote)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    // Configuration - via nginx
    private static final String BASE_URL = "https://p8.rantila.com";

    // Test credentials (admin-service login)
    private static final String TEST_EMAIL = "admin@perfect8.com";
    private static final String TEST_PASSWORD = "password";

    // Endpoints (nginx mapped)
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String POSTS_ENDPOINT = "/api/v1/posts";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        System.out.println("🚀 Blog Service tests configured for: " + BASE_URL);
    }

    // ==================== LOGIN (Get JWT Token from Admin Service) ====================

    @Test
    @Order(1)
    @DisplayName("Get JWT token from admin-service")
    public void testLogin_ValidCredentials_ReturnsToken() {
        System.out.println("\n🧪 Testing: Login with valid credentials");

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
        
        logTestResult("Login returns JWT token", true);
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/posts should work WITHOUT token (public)")
    public void testGetPosts_NoToken_Returns200() {
        System.out.println("\n🧪 Testing: GET posts without token (public endpoint)");

        given()
                .spec(requestSpec)
        .when()
                .get(POSTS_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET posts (public, no token)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/posts WITHOUT token should return 401 or 403")
    public void testCreatePost_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: POST posts WITHOUT token → 401/403");

        String postBody = """
                {
                    "title": "Test Post",
                    "content": "This is test content",
                    "slug": "test-post"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("POST without token → " + statusCode, true);
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/posts/{id} WITHOUT token should return 401 or 403")
    public void testUpdatePost_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: PUT posts WITHOUT token → 401/403");

        String updateBody = """
                {
                    "title": "Updated Title",
                    "content": "Updated content"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/1")
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("PUT without token → " + statusCode, true);
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/v1/posts/{id} WITHOUT token should return 401 or 403")
    public void testDeletePost_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: DELETE posts WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .delete(POSTS_ENDPOINT + "/1")
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
    @DisplayName("POST /api/v1/posts WITH valid JWT token should pass auth")
    public void testCreatePost_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: POST posts WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        String postBody = """
                {
                    "title": "Test Post from REST Assured",
                    "content": "This is test content created by integration test",
                    "slug": "rest-assured-test-post"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST with token → auth passes", true);
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/v1/posts/{id} WITH valid JWT token should pass auth")
    public void testDeletePost_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE posts WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .delete(POSTS_ENDPOINT + "/99999")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, post probably doesn't exist)");
        logTestResult("DELETE with token → auth passes", true);
    }

    // ==================== INVALID TOKENS ====================

    @Test
    @Order(8)
    @DisplayName("Invalid JWT token should return 401 or 403")
    public void testPost_InvalidToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Invalid JWT token → 401/403");

        String postBody = """
                {
                    "title": "Test",
                    "content": "Test"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
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
