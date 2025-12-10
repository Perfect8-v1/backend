package com.perfect8.blog.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Blog Post CRUD operations (Plain branch)
 * 
 * Tests the complete lifecycle:
 * 1. CREATE - POST /api/v1/posts
 * 2. READ   - GET /api/v1/posts, GET /api/v1/posts/{slug}
 * 3. UPDATE - PUT /api/v1/posts/{id}
 * 4. DELETE - DELETE /api/v1/posts/{id}
 * 
 * Uses @TestMethodOrder to run tests in sequence (CREATE → READ → UPDATE → DELETE)
 */
@DisplayName("Blog Service - Post CRUD Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogPostCrudTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int BLOG_PORT = 8082;
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";

    // Endpoints
    private static final String POSTS_ENDPOINT = "/api/v1/posts";

    // Store created post data for subsequent tests
    private static Long createdPostId;
    private static String createdPostSlug;

    // Unique slug to avoid conflicts
    private static final String TEST_SLUG = "rest-assured-crud-test-" + System.currentTimeMillis();

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + BLOG_PORT;
        RestAssured.baseURI = fullUrl;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        authenticatedSpec = new RequestSpecBuilder()
                .setBaseUri(fullUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .addHeader("X-API-Key", BLOG_API_KEY)
                .build();

        System.out.println("🚀 Blog CRUD tests configured for: " + fullUrl);
        System.out.println("📝 Test slug: " + TEST_SLUG);
    }

    // ==================== CREATE ====================

    @Test
    @Order(1)
    @DisplayName("CREATE: POST /api/v1/posts should create new post")
    public void testCreatePost() {
        System.out.println("\n🧪 Testing: CREATE post");

        String postBody = String.format("""
                {
                    "title": "REST Assured CRUD Test Post",
                    "content": "This post was created by REST Assured integration tests. Testing the full CRUD lifecycle.",
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
                .body("title", equalTo("REST Assured CRUD Test Post"))
                .body("slug", equalTo(TEST_SLUG))
                .extract()
                .response();

        // Store ID and slug for subsequent tests
        createdPostId = response.jsonPath().getLong("postId");
        createdPostSlug = response.jsonPath().getString("slug");

        System.out.println("   ✅ Created post with ID: " + createdPostId);
        System.out.println("   ✅ Slug: " + createdPostSlug);
        logTestResult("CREATE post", true);
    }

    // ==================== READ ====================

    @Test
    @Order(2)
    @DisplayName("READ: GET /api/v1/posts should list posts (public)")
    public void testGetAllPosts() {
        System.out.println("\n🧪 Testing: READ all posts (public)");

        given()
                .spec(requestSpec)  // No auth needed for GET
        .when()
                .get(POSTS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("content.size()", greaterThanOrEqualTo(0));

        logTestResult("READ all posts", true);
    }

    @Test
    @Order(3)
    @DisplayName("READ: GET /api/v1/posts/{slug} should return specific post")
    public void testGetPostBySlug() {
        System.out.println("\n🧪 Testing: READ post by slug");

        // Skip if create failed
        Assumptions.assumeTrue(createdPostSlug != null, "Skipped: No post was created");

        given()
                .spec(requestSpec)  // No auth needed for GET
        .when()
                .get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then()
                .statusCode(200)
                .body("slug", equalTo(createdPostSlug))
                .body("title", equalTo("REST Assured CRUD Test Post"));

        System.out.println("   ✅ Found post with slug: " + createdPostSlug);
        logTestResult("READ post by slug", true);
    }

    @Test
    @Order(4)
    @DisplayName("READ: GET /api/v1/posts with pagination")
    public void testGetPostsWithPagination() {
        System.out.println("\n🧪 Testing: READ posts with pagination");

        given()
                .spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 5)
        .when()
                .get(POSTS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("pageable", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));

        logTestResult("READ with pagination", true);
    }

    // ==================== UPDATE ====================

    @Test
    @Order(5)
    @DisplayName("UPDATE: PUT /api/v1/posts/{id} should update post")
    public void testUpdatePost() {
        System.out.println("\n🧪 Testing: UPDATE post");

        // Skip if create failed
        Assumptions.assumeTrue(createdPostId != null, "Skipped: No post was created");

        String updateBody = """
                {
                    "title": "UPDATED: REST Assured CRUD Test Post",
                    "content": "This post was UPDATED by REST Assured integration tests.",
                    "published": true
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/" + createdPostId)
        .then()
                .statusCode(200)
                .body("title", containsString("UPDATED"));

        System.out.println("   ✅ Updated post ID: " + createdPostId);
        logTestResult("UPDATE post", true);
    }

    @Test
    @Order(6)
    @DisplayName("UPDATE: Verify post was actually updated")
    public void testVerifyUpdate() {
        System.out.println("\n🧪 Testing: Verify UPDATE worked");

        // Skip if create failed
        Assumptions.assumeTrue(createdPostSlug != null, "Skipped: No post was created");

        given()
                .spec(requestSpec)
        .when()
                .get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then()
                .statusCode(200)
                .body("title", containsString("UPDATED"));

        logTestResult("Verify UPDATE", true);
    }

    // ==================== DELETE ====================

    @Test
    @Order(7)
    @DisplayName("DELETE: DELETE /api/v1/posts/{id} should delete post")
    public void testDeletePost() {
        System.out.println("\n🧪 Testing: DELETE post");

        // Skip if create failed
        Assumptions.assumeTrue(createdPostId != null, "Skipped: No post was created");

        given()
                .spec(authenticatedSpec)
        .when()
                .delete(POSTS_ENDPOINT + "/" + createdPostId)
        .then()
                .statusCode(anyOf(is(200), is(204)));

        System.out.println("   ✅ Deleted post ID: " + createdPostId);
        logTestResult("DELETE post", true);
    }

    @Test
    @Order(8)
    @DisplayName("DELETE: Verify post was actually deleted")
    public void testVerifyDelete() {
        System.out.println("\n🧪 Testing: Verify DELETE worked");

        // Skip if create failed
        Assumptions.assumeTrue(createdPostSlug != null, "Skipped: No post was created");

        given()
                .spec(requestSpec)
        .when()
                .get(POSTS_ENDPOINT + "/" + createdPostSlug)
        .then()
                .statusCode(404);  // Post should not exist

        logTestResult("Verify DELETE (404)", true);
    }

    // ==================== ERROR CASES ====================

    @Test
    @Order(9)
    @DisplayName("ERROR: GET non-existent post should return 404")
    public void testGetNonExistentPost() {
        System.out.println("\n🧪 Testing: GET non-existent post → 404");

        given()
                .spec(requestSpec)
        .when()
                .get(POSTS_ENDPOINT + "/this-slug-does-not-exist-xyz")
        .then()
                .statusCode(404);

        logTestResult("Non-existent post → 404", true);
    }

    @Test
    @Order(10)
    @DisplayName("ERROR: UPDATE non-existent post should return 404")
    public void testUpdateNonExistentPost() {
        System.out.println("\n🧪 Testing: UPDATE non-existent post → 404");

        String updateBody = """
                {
                    "title": "Should fail",
                    "content": "This update should fail"
                }
                """;

        given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(POSTS_ENDPOINT + "/99999999")
        .then()
                .statusCode(404);

        logTestResult("Update non-existent → 404", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
