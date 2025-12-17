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
 * Integration tests for JWT Authentication via nginx (p8.rantila.com)
 * 
 * Nginx routing:
 * - /api/v1/products/   → shop-service /api/products/
 * - /api/v1/categories/ → shop-service /api/categories/
 * - /api/v1/customers/  → shop-service /api/customers/
 * - /api/v1/orders/     → shop-service /api/orders/
 * 
 * Shop Service endpoints:
 * PUBLIC: GET products, categories, customer register/login
 * PROTECTED: POST/PUT/DELETE products, categories, orders, admin/*
 */
@DisplayName("Shop Service - JWT Authentication Tests (Remote)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    // Configuration - via nginx
    private static final String BASE_URL = "http://p8.rantila.com";

    // Test credentials (shop customer login)
    private static final String TEST_EMAIL = "admin@perfect8.com";
    private static final String TEST_PASSWORD = "admin123";

    // Endpoints (nginx mapped)
    private static final String LOGIN_ENDPOINT = "/api/v1/customers/login";
    private static final String PRODUCTS_ENDPOINT = "/api/v1/products";
    private static final String CATEGORIES_ENDPOINT = "/api/v1/categories";
    private static final String ORDERS_ENDPOINT = "/api/v1/orders";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        System.out.println("🚀 Shop Service tests configured for: " + BASE_URL);
    }

    // ==================== LOGIN (Get JWT Token) ====================

    @Test
    @Order(1)
    @DisplayName("Login should return JWT token")
    public void testLogin_ValidCredentials_ReturnsToken() {
        System.out.println("\n🧪 Testing: Login with valid credentials");

        String loginBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_EMAIL, TEST_PASSWORD);

        Response response = given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        jwtToken = response.jsonPath().getString("token");
        System.out.println("   ✅ Received JWT token: " + jwtToken.substring(0, 20) + "...");
        
        logTestResult("Login returns JWT token", true);
    }

    @Test
    @Order(2)
    @DisplayName("Login with invalid credentials should return 401")
    public void testLogin_InvalidCredentials_Returns401() {
        System.out.println("\n🧪 Testing: Login with invalid credentials → 401");

        String loginBody = """
                {
                    "email": "wrong@example.com",
                    "password": "wrongpassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(401);

        logTestResult("Invalid login → 401", true);
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/products should work WITHOUT token (public)")
    public void testGetProducts_NoToken_Returns200() {
        System.out.println("\n🧪 Testing: GET products without token (public)");

        given()
                .spec(requestSpec)
        .when()
                .get(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET products (public)", true);
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/products/{id} should work WITHOUT token (public)")
    public void testGetProductById_NoToken_PublicEndpoint() {
        System.out.println("\n🧪 Testing: GET product by ID without token (public)");

        // Will return 200 or 404, but NOT 401/403
        int statusCode = given()
                .spec(requestSpec)
        .when()
                .get(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (public endpoint OK)");
        logTestResult("GET product by ID (public)", true);
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/categories should work WITHOUT token (public)")
    public void testGetCategories_NoToken_Returns200() {
        System.out.println("\n🧪 Testing: GET categories without token (public)");

        given()
                .spec(requestSpec)
        .when()
                .get(CATEGORIES_ENDPOINT)
        .then()
                .statusCode(200);

        logTestResult("GET categories (public)", true);
    }

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/products WITHOUT token should return 401 or 403")
    public void testCreateProduct_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: POST product WITHOUT token → 401/403");

        String productBody = """
                {
                    "name": "Test Product",
                    "description": "Test description",
                    "price": 99.99,
                    "sku": "TEST-SKU-001"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("POST product without token → " + statusCode, true);
    }

    @Test
    @Order(7)
    @DisplayName("PUT /api/v1/products/{id} WITHOUT token should return 401 or 403")
    public void testUpdateProduct_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: PUT product WITHOUT token → 401/403");

        String updateBody = """
                {
                    "name": "Updated Product",
                    "price": 149.99
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .body(updateBody)
        .when()
                .put(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("PUT product without token → " + statusCode, true);
    }

    @Test
    @Order(8)
    @DisplayName("DELETE /api/v1/products/{id} WITHOUT token should return 401 or 403")
    public void testDeleteProduct_NoToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: DELETE product WITHOUT token → 401/403");

        int statusCode = given()
                .spec(requestSpec)
        .when()
                .delete(PRODUCTS_ENDPOINT + "/1")
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("DELETE product without token → " + statusCode, true);
    }

    // ==================== PROTECTED ENDPOINTS - WITH VALID TOKEN ====================

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/products WITH valid JWT token should pass auth")
    public void testCreateProduct_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: POST product WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        String productBody = """
                {
                    "name": "REST Assured Test Product",
                    "description": "Created by integration test",
                    "price": 99.99,
                    "sku": "RA-TEST-001"
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK!)");
        logTestResult("POST product with token → auth passes", true);
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /api/v1/products/{id} WITH valid JWT token should pass auth")
    public void testDeleteProduct_WithToken_AuthPasses() {
        System.out.println("\n🧪 Testing: DELETE product WITH JWT token → auth passes");
        
        Assumptions.assumeTrue(jwtToken != null, "JWT token required - run login test first");

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .delete(PRODUCTS_ENDPOINT + "/99999")
        .then()
                .statusCode(allOf(
                        not(401),
                        not(403)
                ))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode + " (auth OK, product probably doesn't exist)");
        logTestResult("DELETE product with token → auth passes", true);
    }

    // ==================== INVALID TOKENS ====================

    @Test
    @Order(11)
    @DisplayName("Invalid JWT token should return 401 or 403")
    public void testCreateProduct_InvalidToken_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Invalid JWT token → 401/403");

        String productBody = """
                {
                    "name": "Test",
                    "price": 10.00
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer invalid.token.here")
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Invalid token → " + statusCode, true);
    }

    @Test
    @Order(12)
    @DisplayName("Empty Authorization header should return 401 or 403")
    public void testCreateProduct_EmptyHeader_ReturnsUnauthorized() {
        System.out.println("\n🧪 Testing: Empty Authorization header → 401/403");

        String productBody = """
                {
                    "name": "Test",
                    "price": 10.00
                }
                """;

        int statusCode = given()
                .spec(requestSpec)
                .header("Authorization", "")
                .body(productBody)
        .when()
                .post(PRODUCTS_ENDPOINT)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)))
                .extract()
                .statusCode();

        System.out.println("   Received status: " + statusCode);
        logTestResult("Empty header → " + statusCode, true);
    }

    // ==================== HELPER ====================

    private void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
