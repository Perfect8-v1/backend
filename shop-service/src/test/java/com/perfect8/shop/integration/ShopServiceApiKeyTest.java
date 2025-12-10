package com.perfect8.shop.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for API-Key Authentication on Shop Service (Plain branch)
 * 
 * Shop Service endpoints:
 * 
 * PUBLIC (no auth):
 * - GET /actuator/health
 * - GET /api/v1/products
 * - GET /api/v1/products/{id}
 * - GET /api/v1/categories
 * - GET /api/v1/categories/{id}
 * - POST /api/v1/customers/register
 * - POST /api/v1/customers/login
 * - GET/POST /api/v1/cart (guest access)
 * 
 * PROTECTED (requires API key):
 * - POST/PUT/DELETE /api/v1/products
 * - POST/PUT/DELETE /api/v1/categories
 * - GET /api/v1/orders (customer orders)
 * - GET /api/v1/admin/* (admin endpoints)
 */
@DisplayName("Shop Service - API Key Authentication Tests")
public class ShopServiceApiKeyTest {

    protected static RequestSpecification requestSpec;
    protected static RequestSpecification authenticatedSpec;

    // Configuration
    private static final String BASE_URL = "http://p8.rantila.com";
    private static final int SHOP_PORT = 8085;
    private static final String SHOP_API_KEY = "p8shop_1Lm3pV6bC9fK2hW4";
    private static final String BLOG_API_KEY = "p8blog_3Fw6yH9jM2nP5vX8";

    // Endpoints
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String PRODUCTS_ENDPOINT = "/api/v1/products";
    private static final String CATEGORIES_ENDPOINT = "/api/v1/categories";
    private static final String CUSTOMERS_ENDPOINT = "/api/v1/customers";
    private static final String ORDERS_ENDPOINT = "/api/v1/orders";
    private static final String ADMIN_ORDERS_ENDPOINT = "/api/v1/admin/orders";

