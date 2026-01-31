package com.perfect8.blog.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for JWT Authentication (p8.rantila.com)
 * v1.3 - Industristandard auth
 * 
 * Endpoints:
 * - /api/posts  → blog-service
 * - /api/auth/  → admin-service
 */
@DisplayName("Blog Service - JWT Auth Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String POSTS_ENDPOINT = "/api/posts";

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

        System.out.println("🚀 Blog tests: " + BASE_URL);
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
    @DisplayName("GET /api/posts without token (public)")
    public void testGetPosts_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/posts WITHOUT token → 401/403")
    public void testCreatePost_NoToken_Unauthorized() {
        String postBody = """
                {"title": "Test", "content": "Test", "slug": "test"}
                """;

        given().spec(requestSpec).body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/posts/{id} WITHOUT token → 401/403")
    public void testUpdatePost_NoToken_Unauthorized() {
        String updateBody = """
                {"title": "Updated", "content": "Updated"}
                """;

        given().spec(requestSpec).body(updateBody)
        .when().put(POSTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/posts/{id} WITHOUT token → 401/403")
    public void testDeletePost_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().delete(POSTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/posts WITH token → auth passes")
    public void testCreatePost_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String postBody = """
                {"title": "REST Test", "content": "Test", "slug": "rest-test"}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/posts/{id} WITH token → auth passes")
    public void testDeletePost_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().delete(POSTS_ENDPOINT + "/99999")
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(8)
    @DisplayName("Invalid token → 401/403")
    public void testPost_InvalidToken_Unauthorized() {
        String postBody = """
                {"title": "Test", "content": "Test"}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
                .body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
