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
 * Integration tests for JWT Authentication (v1.3)
 *
 * Endpoints:
 * - /api/images  → image-service
 * - /api/auth/   → admin-service
 */
@DisplayName("Image Service - JWT Auth Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String IMAGES_ENDPOINT = "/api/images";

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

        System.out.println("🚀 Image tests: " + BASE_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Get JWT token")
    public void testLogin_ReturnsToken() {
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
    @DisplayName("GET /api/images/{id} without token (public)")
    public void testGetImage_NoToken_Returns200() {
        given().spec(requestSpec)
                .when().get(IMAGES_ENDPOINT + "/1")
                .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/images/category/{cat} without token (public)")
    public void testGetImagesByCategory_NoToken_Returns200() {
        given().spec(requestSpec)
                .when().get(IMAGES_ENDPOINT + "/category/test")
                .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/images/upload WITHOUT token → 401/403")
    public void testUploadImage_NoToken_Unauthorized() {
        given()
                .multiPart("file", "dummy", "test content".getBytes())
                .multiPart("altText", "Test")
                .when()
                .post(IMAGES_ENDPOINT + "/upload")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/images/{id} WITHOUT token → 401/403")
    public void testDeleteImage_NoToken_Unauthorized() {
        given().spec(requestSpec)
                .when().delete(IMAGES_ENDPOINT + "/1")
                .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(6)
    @DisplayName("PUT /api/images/{id} WITHOUT token → 401/403")
    public void testUpdateImage_NoToken_Unauthorized() {
        given().spec(requestSpec)
                .param("altText", "Updated")
                .when()
                .put(IMAGES_ENDPOINT + "/1")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/images/{id} WITH token → auth passes")
    public void testDeleteImage_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(IMAGES_ENDPOINT + "/99999")
                .then()
                .statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(8)
    @DisplayName("Invalid token → 401/403")
    public void testUpload_InvalidToken_Unauthorized() {
        given()
                .header("Authorization", "Bearer invalid.token")
                .multiPart("file", "dummy", "test".getBytes())
                .multiPart("altText", "Test")
                .when()
                .post(IMAGES_ENDPOINT + "/upload")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}