    @BeforeAll
    public static void setup() {
        String fullUrl = BASE_URL + ":" + SHOP_PORT;
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
                .addHeader("X-API-Key", SHOP_API_KEY)
                .build();

        System.out.println("🚀 Shop Service tests configured for: " + fullUrl);
        System.out.println("🔑 Using API-Key: " + SHOP_API_KEY.substring(0, 10) + "...");
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @DisplayName("Health endpoint should work WITHOUT API key")
    public void testHealthEndpoint_NoApiKey_Returns200() {
        System.out.println("\n🧪 Testing: Health endpoint without API key");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        logTestResult("Health endpoint (no key)", true);
    }

    @Test
    @DisplayName("GET /api/v1/products should work WITHOUT API key (public)")
    public void testGetProducts_NoApiKey_Returns200() {
        System.out.println("\n🧪 Testing: GET products without API key (public)");

        given()
                .spec(requestSpec)
        .when()
                .get(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET products (public)", true);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} should work WITHOUT API key (public)")
    public void testGetProductById_NoApiKey_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET product by ID without API key (public)");

        // Will return 200 or 404, but NOT 403
        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET product by ID (public)", true);
    }

    @Test
    @DisplayName("GET /api/v1/categories should work WITHOUT API key (public)")
    public void testGetCategories_NoApiKey_Returns200() {
        System.out.println("\n🧪 Testing: GET categories without API key (public)");

        given()
                .spec(requestSpec)
        .when()
                .get(CATEGORIES_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET categories (public)", true);
    }

    // ==================== PROTECTED PRODUCT ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("POST /api/v1/products WITHOUT API key should return 403")
    public void testCreateProduct_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST product WITHOUT API key → 403");

        String productBody = """
                {
                    "name": "Test Product",
                    "description": "Test description",
                    "price": 99.99,
                    "sku": "TEST-SKU-001"
                }
                """;

        given()
                .spec(requestSpec)
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("POST product without key → 403", true);
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} WITHOUT API key should return 403")
    public void testUpdateProduct_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: PUT product WITHOUT API key → 403");

        String updateBody = """
                {
                    "name": "Updated Product",
                    "price": 149.99
                }
                """;

        given()
                .spec(requestSpec)
                .body(updateBody)
        .when()
                .put(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(403);

        logTestResult("PUT product without key → 403", true);
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} WITHOUT API key should return 403")
    public void testDeleteProduct_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: DELETE product WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .delete(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(403);

        logTestResult("DELETE product without key → 403", true);
    }

    // ==================== PROTECTED CATEGORY ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("POST /api/v1/categories WITHOUT API key should return 403")
    public void testCreateCategory_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: POST category WITHOUT API key → 403");

        String categoryBody = """
                {
                    "name": "Test Category",
                    "description": "Test description"
                }
                """;

        given()
                .spec(requestSpec)
                .body(categoryBody)
        .when()
                .post(CATEGORIES_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("POST category without key → 403", true);
    }

    // ==================== ADMIN ENDPOINTS - NO KEY ====================

    @Test
    @DisplayName("GET /api/v1/admin/orders WITHOUT API key should return 403")
    public void testGetAdminOrders_NoApiKey_Returns403() {
        System.out.println("\n🧪 Testing: GET admin orders WITHOUT API key → 403");

        given()
                .spec(requestSpec)
        .when()
                .get(ADMIN_ORDERS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("GET admin orders without key → 403", true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH KEY ====================

    @Test
    @DisplayName("POST /api/v1/products WITH valid API key should pass auth")
    public void testCreateProduct_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: POST product WITH API key → auth passes");

        String productBody = """
                {
                    "name": "REST Assured Test Product",
                    "description": "Created by integration test",
                    "price": 99.99,
                    "sku": "RA-TEST-001"
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST product with key → auth passes", true);
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} WITH valid API key should pass auth")
    public void testUpdateProduct_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: PUT product WITH API key → auth passes");

        String updateBody = """
                {
                    "name": "Updated by REST Assured",
                    "price": 149.99
                }
                """;

        int statusCode = given()
                .spec(authenticatedSpec)
                .body(updateBody)
        .when()
                .put(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, product might not exist)");
        logTestResult("PUT product with key → auth passes", true);
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} WITH valid API key should pass auth")
    public void testDeleteProduct_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE product WITH API key → auth passes");

        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .delete(PRODUCTS_ENDPOINT + "/99999")
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, product probably doesn't exist)");
        logTestResult("DELETE product with key → auth passes", true);
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders WITH valid API key should pass auth")
    public void testGetAdminOrders_WithApiKey_AuthPasses() {
        System.out.println("\n🧪 Testing: GET admin orders WITH API key → auth passes");

        int statusCode = given()
                .spec(authenticatedSpec)
        .when()
                .get(ADMIN_ORDERS_ENDPOINT)
        .then()
                .statusCode(not(403))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("GET admin orders with key → auth passes", true);
    }

    // ==================== WRONG SERVICE KEY ====================

    @Test
    @DisplayName("Blog API key on Shop service should return 403")
    public void testCreateProduct_WrongServiceKey_Returns403() {
        System.out.println("\n🧪 Testing: Blog key on Shop service → 403");

        String productBody = """
                {
                    "name": "Test",
                    "price": 10.00
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", BLOG_API_KEY)  // Wrong key!
                .body(productBody)
        .when()
                .post(BASE_URL + ":" + SHOP_PORT + PRODUCTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Wrong service key → 403", true);
    }

    @Test
    @DisplayName("Invalid API key should return 403")
    public void testCreateProduct_InvalidApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Invalid API key → 403");

        String productBody = """
                {
                    "name": "Test",
                    "price": 10.00
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "invalid_key_xyz")
                .body(productBody)
        .when()
                .post(BASE_URL + ":" + SHOP_PORT + PRODUCTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Invalid API key → 403", true);
    }

    @Test
    @DisplayName("Empty API key should return 403")
    public void testCreateProduct_EmptyApiKey_Returns403() {
        System.out.println("\n🧪 Testing: Empty API key → 403");

        String productBody = """
                {
                    "name": "Test",
                    "price": 10.00
                }
                """;

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("X-API-Key", "")
                .body(productBody)
        .when()
                .post(BASE_URL + ":" + SHOP_PORT + PRODUCTS_ENDPOINT)
        .then()
                .statusCode(403);

        logTestResult("Empty API key → 403", true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
