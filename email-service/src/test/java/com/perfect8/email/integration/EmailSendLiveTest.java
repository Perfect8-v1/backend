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
 * LIVE Email Test - Sends REAL emails! (v1.3)
 * 
 * ⚠️ WARNING: This test sends ACTUAL emails!
 * 
 * HOW TO RUN:
 * 1. Change SEND_LIVE_EMAILS = true
 * 2. Run tests
 * 3. Check inbox
 * 4. Change back to false!
 */
@DisplayName("Email Service - LIVE Tests ⚠️ (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailSendLiveTest {

    // ╔════════════════════════════════════════════════════════════════╗
    // ║  ÄNDRA TILL true FÖR ATT SKICKA RIKTIGA EMAILS                ║
    // ╚════════════════════════════════════════════════════════════════╝
    private static final boolean SEND_LIVE_EMAILS = false;

    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";
    private static final String TEST_RECIPIENT = "taxiberglund@gmail.com";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String SEND_ENDPOINT = "/api/email/send";

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("🚀 LIVE Email tests: " + BASE_URL);
        System.out.println("📧 Recipient: " + TEST_RECIPIENT);
        
        if (SEND_LIVE_EMAILS) {
            System.out.println("⚠️ SEND_LIVE_EMAILS = true → REAL EMAILS!");
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
        } else {
            System.out.println("ℹ️ SEND_LIVE_EMAILS = false → Tests skipped");
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
    @DisplayName("LIVE: Send simple test email")
    public void testSendSimpleEmail_LIVE() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS = false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 Test (v1.3) - %s",
                    "body": "Test email from REST Assured.\\nTimestamp: %s",
                    "htmlBody": false
                }
                """, TEST_RECIPIENT, timestamp, java.time.Instant.now());

        Response response = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201), is(202)))
                .extract().response();

        System.out.println("✅ Email sent! Status: " + response.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("LIVE: Send HTML formatted email")
    public void testSendHtmlEmail_LIVE() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "SEND_LIVE_EMAILS = false");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String htmlContent = """
                <html>
                <body style="font-family: Arial;">
                    <h1 style="color: #4CAF50;">✅ Perfect8 Email Test (v1.3)</h1>
                    <p>This is an <strong>HTML</strong> test email.</p>
                    <p>Timestamp: %s</p>
                </body>
                </html>
                """.formatted(timestamp);

        String escapedHtml = htmlContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 HTML Test (v1.3) - %s",
                    "body": "%s",
                    "htmlBody": true
                }
                """, TEST_RECIPIENT, timestamp, escapedHtml);

        Response response = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201), is(202)))
                .extract().response();

        System.out.println("✅ HTML Email sent! Status: " + response.getStatusCode());
    }

    @Test
    @Order(99)
    @DisplayName("INFO: Current SEND_LIVE_EMAILS status")
    public void infoCurrentStatus() {
        System.out.println("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║           EMAIL LIVE TEST STATUS (v1.3)                     ║
                ╠══════════════════════════════════════════════════════════════╣
                ║  SEND_LIVE_EMAILS = %-5s                                    ║
                ║  To send emails: Change line 26 to true                     ║
                ╚══════════════════════════════════════════════════════════════╝
                """.formatted(SEND_LIVE_EMAILS));
    }
}
