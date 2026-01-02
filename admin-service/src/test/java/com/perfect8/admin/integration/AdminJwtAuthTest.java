package com.perfect8.admin.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for JWT Authentication via nginx (p8.rantila.com)
 * 
 * Nginx routing:
 * - /api/v1/auth/  → admin-service /api/auth/
 * - /api/v1/admin/ → admin-service /api/admin/
 */
@DisplayName("Admin Service - JWT Authentication Tests (Remote)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    // Configuration - via nginx
    private static final String BASE_URL = "https://p8.rantila.com";

    // Test credentials (must exist in database)
    private static final String TEST_EMAIL = "admin@perfect8.com";
    private static final String TEST_PASSWORD = "password";

    // Endpoints (nginx mapped)
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String SALT_ENDPOINT = "/api/v1/auth/salt";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String PROTECTED_ENDPOINT = "/api/v1/admin/users";

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        System.out.println("🚀 REST Assured configured for: " + BASE_URL);
    }

    // ==================== LOGIN (Get JWT Token) ====================

    @Test
    @Order(1)
    @DisplayName("Login should return JWT token")
    public void testLogin_ValidCredentials_ReturnsToken() {
        System.out.println("\n🧪 Testing: Login with valid credentials (client-side hashing)");

        // Step 1: Fetch salt for user
        String salt = given()
                .spec(requestSpec)
        .when()
                .get(SALT_ENDPOINT + "?email=" + TEST_EMAIL)
        .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("salt");

        System.out.println("   Got salt: " + salt);

        // Step 2: Hash password with BCrypt
        String passwordHash = BCrypt.hashpw(TEST_PASSWORD, salt);
        System.out.println("   Created hash: " + passwordHash.substring(0, 20) + "...");

        // Step 3: Login with hash
        String loginBody = """
                {
                    "email": "%s",
                    "passwordHash": "%s"
                }
                """.formatted(TEST_EMAIL, passwordHash);

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

    @Test
    @Order(2)
    @DisplayName("Login with invalid credentials should return 401")
    public void testLogin_InvalidCredentials_Returns401() {
        System.out.println("\n🧪 Testing: Login with invalid credentials → 401");

        // For invalid user, we send a fake hash (user doesn't exist, so no salt to fetch)
        String fakeHash = BCrypt.hashpw("wrongpassword", BCrypt.gensalt(10));

        String loginBody = """
                {
                    "email": "wrong@example.com",
                    "passwordHash": "%s"
                }
                """.formatted(fakeHash);

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(401);

        logTestResult("Invalid login → 401", true);
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @Order(3)
    @DisplayName("Health endpoint should work WITHOUT JWT token")
    public void testHealthEndpoint_NoToken_Returns200() {
        System.out.println("\n🧪 Testing: Health endpoint without token");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        logTestResult("Health endpoint (no token)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(4)
    @DisplayName("Protected endpoint WITHOUT token should return 401 or 403")
    public void testProtectedEndpoint_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Protected endpoint WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Protected GET without token → " + statusCode, true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH VALID TOKEN ====================

    @Test
    @Order(5)
    @DisplayName("Protected endpoint WITH valid JWT token should succeed")
    public void testProtectedEndpoint_WithToken_Succeeds() {
        System.out.println("\n🧪 Testing: Protected endpoint WITH JWT token → success");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("Protected GET with token → " + statusCode, true);
    }

    // ==================== INVALID TOKENS ====================

    @Test
    @Order(6)
    @DisplayName("Invalid JWT token should return 401 or 403")
    public void testProtectedEndpoint_InvalidToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Invalid JWT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Invalid token → " + statusCode, true);
    }

    @Test
    @Order(7)
    @DisplayName("Empty Authorization header should return 401 or 403")
    public void testProtectedEndpoint_EmptyHeader_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Empty Authorization header → 401/403");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "")
        .when()
                .get(PROTECTED_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Empty header → " + statusCode, true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
