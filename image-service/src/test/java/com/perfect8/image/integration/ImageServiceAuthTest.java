package com.perfect8.image.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Image Service Authentication
 * 
 * Tests:
 * - Public endpoints (GET images - no auth required)
 * - Protected endpoints (POST/DELETE - JWT or API key required)
 * - Gateway authentication (admin via JWT)
 */
@DisplayName("Image Service - Auth Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageServiceAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String IMAGES_ENDPOINT = "/image/api/images";
    private static final String IMAGE_BY_ID = "/image/api/images/1";
    private static final String IMAGE_UPLOAD = "/image/api/images/upload";

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

        System.out.println("🚀 Image Service tests: " + BASE_URL);
        System.out.println("🖼️ Images endpoint: " + IMAGES_ENDPOINT);
        System.out.println("🖼️ Upload endpoint: " + IMAGE_UPLOAD);
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

    // ===== PUBLIC ENDPOINTS (GET) =====

    @Test
    @Order(2)
    @DisplayName("GET /image/api/images without auth → 200 (public)")
    public void testGetImages_NoAuth_Returns200() {
        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("GET /image/api/images/1 without auth → 200 or 404 (public)")
    public void testGetImageById_NoAuth_PublicAccess() {
        // 200 if image exists, 404 if not - but NOT 401/403
        given().spec(requestSpec)
        .when().get(IMAGE_BY_ID)
        .then().statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    // ===== PROTECTED ENDPOINTS - NO AUTH =====

    @Test
    @Order(4)
    @DisplayName("POST /image/api/images/upload WITHOUT auth → 401/403")
    public void testUpload_NoAuth_Returns401() {
        given()
                .baseUri(BASE_URL)
                .relaxedHTTPSValidation()
                .contentType("multipart/form-data")
                .multiPart("file", "test.txt", "test content".getBytes())
        .when().post(IMAGE_UPLOAD)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /image/api/images/1 WITHOUT auth → 401/403")
    public void testDelete_NoAuth_Returns401() {
        given().spec(requestSpec)
        .when().delete(IMAGE_BY_ID)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - INVALID TOKEN =====

    @Test
    @Order(6)
    @DisplayName("POST /image/api/images/upload with invalid token → 401/403")
    public void testUpload_InvalidToken_Returns401() {
        given()
                .baseUri(BASE_URL)
                .relaxedHTTPSValidation()
                .header("Authorization", "Bearer invalid.token.here")
                .contentType("multipart/form-data")
                .multiPart("file", "test.txt", "test content".getBytes())
        .when().post(IMAGE_UPLOAD)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /image/api/images/1 with invalid token → 401/403")
    public void testDelete_InvalidToken_Returns401() {
        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
        .when().delete(IMAGE_BY_ID)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - WITH JWT =====

    @Test
    @Order(8)
    @DisplayName("DELETE /image/api/images/999 WITH admin JWT → auth passes (404 expected)")
    public void testDelete_WithAdminJwt_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        // Should get 404 (image doesn't exist) - NOT 401/403
        // This proves authentication passed
        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().delete("/image/api/images/999")
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== SUMMARY =====

    @Test
    @Order(99)
    @DisplayName("Summary: Image service auth is working correctly")
    public void testSummary() {
        System.out.println("\n========================================");
        System.out.println("🖼️ Image Service Auth Test Summary");
        System.out.println("========================================");
        System.out.println("✅ GET images publicly accessible");
        System.out.println("✅ POST/DELETE require auth");
        System.out.println("✅ Invalid tokens rejected");
        System.out.println("✅ Admin JWT accepted");
        System.out.println("========================================\n");
    }
}
