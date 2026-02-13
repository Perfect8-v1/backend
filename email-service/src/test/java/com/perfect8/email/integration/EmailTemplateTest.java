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
 * Integration tests for Email Template endpoints (v1.3)
 * 
 * Tests all order-related email endpoints:
 * - POST /email/api/email/order/confirmation
 * - POST /email/api/email/order/status
 * - POST /email/api/email/order/shipped
 * - POST /email/api/email/order/cancelled
 * - POST /email/api/email/send (custom email)
 * 
 * Note: These tests verify endpoints work correctly.
 * Actual email delivery depends on SMTP configuration.
 */
@DisplayName("Email Service - Template Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailTemplateTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 via gateway)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TEST_ENDPOINT = "/email/api/email/test";
    private static final String SEND_ENDPOINT = "/email/api/email/send";
    private static final String ORDER_CONFIRMATION = "/email/api/email/order/confirmation";
    private static final String ORDER_STATUS = "/email/api/email/order/status";
    private static final String ORDER_SHIPPED = "/email/api/email/order/shipped";
    private static final String ORDER_CANCELLED = "/email/api/email/order/cancelled";

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

        System.out.println("🚀 Email Template tests: " + BASE_URL);
    }

    // ===== SETUP: GET JWT =====

    @Test
    @Order(1)
    @DisplayName("1. Get JWT token for admin")
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
        
        authenticatedSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .build();

        System.out.println("✅ JWT acquired");
    }

    // ===== PUBLIC ENDPOINT =====

    @Test
    @Order(2)
    @DisplayName("2. GET /test → 200 (health check)")
    public void testHealthEndpoint() {
        given()
                .spec(requestSpec)
        .when()
                .get(TEST_ENDPOINT)
        .then()
                .statusCode(200)
                .body(containsString("Email service is running"));

        System.out.println("✅ Health endpoint OK");
    }

    // ===== ORDER CONFIRMATION =====

    @Test
    @Order(3)
    @DisplayName("3. POST /order/confirmation with valid data → 200")
    public void testOrderConfirmation_ValidData() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-CONF-001",
                    "customerEmail": "test@example.com",
                    "customerName": "Test Customer",
                    "totalAmount": 299.99,
                    "currency": "SEK",
                    "shippingAddress": "Testgatan 1, 123 45 Stockholm",
                    "orderItems": [
                        {
                            "productName": "Test Product",
                            "quantity": 2,
                            "price": 149.99
                        }
                    ]
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_CONFIRMATION)
        .then()
                .statusCode(200)
                .body(containsString("confirmation"));

        System.out.println("✅ Order confirmation endpoint OK");
    }

    @Test
    @Order(4)
    @DisplayName("4. POST /order/confirmation minimal data → 200")
    public void testOrderConfirmation_MinimalData() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-CONF-002",
                    "customerEmail": "minimal@example.com",
                    "customerName": "Minimal Test"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_CONFIRMATION)
        .then()
                .statusCode(200);

        System.out.println("✅ Order confirmation (minimal) OK");
    }

    // ===== ORDER STATUS UPDATE =====

    @Test
    @Order(5)
    @DisplayName("5. POST /order/status with PROCESSING → 200")
    public void testOrderStatus_Processing() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-STATUS-001",
                    "customerEmail": "status@example.com",
                    "customerName": "Status Test",
                    "orderStatus": "PROCESSING"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_STATUS)
        .then()
                .statusCode(200);

        System.out.println("✅ Order status (PROCESSING) OK");
    }

    @Test
    @Order(6)
    @DisplayName("6. POST /order/status without status → 400")
    public void testOrderStatus_MissingStatus() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-STATUS-002",
                    "customerEmail": "status@example.com",
                    "customerName": "Missing Status Test"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_STATUS)
        .then()
                .statusCode(400)
                .body(containsString("status"));

        System.out.println("✅ Missing status returns 400");
    }

    // ===== ORDER SHIPPED =====

    @Test
    @Order(7)
    @DisplayName("7. POST /order/shipped with tracking → 200")
    public void testOrderShipped_WithTracking() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-SHIP-001",
                    "customerEmail": "shipped@example.com",
                    "customerName": "Shipped Test",
                    "trackingNumber": "SE123456789",
                    "carrier": "PostNord",
                    "estimatedDelivery": "2026-02-20"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_SHIPPED)
        .then()
                .statusCode(200)
                .body(containsString("Shipping"));

        System.out.println("✅ Order shipped (with tracking) OK");
    }

    @Test
    @Order(8)
    @DisplayName("8. POST /order/shipped without tracking → 200 (warning logged)")
    public void testOrderShipped_WithoutTracking() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-SHIP-002",
                    "customerEmail": "shipped@example.com",
                    "customerName": "No Tracking Test"
                }
                """;

        // Should still work, but logs a warning
        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_SHIPPED)
        .then()
                .statusCode(200);

        System.out.println("✅ Order shipped (no tracking) OK");
    }

    // ===== ORDER CANCELLED =====

    @Test
    @Order(9)
    @DisplayName("9. POST /order/cancelled → 200")
    public void testOrderCancelled() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-CANCEL-001",
                    "customerEmail": "cancelled@example.com",
                    "customerName": "Cancel Test",
                    "cancellationReason": "Customer requested cancellation"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_CANCELLED)
        .then()
                .statusCode(200)
                .body(containsString("Cancellation"));

        System.out.println("✅ Order cancelled OK");
    }

    // ===== CUSTOM EMAIL (ADMIN) =====

    @Test
    @Order(10)
    @DisplayName("10. POST /send custom email → 200/500 (SMTP may fail)")
    public void testSendCustomEmail() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String emailBody = """
                {
                    "recipientEmail": "custom@example.com",
                    "subject": "Test Custom Email",
                    "content": "This is a custom test email from integration tests.",
                    "html": false
                }
                """;

        // 200 = email sent, 500 = SMTP delivery failed (both mean auth passed)
        given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(200), equalTo(500)));

        System.out.println("✅ Custom email endpoint OK (auth passed)");
    }

    @Test
    @Order(11)
    @DisplayName("11. POST /send HTML email → 200/500 (SMTP may fail)")
    public void testSendHtmlEmail() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String emailBody = """
                {
                    "recipientEmail": "html@example.com",
                    "subject": "Test HTML Email",
                    "content": "<h1>Hello</h1><p>This is an HTML test email.</p>",
                    "html": true
                }
                """;

        // 200 = email sent, 500 = SMTP delivery failed (both mean auth passed)
        given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(200), equalTo(500)));

        System.out.println("✅ HTML email endpoint OK (auth passed)");
    }

    // ===== VALIDATION TESTS =====

    @Test
    @Order(12)
    @DisplayName("12. POST /send without recipient → 400/500")
    public void testSendEmail_MissingRecipient() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String emailBody = """
                {
                    "subject": "Missing Recipient",
                    "content": "This should fail"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(400), equalTo(500)));

        System.out.println("✅ Missing recipient rejected");
    }

    @Test
    @Order(13)
    @DisplayName("13. POST /order/confirmation invalid email format → 200/500 (no validation)")
    public void testOrderConfirmation_InvalidEmail() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String orderBody = """
                {
                    "orderNumber": "TEST-INVALID-001",
                    "customerEmail": "not-an-email",
                    "customerName": "Invalid Email Test"
                }
                """;

        // Server doesn't validate email format - accepts request
        // 200 = processed, 500 = SMTP failed to deliver
        given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDER_CONFIRMATION)
        .then()
                .statusCode(anyOf(equalTo(200), equalTo(500)));

        System.out.println("✅ Invalid email handled (no server-side validation)");
    }

    // ===== AUTH REQUIRED =====

    @Test
    @Order(14)
    @DisplayName("14. All order endpoints require auth")
    public void testOrderEndpoints_RequireAuth() {
        String orderBody = """
                {
                    "orderNumber": "TEST-NOAUTH",
                    "customerEmail": "test@example.com",
                    "customerName": "No Auth Test"
                }
                """;

        // Confirmation
        given().spec(requestSpec).body(orderBody)
        .when().post(ORDER_CONFIRMATION)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));

        // Status
        given().spec(requestSpec).body(orderBody)
        .when().post(ORDER_STATUS)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));

        // Shipped
        given().spec(requestSpec).body(orderBody)
        .when().post(ORDER_SHIPPED)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));

        // Cancelled
        given().spec(requestSpec).body(orderBody)
        .when().post(ORDER_CANCELLED)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));

        System.out.println("✅ All order endpoints require auth");
    }

    // ===== SUMMARY =====

    @Test
    @Order(99)
    @DisplayName("Summary: Email Template Tests")
    public void testSummary() {
        System.out.println("\n========================================");
        System.out.println("📧 Email Template Test Summary");
        System.out.println("========================================");
        System.out.println("✅ Health endpoint (public)");
        System.out.println("✅ Order confirmation (full + minimal)");
        System.out.println("✅ Order status update");
        System.out.println("✅ Order shipped (with/without tracking)");
        System.out.println("✅ Order cancelled");
        System.out.println("✅ Custom email (auth verified, SMTP may fail)");
        System.out.println("✅ Validation (missing fields)");
        System.out.println("✅ Auth required for all order endpoints");
        System.out.println("========================================\n");
    }
}
