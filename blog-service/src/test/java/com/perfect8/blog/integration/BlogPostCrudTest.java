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
 * Integration tests for Blog Post CRUD (v1.3)
 * 
 * Endpoints:
 * - /api/posts  → blog-service
 * - /api/auth/  → admin-service
 */
@DisplayName("Blog Service - Post CRUD Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogPostCrudTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String POSTS_ENDPOINT = "/api/posts";

    private static Long createdPostId;
    private static String createdPostSlug;
    private static final String TEST_SLUG = "rest-crud-test-" + System.currentTimeMillis();

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
    @DisplayName("CREATE: POST /api/posts")
    public void testCreatePost() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String postBody = String.format("""
                {
                    "title": "REST CRUD Test Post",
                    "content": "Created by integration test",
                    "slug": "%s",
                    "published": true
                }
                """, TEST_SLUG);

        Response response = given()
                .spec(authenticatedSpec)
                .body(postBody)
        .when()
                .post(POSTS_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("title", equalTo("REST CRUD Test Post"))
                .extract().response();

        createdPostId = response.jsonPath().getLong("postId");
        createdPostSlug = response.jsonPath().getString("slug");
        System.out.println("✅ Created post ID: " + createdPostId);
    }

    @Test
    @Order(2)
    @DisplayName("READ: GET /api/posts (public)")
    public void testGetAllPosts() {
        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT)
        .then().statusCode(200).body("content", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("READ: GET /api/posts/{slug}")
    public void testGetPostBySlug() {
        Assumptions.assumeTrue(createdPostSlug != null, "No post created");

        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then().statusCode(200).body("slug", equalTo(createdPostSlug));
    }

    @Test
    @Order(4)
    @DisplayName("READ: GET /api/posts with pagination")
    public void testGetPostsWithPagination() {
        given().spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 5)
        .when().get(POSTS_ENDPOINT)
        .then().statusCode(200).body("content", notNullValue());
    }

    @Test
    @Order(5)
    @DisplayName("UPDATE: PUT /api/posts/{id}")
    public void testUpdatePost() {
        Assumptions.assumeTrue(createdPostId != null, "No post created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String updateBody = """
                {
                    "title": "UPDATED: REST CRUD Test Post",
                    "content": "Updated by integration test",
                    "published": true
                }
                """;

        Response response = given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/" + createdPostId)
        .then()
                .statusCode(200)
                .body("title", containsString("UPDATED"))
                .extract().response();

        createdPostSlug = response.jsonPath().getString("slug");
    }

    @Test
    @Order(6)
    @DisplayName("UPDATE: Verify update worked")
    public void testVerifyUpdate() {
        Assumptions.assumeTrue(createdPostSlug != null, "No post created");

        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then().statusCode(200).body("title", containsString("UPDATED"));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE: DELETE /api/posts/{id}")
    public void testDeletePost() {
        Assumptions.assumeTrue(createdPostId != null, "No post created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().delete(POSTS_ENDPOINT + "/" + createdPostId)
        .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(8)
    @DisplayName("DELETE: Verify delete worked (404)")
    public void testVerifyDelete() {
        Assumptions.assumeTrue(createdPostSlug != null, "No post created");

        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then().statusCode(404);
    }

    @Test
    @Order(9)
    @DisplayName("ERROR: GET non-existent post → 404")
    public void testGetNonExistentPost() {
        given().spec(requestSpec)
        .when().get(POSTS_ENDPOINT + "/this-slug-does-not-exist-xyz")
        .then().statusCode(404);
    }

    @Test
    @Order(10)
    @DisplayName("ERROR: UPDATE non-existent post → 404")
    public void testUpdateNonExistentPost() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String updateBody = """
                {"title": "Should fail", "content": "Should fail"}
                """;

        given().spec(authenticatedSpec).body(updateBody)
        .when().put(POSTS_ENDPOINT + "/99999999")
        .then().statusCode(404);
    }
}
