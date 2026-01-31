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
 * Integration tests for JWT Authentication (v1.3)
 * 
 * Endpoints (via Gateway):
 * - /shop/api/products    → shop-service (public GET)
 * - /shop/api/categories  → shop-service (public GET)
 * - /shop/api/cart        → shop-service (JWT required)
 * - /shop/api/orders      → shop-service (JWT required)
 * - /api/auth/            → admin-service
 */
@DisplayName("Shop Service - JWT Auth Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 med service-prefix)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String PRODUCTS_ENDPOINT = "/shop/api/products";
    private static final String CATEGORIES_ENDPOINT = "/shop/api/categories";
    private static final String CART_ENDPOINT = "/shop/api/cart";
    private static final String ORDERS_ENDPOINT = "/shop/api/orders";

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

        System.out.println("🚀 Shop tests: " + BASE_URL);
        System.out.println("📍 Products: " + PRODUCTS_ENDPOINT);
        System.out.println("📍 Categories: " + CATEGORIES_ENDPOINT);
        System.out.println("📍 Cart: " + CART_ENDPOINT);
        System.out.println("📍 Orders: " + ORDERS_ENDPOINT);
    }

    @Test
    @Order(1)
    @DisplayName("Get JWT token")
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

    // ===== PUBLIC ENDPOINTS =====

    @Test
    @Order(2)
    @DisplayName("GET /shop/api/products without token (public)")
    public void testGetProducts_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("GET /shop/api/categories without token (public)")
    public void testGetCategories_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT)
        .then().statusCode(200);
    }

    // ===== PROTECTED CART ENDPOINTS =====

    @Test
    @Order(4)
    @DisplayName("GET /shop/api/cart WITHOUT token → 401/403")
    public void testGetCart_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(CART_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(5)
    @DisplayName("POST /shop/api/cart/add WITHOUT token → 401/403")
    public void testAddToCart_NoToken_Unauthorized() {
        String cartBody = """
                {"productId": 1, "quantity": 1}
                """;

        given().spec(requestSpec).body(cartBody)
        .when().post(CART_ENDPOINT + "/add")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /shop/api/cart WITH token → auth passes")
    public void testGetCart_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(CART_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== PROTECTED ORDERS ENDPOINTS =====

    @Test
    @Order(7)
    @DisplayName("GET /shop/api/orders WITHOUT token → 401/403")
    public void testGetOrders_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(8)
    @DisplayName("GET /shop/api/orders WITH token → auth passes")
    public void testGetOrders_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(ORDERS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== ADMIN ONLY ENDPOINTS =====

    @Test
    @Order(9)
    @DisplayName("POST /shop/api/products WITHOUT token → 401/403")
    public void testCreateProduct_NoToken_Unauthorized() {
        String productBody = """
                {"name": "Test Product", "price": 99.99}
                """;

        given().spec(requestSpec).body(productBody)
        .when().post(PRODUCTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(10)
    @DisplayName("PUT /shop/api/products/{id} WITHOUT token → 401/403")
    public void testUpdateProduct_NoToken_Unauthorized() {
        String productBody = """
                {"name": "Updated Product", "price": 149.99}
                """;

        given().spec(requestSpec).body(productBody)
        .when().put(PRODUCTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(11)
    @DisplayName("DELETE /shop/api/products/{id} WITHOUT token → 401/403")
    public void testDeleteProduct_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().delete(PRODUCTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(12)
    @DisplayName("POST /shop/api/products WITH admin token → auth passes")
    public void testCreateProduct_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String productBody = """
                {"name": "Test", "price": 0}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(productBody)
        .when().post(PRODUCTS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ===== INVALID TOKEN TESTS =====

    @Test
    @Order(13)
    @DisplayName("GET /shop/api/cart with invalid token → 401/403")
    public void testCart_InvalidToken_Unauthorized() {
        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
        .when().get(CART_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(14)
    @DisplayName("GET /shop/api/orders with invalid token → 401/403")
    public void testOrders_InvalidToken_Unauthorized() {
        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
        .when().get(ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
