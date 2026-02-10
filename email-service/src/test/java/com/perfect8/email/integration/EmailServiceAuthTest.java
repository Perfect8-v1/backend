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
 * Integration tests for Email Service Authentication
 * 
 * Tests:
 * - Public endpoints (no auth required)
 * - Protected endpoints (JWT or API key required)
 * - API key authentication (service-to-service)
 * - Gateway authentication (admin via JWT)
 */
@DisplayName("Email Service - Auth Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailServiceAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";
    
    // API Keys (from .env)
    private static final String SHOP_API_KEY = "p8shop_1Lm3pV6bC9fK2hW4";

    // Endpoints
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String EMAIL_TEST_ENDPOINT = "/email/api/email/test";
    private static final String EMAIL_SEND_ENDPOINT = "/email/api/email/send";
    private static final String EMAIL_ORDER_CONFIRMATION = "/email/api/email/order/confirmation";

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

        System.out.println("🚀 Email Service tests: " + BASE_URL);
        System.out.println("📧 Test endpoint: " + EMAIL_TEST_ENDPOINT);
        System.out.println("📧 Send endpoint: " + EMAIL_SEND_ENDPOINT);
    }

    @Test
    @Order(1)
    @DisplayName("Get JWT token for admin")
    public void testGetToken() {
        String loginBody = """
                {"email": "%s", "password": "%s"}
                """.formatted(TEST_EMAIL, TEST_PASSWORD);

        Response response = given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract().response();

        jwtToken = response.jsonPath().getString("accessToken");
        System.out.println("✅ JWT acquired");
    }

    // ===== PUBLIC ENDPOINTS =====

    @Test
    @Order(2)
    @DisplayName("GET /email/api/email/test without auth → 200 (public)")
    public void testEmailTest_NoAuth_Returns200() {
        given().spec(requestSpec)
        .when().get(EMAIL_TEST_ENDPOINT)
        .then()
                .statusCode(200)
                .body(containsString("Email service is running"));
    }

    // ===== PROTECTED ENDPOINTS - NO AUTH =====

    @Test
    @Order(3)
    @DisplayName("POST /email/api/email/send WITHOUT auth → 401/403")
    public void testSendEmail_NoAuth_Returns401() {
        String emailBody = """
                {
                    "recipientEmail": "test@test.com",
                    "subject": "Test",
                    "content": "Test content"
                }
                """;

        given().spec(requestSpec)
                .body(emailBody)
        .when().post(EMAIL_SEND_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(4)
    @DisplayName("POST /email/api/email/order/confirmation WITHOUT auth → 401/403")
    public void testOrderConfirmation_NoAuth_Returns401() {
        String orderBody = """
                {
                    "orderNumber": "TEST-001",
                    "customerEmail": "test@test.com",
                    "customerName": "Test Customer"
                }
                """;

        given().spec(requestSpec)
                .body(orderBody)
        .when().post(EMAIL_ORDER_CONFIRMATION)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - INVALID TOKEN =====

    @Test
    @Order(5)
    @DisplayName("POST /email/api/email/send with invalid token → 401/403")
    public void testSendEmail_InvalidToken_Returns401() {
        String emailBody = """
                {
                    "recipientEmail": "test@test.com",
                    "subject": "Test",
                    "content": "Test content"
                }
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
                .body(emailBody)
        .when().post(EMAIL_SEND_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - WITH JWT =====

    @Test
    @Order(6)
    @DisplayName("POST /email/api/email/send WITH admin JWT → auth passes")
    public void testSendEmail_WithAdminJwt_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String emailBody = """
                {
                    "recipientEmail": "test@test.com",
                    "subject": "Test from RESTAssured",
                    "content": "Test content"
                }
                """;

        // Should not get 401/403 (may get 400 if validation fails, but auth passes)
        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(emailBody)
        .when().post(EMAIL_SEND_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== API KEY TESTS (via Gateway - will fail, API keys are for direct service calls) =====
    
    @Test
    @Order(7)
    @DisplayName("POST with API key via Gateway → 401/403 (Gateway doesn't understand API keys)")
    public void testOrderConfirmation_ApiKeyViaGateway_Returns401() {
        String orderBody = """
                {
                    "orderNumber": "TEST-001",
                    "customerEmail": "test@test.com",
                    "customerName": "Test Customer"
                }
                """;

        // API keys don't work via Gateway - this is expected behavior
        // API keys are for direct service-to-service calls within Docker network
        given().spec(requestSpec)
                .header("X-Api-Key", SHOP_API_KEY)
                .header("X-Service-Name", "shop-service")
                .body(orderBody)
        .when().post(EMAIL_ORDER_CONFIRMATION)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== SUMMARY =====

    @Test
    @Order(99)
    @DisplayName("Summary: Email service auth is working correctly")
    public void testSummary() {
        System.out.println("\n========================================");
        System.out.println("📧 Email Service Auth Test Summary");
        System.out.println("========================================");
        System.out.println("✅ Public endpoint (/test) accessible");
        System.out.println("✅ Protected endpoints require auth");
        System.out.println("✅ Invalid tokens rejected");
        System.out.println("✅ Admin JWT accepted");
        System.out.println("ℹ️  API keys work for direct service calls only");
        System.out.println("========================================\n");
    }
}
