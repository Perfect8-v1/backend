package com.perfect8.shop.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Category CRUD (v1.3)
 *
 * IMPORTANT: Category JSON has circular references (parent ↔ subcategories).
 * RestAssured's jsonPath() triggers Groovy JsonSlurper → StackOverflow.
 * Solution: Use extract().asString() + regex extraction for ALL category responses.
 *
 * Flow: CREATE → READ → UPDATE → VERIFY → DELETE → VERIFY
 */
@DisplayName("Shop Service - Category CRUD Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopCategoryCrudTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String CATEGORIES_ENDPOINT = "/shop/api/categories";

    private static Long createdCategoryId;
    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_CATEGORY_NAME = "CRUD-Test-Category-" + TIMESTAMP;
    private static final String TEST_SLUG = "crud-test-category-" + TIMESTAMP;

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

        System.out.println("🚀 Category CRUD tests: " + BASE_URL);
        System.out.println("📂 Categories endpoint: " + CATEGORIES_ENDPOINT);

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

    /** Extract Long from raw JSON via regex (avoids JsonSlurper StackOverflow) */
    private static Long extractLong(String json, String fieldName) {
        Matcher m = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    /** Extract boolean from raw JSON via regex */
    private static Boolean extractBoolean(String json, String fieldName) {
        Matcher m = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : null;
    }

    // ===== CREATE =====

    @Test
    @Order(1)
    @DisplayName("CREATE: POST /shop/api/categories")
    public void testCreateCategory() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String categoryBody = String.format("""
                {
                    "name": "%s",
                    "description": "Created by integration test",
                    "slug": "%s",
                    "active": true
                }
                """, TEST_CATEGORY_NAME, TEST_SLUG);

        // CRITICAL: Use asString() — NEVER jsonPath() on category responses!
        String raw = given()
                .spec(authenticatedSpec)
                .body(categoryBody)
        .when()
                .post(CATEGORIES_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract().asString();

        System.out.println("📂 CREATE (first 200 chars): " + raw.substring(0, Math.min(200, raw.length())));

        createdCategoryId = extractLong(raw, "categoryId");
        Assertions.assertNotNull(createdCategoryId, "Could not extract categoryId from response");
        System.out.println("✅ Created category ID: " + createdCategoryId);
    }

    // ===== READ =====

    @Test
    @Order(2)
    @DisplayName("READ: GET /shop/api/categories (public, status only)")
    public void testGetAllCategories() {
        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("READ: GET /shop/api/categories/{id}")
    public void testGetCategoryById() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");

        String raw = given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT + "/" + createdCategoryId)
        .then()
                .statusCode(200)
                .extract().asString();

        Assertions.assertTrue(raw.contains(TEST_CATEGORY_NAME), "Category name not found in response");
    }

    @Test
    @Order(4)
    @DisplayName("READ: GET /shop/api/categories/{id}/products (empty)")
    public void testGetCategoryProducts() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");

        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT + "/" + createdCategoryId + "/products")
        .then().statusCode(200);
    }

    // ===== UPDATE =====

    @Test
    @Order(5)
    @DisplayName("UPDATE: PUT /shop/api/categories/{id}")
    public void testUpdateCategory() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String updateBody = String.format("""
                {
                    "name": "UPDATED: CRUD Test Category",
                    "description": "Updated by integration test",
                    "slug": "%s",
                    "active": true
                }
                """, TEST_SLUG);

        given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(CATEGORIES_ENDPOINT + "/" + createdCategoryId)
        .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    @DisplayName("UPDATE: Verify update worked")
    public void testVerifyUpdate() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");

        String raw = given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT + "/" + createdCategoryId)
        .then()
                .statusCode(200)
                .extract().asString();

        Assertions.assertTrue(raw.contains("UPDATED"), "Update not reflected in response");
    }

    // ===== DELETE =====

    @Test
    @Order(7)
    @DisplayName("DELETE: DELETE /shop/api/categories/{id}")
    public void testDeleteCategory() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().delete(CATEGORIES_ENDPOINT + "/" + createdCategoryId)
        .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(8)
    @DisplayName("DELETE: Verify soft delete worked")
    public void testVerifyDelete() {
        Assumptions.assumeTrue(createdCategoryId != null, "No category created");

        String raw = given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT + "/" + createdCategoryId)
        .then()
                .extract().asString();

        // Soft delete: active=false in JSON, or 400/404 (hidden)
        Boolean active = extractBoolean(raw, "active");
        if (active != null) {
            Assertions.assertFalse(active, "Expected soft delete (active=false)");
        }
        // 400/404 also acceptable
    }

    // ===== ERROR CASES =====

    @Test
    @Order(9)
    @DisplayName("ERROR: GET non-existent category → 400/404")
    public void testGetNonExistentCategory() {
        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT + "/99999999")
        .then().statusCode(anyOf(is(400), is(404)));
    }

    @Test
    @Order(10)
    @DisplayName("ERROR: CREATE without auth → 401/403")
    public void testCreateCategory_NoToken_Unauthorized() {
        given().spec(requestSpec)
                .body("""
                {"name": "No Auth Category", "slug": "no-auth-cat"}
                """)
        .when().post(CATEGORIES_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(11)
    @DisplayName("ERROR: DELETE without auth → 401/403")
    public void testDeleteCategory_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().delete(CATEGORIES_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
