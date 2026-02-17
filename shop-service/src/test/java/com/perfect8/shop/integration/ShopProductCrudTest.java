package com.perfect8.shop.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Product CRUD (v1.3)
 *
 * Endpoints (via Gateway):
 * - /shop/api/products    → shop-service
 * - /shop/api/categories  → shop-service
 * - /api/auth/            → admin-service
 *
 * NOTE: All responses wrapped in ApiResponse { success, message, data }
 * NOTE: Controller catches all exceptions → returns 400 (not 404)
 * NOTE: ProductCreateRequest requires: name, price, sku, stockQuantity, categoryId
 *
 * Flow: CREATE → READ → UPDATE → VERIFY → DELETE → VERIFY
 */
@DisplayName("Shop Service - Product CRUD Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopProductCrudTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 med service-prefix)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String PRODUCTS_ENDPOINT = "/shop/api/products";
    private static final String CATEGORIES_ENDPOINT = "/shop/api/categories";

    private static Long createdProductId;
    private static Long existingCategoryId;
    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final String TEST_PRODUCT_NAME = "CRUD-Test-Product-" + TIMESTAMP;
    private static final String TEST_SKU = "TEST-SKU-" + TIMESTAMP;

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

        System.out.println("🚀 Product CRUD tests: " + BASE_URL);
        System.out.println("📦 Products endpoint: " + PRODUCTS_ENDPOINT);

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

        // Electronics = categoryId 1 (mockdata)
        existingCategoryId = 1L;
        System.out.println("📂 Using categoryId: " + existingCategoryId);
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

    // ===== CREATE =====

    @Test
    @Order(1)
    @DisplayName("CREATE: POST /shop/api/products")
    public void testCreateProduct() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");
        Assumptions.assumeTrue(existingCategoryId != null, "CategoryId required");

        String productBody = String.format("""
                {
                    "name": "%s",
                    "description": "Created by integration test",
                    "price": 299.99,
                    "sku": "%s",
                    "stockQuantity": 10,
                    "categoryId": %d
                }
                """, TEST_PRODUCT_NAME, TEST_SKU, existingCategoryId);

        Response response = given()
                .spec(authenticatedSpec)
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.name", equalTo(TEST_PRODUCT_NAME))
                .body("data.price", notNullValue())
                .extract().response();

        createdProductId = response.jsonPath().getLong("data.productId");
        System.out.println("✅ Created product ID: " + createdProductId);
    }

    // ===== READ =====

    @Test
    @Order(2)
    @DisplayName("READ: GET /shop/api/products (public)")
    public void testGetAllProducts() {
        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Order(3)
    @DisplayName("READ: GET /shop/api/products/{id}")
    public void testGetProductById() {
        Assumptions.assumeTrue(createdProductId != null, "No product created");

        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT + "/" + createdProductId)
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.productId", equalTo(createdProductId.intValue()))
                .body("data.name", equalTo(TEST_PRODUCT_NAME));
    }

    @Test
    @Order(4)
    @DisplayName("READ: GET /shop/api/categories (public)")
    public void testGetAllCategories() {
        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT)
        .then().statusCode(200);
    }

    // ===== UPDATE =====

    @Test
    @Order(5)
    @DisplayName("UPDATE: PUT /shop/api/products/{id}")
    public void testUpdateProduct() {
        Assumptions.assumeTrue(createdProductId != null, "No product created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String updateBody = String.format("""
                {
                    "name": "UPDATED: CRUD Test Product",
                    "description": "Updated by integration test",
                    "price": 399.99,
                    "sku": "%s",
                    "stockQuantity": 20,
                    "categoryId": %d,
                    "active": true
                }
                """, TEST_SKU, existingCategoryId);

        Response updateResponse = given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(PRODUCTS_ENDPOINT + "/" + createdProductId)
        .then()
                .extract().response();

        System.out.println("📝 UPDATE status: " + updateResponse.getStatusCode());
        System.out.println("📝 UPDATE body: " + updateResponse.getBody().asString());

        Assertions.assertEquals(200, updateResponse.getStatusCode(),
                "Update failed: " + updateResponse.getBody().asString());
        Assertions.assertTrue(updateResponse.jsonPath().getBoolean("success"));
        Assertions.assertTrue(updateResponse.jsonPath().getString("data.name").contains("UPDATED"));
    }

    @Test
    @Order(6)
    @DisplayName("UPDATE: Verify update worked")
    public void testVerifyUpdate() {
        Assumptions.assumeTrue(createdProductId != null, "No product created");

        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT + "/" + createdProductId)
        .then()
                .statusCode(200)
                .body("data.name", containsString("UPDATED"))
                .body("data.price", equalTo(399.99f));
    }

    // ===== DELETE =====

    @Test
    @Order(7)
    @DisplayName("DELETE: DELETE /shop/api/products/{id}")
    public void testDeleteProduct() {
        Assumptions.assumeTrue(createdProductId != null, "No product created");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().delete(PRODUCTS_ENDPOINT + "/" + createdProductId)
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Order(8)
    @DisplayName("DELETE: Verify soft delete worked (active = false)")
    public void testVerifyDelete() {
        Assumptions.assumeTrue(createdProductId != null, "No product created");

        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT + "/" + createdProductId)
        .then()
                .statusCode(200)
                .body("data.active", equalTo(false));
    }

    // ===== ERROR CASES =====

    @Test
    @Order(9)
    @DisplayName("ERROR: GET non-existent product → 400/404")
    public void testGetNonExistentProduct() {
        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT + "/99999999")
        .then()
                .statusCode(anyOf(is(400), is(404)))
                .body("success", equalTo(false));
    }

    @Test
    @Order(10)
    @DisplayName("ERROR: UPDATE non-existent product → 400/404")
    public void testUpdateNonExistentProduct() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String updateBody = """
                {"name": "Should fail", "price": 1.00, "sku": "NONEXIST", "active": true}
                """;

        given().spec(authenticatedSpec).body(updateBody)
        .when().put(PRODUCTS_ENDPOINT + "/99999999")
        .then()
                .statusCode(anyOf(is(400), is(404)))
                .body("success", equalTo(false));
    }

    @Test
    @Order(11)
    @DisplayName("ERROR: CREATE product without auth → 401/403")
    public void testCreateProduct_NoToken_Unauthorized() {
        String productBody = """
                {"name": "No Auth Product", "price": 99.99, "sku": "NOAUTH", "stockQuantity": 1, "categoryId": 1}
                """;

        given().spec(requestSpec).body(productBody)
        .when().post(PRODUCTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(12)
    @DisplayName("ERROR: DELETE product without auth → 401/403")
    public void testDeleteProduct_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().delete(PRODUCTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
