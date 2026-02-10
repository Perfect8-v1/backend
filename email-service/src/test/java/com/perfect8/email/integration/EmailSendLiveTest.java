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
 * LIVE Email Integration Tests (v1.3)
 * 
 * ⚠️ WARNING: These tests send REAL emails when SEND_LIVE_EMAILS = true
 * 
 * Endpoints (via Gateway):
 * - /email/api/email/**  → email-service (admin only)
 * - /api/auth/           → admin-service
 */
@DisplayName("Email Service - Live Email Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailSendLiveTest {

    // ⚠️ SET TO true TO SEND REAL EMAILS
    private static final boolean SEND_LIVE_EMAILS = false;

    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";
    private static final String RECIPIENT_EMAIL = "taxiberglund@gmail.com";

    // Endpoints (v1.3 - korrekta paths)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String SEND_ENDPOINT = "/email/api/email/send";
    private static final String ORDER_CONFIRMATION_ENDPOINT = "/email/api/email/order/confirmation";
    private static final String SHIPPING_NOTIFICATION_ENDPOINT = "/email/api/email/order/shipping";

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("🚀 Email LIVE tests: " + BASE_URL);
        System.out.println("📧 Send endpoint: " + SEND_ENDPOINT);
        System.out.println("⚠️ SEND_LIVE_EMAILS = " + SEND_LIVE_EMAILS);

        jwtToken = getAuthToken();

        if (jwtToken != null) {
            authenticatedSpec = new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .build();
            System.out.println("✅ JWT acquired");
        }
    }

    private static String getAuthToken() {
        try {
            String loginBody = """
                    {"email": "%s", "password": "%s"}
                    """.formatted(TEST_EMAIL, TEST_PASSWORD);

            Response response = given()
                    .relaxedHTTPSValidation()
                    .contentType(ContentType.JSON)
                    .body(loginBody)
            .when()
                    .post(BASE_URL + LOGIN_ENDPOINT)
            .then()
                    .statusCode(200)
                    .extract().response();

            return response.jsonPath().getString("accessToken");
        } catch (Exception e) {
            System.out.println("⚠️ Could not get auth token: " + e.getMessage());
            return null;
        }
    }

    @Test
    @Order(1)
    @DisplayName("LIVE: Send plain text email")
    public void testSendPlainTextEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "recipientEmail": "%s",
                    "subject": "Perfect8 Integration Test",
                    "content": "This is a test email from Perfect8 integration tests. Time: %s"
                }
                """, RECIPIENT_EMAIL, java.time.LocalDateTime.now());

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Plain text email sent to " + RECIPIENT_EMAIL);
    }

    @Test
    @Order(2)
    @DisplayName("LIVE: Send order confirmation email")
    public void testSendOrderConfirmationEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "orderNumber": "TEST-12345",
                    "customerEmail": "%s",
                    "customerName": "Test User",
                    "totalAmount": 99.99,
                    "currency": "SEK",
                    "shippingAddress": "Testgatan 1, 123 45 Stockholm"
                }
                """, RECIPIENT_EMAIL);

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(ORDER_CONFIRMATION_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Order confirmation sent to " + RECIPIENT_EMAIL);
    }

    @Test
    @Order(3)
    @DisplayName("LIVE: Send shipping notification email")
    public void testSendShippingNotificationEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "orderNumber": "TEST-12345",
                    "customerEmail": "%s",
                    "customerName": "Test User",
                    "trackingNumber": "SE123456789",
                    "carrier": "PostNord",
                    "estimatedDelivery": "2026-02-15"
                }
                """, RECIPIENT_EMAIL);

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SHIPPING_NOTIFICATION_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Shipping notification sent to " + RECIPIENT_EMAIL);
    }

    // ===== DRY RUN TESTS (always run, verify auth works) =====

    @Test
    @Order(10)
    @DisplayName("DRY RUN: Verify send endpoint accepts valid request (auth passes)")
    public void testDryRun_SendEndpoint_AuthPasses() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = """
                {
                    "recipientEmail": "test@example.com",
                    "subject": "Dry Run Test",
                    "content": "This should not be sent"
                }
                """;

        // Just verify auth passes (not 401/403)
        // May get 400 validation error or 200 success, but NOT auth error
        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));

        System.out.println("✅ DRY RUN: Auth passed for send endpoint");
    }

    @Test
    @Order(11)
    @DisplayName("DRY RUN: Verify order confirmation endpoint accepts valid request")
    public void testDryRun_OrderConfirmation_AuthPasses() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = """
                {
                    "orderNumber": "DRY-RUN-001",
                    "customerEmail": "test@example.com",
                    "customerName": "Dry Run Test"
                }
                """;

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(ORDER_CONFIRMATION_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));

        System.out.println("✅ DRY RUN: Auth passed for order confirmation endpoint");
    }
}
