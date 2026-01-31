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
 * - /email/**    → email-service (admin only)
 * - /api/auth/   → admin-service
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

    // Endpoints (v1.3)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String SEND_ENDPOINT = "/email/send";

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("🚀 Email LIVE tests: " + BASE_URL);
        System.out.println("📍 Email endpoint: /email/**");
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
                    .contentType(ContentType.JSON)
                    .body(loginBody)
            .when()
                    .post(BASE_URL + LOGIN_ENDPOINT)
            .then()
                    .statusCode(200)
                    .extract().response();

            return response.jsonPath().getString("accessToken");
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    @Order(1)
    @DisplayName("LIVE: Send welcome email")
    public void testSendWelcomeEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 Test - Welcome Email",
                    "templateName": "welcome",
                    "variables": {
                        "customerName": "Test User",
                        "loginUrl": "https://p8.rantila.com/login"
                    }
                }
                """, RECIPIENT_EMAIL);

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Welcome email sent to " + RECIPIENT_EMAIL);
    }

    @Test
    @Order(2)
    @DisplayName("LIVE: Send order confirmation email")
    public void testSendOrderConfirmationEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 Test - Order Confirmation",
                    "templateName": "order-confirmation",
                    "variables": {
                        "orderNumber": "TEST-12345",
                        "customerName": "Test User",
                        "totalAmount": "99.99",
                        "currency": "SEK"
                    }
                }
                """, RECIPIENT_EMAIL);

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Order confirmation sent to " + RECIPIENT_EMAIL);
    }

    @Test
    @Order(3)
    @DisplayName("LIVE: Send plain text email")
    public void testSendPlainTextEmail() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS is false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 Integration Test",
                    "body": "This is a test email from Perfect8 integration tests. Time: %s"
                }
                """, RECIPIENT_EMAIL, java.time.LocalDateTime.now());

        given().spec(authenticatedSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(is(200), is(201), is(202)));

        System.out.println("✅ Plain text email sent to " + RECIPIENT_EMAIL);
    }
}
