package com.perfect8.email.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * LIVE Email Test - Sends REAL emails!
 * 
 * ⚠️ WARNING: This test sends ACTUAL emails to real addresses!
 * 
 * HOW TO RUN:
 * 1. Change SEND_LIVE_EMAILS = true (line 32)
 * 2. Run tests from IntelliJ
 * 3. Check inbox for taxiberglund@gmail.com
 * 4. Change back to false when done!
 */
@DisplayName("Email Service - LIVE Email Tests ⚠️")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailSendLiveTest {

    // ╔════════════════════════════════════════════════════════════════╗
    // ║  ÄNDRA DENNA TILL true FÖR ATT SKICKA RIKTIGA EMAILS          ║
    // ╚════════════════════════════════════════════════════════════════╝
    private static final boolean SEND_LIVE_EMAILS = false;

    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int EMAIL_PORT = 8083;
    private static final String EMAIL_API_KEY = "p8email_9Bz4cD7gJ1kQ6wY3";

    // Test recipient - Magnus test address
    private static final String TEST_RECIPIENT = "taxiberglund@gmail.com";

    // Endpoints
    private static final String SEND_ENDPOINT = "/api/email/send";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + EMAIL_PORT;
        RestAssured.baseURI = fullUrl;

        authenticatedSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .addHeader("X-API-Key", EMAIL_API_KEY)
                .build();

        System.out.println("🚀 LIVE Email tests configured for: " + fullUrl);
        System.out.println("📧 Test recipient: " + TEST_RECIPIENT);
        
        if (SEND_LIVE_EMAILS) {
            System.out.println("⚠️  SEND_LIVE_EMAILS = true → REAL EMAILS WILL BE SENT!");
        } else {
            System.out.println("ℹ️  SEND_LIVE_EMAILS = false → Tests will be skipped");
        }
    }

    // ==================== LIVE EMAIL TESTS ====================

    @Test
    @Order(1)
    @DisplayName("LIVE: Send simple test email")
    public void testSendSimpleEmail_LIVE() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "Skipped: SEND_LIVE_EMAILS = false");

        System.out.println("\n📧 SENDING LIVE EMAIL to: " + TEST_RECIPIENT);

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 REST Assured Test - %s",
                    "body": "This is a test email sent from REST Assured integration tests.\\n\\nTimestamp: %s\\n\\nIf you received this, the email service is working correctly!",
                    "htmlBody": false
                }
                """,
                TEST_RECIPIENT,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                java.time.Instant.now()
        );

        Response response = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201), is(202)))
                .extract()
                .response();

        System.out.println("   ✅ Email sent! Status: " + response.getStatusCode());
        System.out.println("   📬 Check inbox: " + TEST_RECIPIENT);
        System.out.println("   Response: " + response.asString());
    }

    @Test
    @Order(2)
    @DisplayName("LIVE: Send HTML formatted email")
    public void testSendHtmlEmail_LIVE() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "Skipped: SEND_LIVE_EMAILS = false");

        System.out.println("\n📧 SENDING LIVE HTML EMAIL to: " + TEST_RECIPIENT);

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h1 style="color: #4CAF50;">✅ Perfect8 Email Test</h1>
                    <p>This is an <strong>HTML formatted</strong> test email.</p>
                    <hr>
                    <table style="border-collapse: collapse;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Service:</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">email-service</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Port:</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">8083</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Branch:</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">Plain (API-key auth)</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Timestamp:</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                    <hr>
                    <p style="color: #666; font-size: 12px;">
                        Sent by REST Assured integration tests
                    </p>
                </body>
                </html>
                """.formatted(timestamp);

        // Escape for JSON
        String escapedHtml = htmlContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Perfect8 HTML Test - %s",
                    "body": "%s",
                    "htmlBody": true
                }
                """,
                TEST_RECIPIENT,
                timestamp,
                escapedHtml
        );

        Response response = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201), is(202)))
                .extract()
                .response();

        System.out.println("   ✅ HTML Email sent! Status: " + response.getStatusCode());
        System.out.println("   📬 Check inbox: " + TEST_RECIPIENT);
    }

    @Test
    @Order(3)
    @DisplayName("LIVE: Send order confirmation template")
    public void testSendOrderConfirmation_LIVE() {
        Assumptions.assumeTrue(SEND_LIVE_EMAILS, "Skipped: SEND_LIVE_EMAILS = false");

        System.out.println("\n📧 SENDING ORDER CONFIRMATION to: " + TEST_RECIPIENT);

        String emailBody = String.format("""
                {
                    "to": "%s",
                    "subject": "Order Confirmation #TEST-12345",
                    "templateName": "order-confirmation",
                    "templateData": {
                        "orderId": "TEST-12345",
                        "customerName": "Magnus Test",
                        "orderDate": "%s",
                        "totalAmount": "1299.00",
                        "currency": "SEK"
                    }
                }
                """,
                TEST_RECIPIENT,
                java.time.LocalDate.now()
        );

        Response response = given()
                .spec(authenticatedSpec)
                .body(emailBody)
        .when()
                .post(SEND_ENDPOINT)
        .then()
                .extract()
                .response();

        System.out.println("   Status: " + response.getStatusCode());
        System.out.println("   Response: " + response.asString());
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            System.out.println("   ✅ Order confirmation sent!");
        } else {
            System.out.println("   ⚠️ Check if template 'order-confirmation' exists");
        }
    }

    // ==================== INFO TEST (always runs) ====================

    @Test
    @Order(99)
    @DisplayName("INFO: Current SEND_LIVE_EMAILS status")
    public void infoCurrentStatus() {
        System.out.println("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║           EMAIL LIVE TEST STATUS                            ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  SEND_LIVE_EMAILS = %-5s                                    ║
                ║                                                              ║
                ║  To send real emails:                                       ║
                ║  1. Change line 32: SEND_LIVE_EMAILS = true                 ║
                ║  2. Run tests                                               ║
                ║  3. Check inbox: taxiberglund@gmail.com                     ║
                ║  4. Change back to false when done!                         ║
                ║                                                              ║
                ╚══════════════════════════════════════════════════════════════╝
                """.formatted(SEND_LIVE_EMAILS));
    }
}
