package com.perfect8.email.integration;

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
 * - /api/v1/email/ → email-service /api/email/
 * - /api/v1/auth/  → admin-service /api/auth/ (for login)
 */
@DisplayName("Email Service - JWT Authentication Tests (Remote)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    // Configuration - via nginx
    private static final String BASE_URL = "https://p8.rantila.com";

    // Test credentials (admin-service login)
    private static final String TEST_EMAIL = "admin@perfect8.com";
    private static final String TEST_PASSWORD = "password";

    // Endpoints (nginx mapped)
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String SEND_ENDPOINT = "/api/v1/email/send";
    private static final String LOGS_ENDPOINT = "/api/v1/email/logs";
    private static final String TEMPLATES_ENDPOINT = "/api/v1/email/templates";

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

        System.out.println("🚀 Email Service tests configured for: " + BASE_URL);
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

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/email/send WITHOUT token should return 401 or 403")
    public void testSendEmail_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: POST send email WITHOUT token → 401/403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test Email",
                    "body": "This is a test email"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("POST send without token → " + statusCode, true);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/email/logs WITHOUT token should return 401 or 403")
    public void testGetLogs_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: GET logs WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(LOGS_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("GET logs without token → " + statusCode, true);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/email/templates WITHOUT token should return 401 or 403")
    public void testGetTemplates_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: GET templates WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(TEMPLATES_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("GET templates without token → " + statusCode, true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH VALID TOKEN ====================

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/email/send WITH valid JWT token should pass auth")
    public void testSendEmail_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: POST send email WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        // Invalid email body to trigger validation error (400), not auth error (403)
        String emailBody = """
                {
                    "to": "invalid",
                    "subject": "",
                    "body": ""
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST send with token → auth passes", true);
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/email/logs WITH valid JWT token should pass auth")
    public void testGetLogs_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: GET logs WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .get(LOGS_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("GET logs with token → auth passes", true);
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/email/templates WITH valid JWT token should pass auth")
    public void testGetTemplates_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: GET templates WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .get(TEMPLATES_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("GET templates with token → auth passes", true);
    }

    // ==================== INVALID TOKENS ====================

    @Test
    @Order(8)
    @DisplayName("Invalid JWT token should return 401 or 403")
    public void testSendEmail_InvalidToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Invalid JWT token → 401/403");

        String emailBody = """
                {
                    "to": "test@example.com",
                    "subject": "Test",
                    "body": "Test"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
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
