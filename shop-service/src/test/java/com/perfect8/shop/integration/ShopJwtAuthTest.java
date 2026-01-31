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
 * Endpoints:
 * - /api/products    → shop-service (public GET)
 * - /api/categories  → shop-service (public GET)
 * - /api/cart        → shop-service (customer, JWT)
 * - /api/orders      → shop-service (customer, JWT)
 * - /api/auth/       → admin-service
 */
@DisplayName("Shop Service - JWT Auth Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopJwtAuthTest {

    private static RequestSpecification requestSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String PRODUCTS_ENDPOINT = "/api/products";
    private static final String CATEGORIES_ENDPOINT = "/api/categories";
    private static final String CART_ENDPOINT = "/api/cart";
    private static final String ORDERS_ENDPOINT = "/api/orders";

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

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    @Order(2)
    @DisplayName("GET /api/products without token (public)")
    public void testGetProducts_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/products/{id} without token (public)")
    public void testGetProductById_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/categories without token (public)")
    public void testGetCategories_NoToken_Returns200() {
        given().spec(requestSpec)
        .when().get(CATEGORIES_ENDPOINT)
        .then().statusCode(200);
    }

    // ==================== PROTECTED ENDPOINTS - NO TOKEN ====================

    @Test
    @Order(5)
    @DisplayName("GET /api/cart WITHOUT token → 401/403")
    public void testGetCart_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(CART_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/cart/add WITHOUT token → 401/403")
    public void testAddToCart_NoToken_Unauthorized() {
        String cartBody = """
                {"productId": 1, "quantity": 1}
                """;

        given().spec(requestSpec).body(cartBody)
        .when().post(CART_ENDPOINT + "/add")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/orders WITHOUT token → 401/403")
    public void testGetOrders_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/products WITHOUT token → 401/403 (admin)")
    public void testCreateProduct_NoToken_Unauthorized() {
        String productBody = """
                {"name": "Test Product", "price": 100.00, "stock": 10}
                """;

        given().spec(requestSpec).body(productBody)
        .when().post(PRODUCTS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/products/{id} WITHOUT token → 401/403 (admin)")
    public void testDeleteProduct_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().delete(PRODUCTS_ENDPOINT + "/1")
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ==================== PROTECTED ENDPOINTS - WITH TOKEN ====================

    @Test
    @Order(10)
    @DisplayName("GET /api/cart WITH token → auth passes")
    public void testGetCart_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(CART_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/cart/add WITH token → auth passes")
    public void testAddToCart_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        String cartBody = """
                {"productId": 1, "quantity": 1}
                """;

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
                .body(cartBody)
        .when().post(CART_ENDPOINT + "/add")
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/orders WITH token → auth passes")
    public void testGetOrders_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().get(ORDERS_ENDPOINT)
        .then().statusCode(allOf(not(401), not(403)));
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /api/products/{id} WITH admin token → auth passes")
    public void testDeleteProduct_WithToken_AuthPasses() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        given().spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken)
        .when().delete(PRODUCTS_ENDPOINT + "/99999")
        .then().statusCode(allOf(not(401), not(403)));
    }

    // ==================== INVALID TOKEN ====================

    @Test
    @Order(14)
    @DisplayName("Invalid token → 401/403")
    public void testCart_InvalidToken_Unauthorized() {
        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
        .when().get(CART_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
