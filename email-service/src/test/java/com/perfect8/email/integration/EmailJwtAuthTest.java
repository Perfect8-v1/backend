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
 * Integration tests for JWT Authentication (v1.3)
 * 
 * Endpoints (via Gateway):
 * - /email/**    → email-service (admin only)
 * - /api/auth/   → admin-service
 */
@DisplayName("Email Service - JWT Auth Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 med service-prefix)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String SEND_ENDPOINT = "/email/send";
    private static final String LOGS_ENDPOINT = "/email/logs";
    private static final String TEMPLATES_ENDPOINT = "/email/templates";

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

        System.out.println("🚀 Email tests: " + BASE_URL);
        System.out.println("📍 Email endpoint: /email/**");
    }

    @Test
    @Order(1)
    @DisplayName("Get JWT token")
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

    @Test
    @Order(2)
    @DisplayName("POST /email/send WITHOUT token → 401/403")
    public void testSendEmail_NoToken_Unauthorized() {
        String emailBody = """
                {"to": "test@example.com", "subject": "Test", "body": "Test"}
                """;

        given().spec(requestSpec).body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /email/logs WITHOUT token → 401/403")
    public void testGetLogs_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(LOGS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /email/templates WITHOUT token → 401/403")
    public void testGetTemplates_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(TEMPLATES_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("POST /email/send WITH token → auth passes")
    public void testSendEmail_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String emailBody = """
                {"to": "invalid", "subject": "", "body": ""}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /email/logs WITH token → auth passes")
    public void testGetLogs_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(LOGS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(7)
    @DisplayName("GET /email/templates WITH token → auth passes")
    public void testGetTemplates_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(TEMPLATES_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(8)
    @DisplayName("Invalid token → 401/403")
    public void testSendEmail_InvalidToken_Unauthorized() {
        String emailBody = """
                {"to": "test@example.com", "subject": "Test", "body": "Test"}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
                .body(emailBody)
        .when().post(SEND_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
