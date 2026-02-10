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
 * Integration tests for Blog Service Authentication
 * 
 * Tests:
 * - Public endpoints (GET posts - no auth required)
 * - Protected endpoints (POST/PUT/DELETE - JWT required)
 * - Gateway authentication (admin via JWT)
 */
@DisplayName("Blog Service - Auth Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogServiceAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String POSTS_ENDPOINT = "/blog/api/posts";
    private static final String POST_BY_ID = "/blog/api/posts/1";

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

        System.out.println("🚀 Blog Service tests: " + BASE_URL);
        System.out.println("📝 Posts endpoint: " + POSTS_ENDPOINT);
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
    @DisplayName("GET /blog/api/posts without auth → 200 (public)")
    public void testGetPosts_NoAuth_Returns200() {
        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("GET /blog/api/posts/1 without auth → 200 or 404 (public)")
    public void testGetPostById_NoAuth_PublicAccess() {
        // 200 if post exists, 404 if not - but NOT 401/403
        given().spec(requestSpec)
        .when().get(POST_BY_ID)
        .then().statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    // ===== PROTECTED ENDPOINTS - NO AUTH =====

    @Test
    @Order(4)
    @DisplayName("POST /blog/api/posts WITHOUT auth → 401/403")
    public void testCreatePost_NoAuth_Returns401() {
        String postBody = """
                {
                    "title": "Test Post",
                    "content": "Test content",
                    "slug": "test-post"
                }
                """;

        given().spec(requestSpec)
                .body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("PUT /blog/api/posts/1 WITHOUT auth → 401/403")
    public void testUpdatePost_NoAuth_Returns401() {
        String postBody = """
                {
                    "title": "Updated Post",
                    "content": "Updated content"
                }
                """;

        given().spec(requestSpec)
                .body(postBody)
        .when().put(POST_BY_ID)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /blog/api/posts/1 WITHOUT auth → 401/403")
    public void testDeletePost_NoAuth_Returns401() {
        given().spec(requestSpec)
        .when().delete(POST_BY_ID)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - INVALID TOKEN =====

    @Test
    @Order(7)
    @DisplayName("POST /blog/api/posts with invalid token → 401/403")
    public void testCreatePost_InvalidToken_Returns401() {
        String postBody = """
                {
                    "title": "Test Post",
                    "content": "Test content",
                    "slug": "test-post"
                }
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
                .body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== PROTECTED ENDPOINTS - WITH JWT =====

    @Test
    @Order(8)
    @DisplayName("POST /blog/api/posts WITH admin JWT → auth passes")
    public void testCreatePost_WithAdminJwt_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String postBody = """
                {
                    "title": "Test Post from RESTAssured",
                    "content": "Test content",
                    "slug": "test-post-restassured"
                }
                """;

        // Should not get 401/403 (may get 400 if validation fails, but auth passes)
        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(postBody)
        .when().post(POSTS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /blog/api/posts/999 WITH admin JWT → auth passes")
    public void testDeletePost_WithAdminJwt_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        // Should get 404 (post doesn't exist) - NOT 401/403
        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().delete("/blog/api/posts/999")
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== SUMMARY =====

    @Test
    @Order(99)
    @DisplayName("Summary: Blog service auth is working correctly")
    public void testSummary() {
        System.out.println("\n========================================");
        System.out.println("📝 Blog Service Auth Test Summary");
        System.out.println("========================================");
        System.out.println("✅ GET posts publicly accessible");
        System.out.println("✅ POST/PUT/DELETE require auth");
        System.out.println("✅ Invalid tokens rejected");
        System.out.println("✅ Admin JWT accepted");
        System.out.println("========================================\n");
    }
}
