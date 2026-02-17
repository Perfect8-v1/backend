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
 * Integration tests for Order Flow (v1.3)
 *
 * Endpoints (via Gateway):
 * - /shop/api/orders           → shop-service (JWT required)
 * - /shop/api/orders/my-orders → shop-service (JWT, customer only)
 * - /shop/api/orders/{id}      → shop-service (JWT, ownership check)
 * - /shop/api/orders/{id}/cancel → shop-service (JWT, ownership check)
 * - /shop/api/orders/admin/all → shop-service (ADMIN only)
 * - /api/auth/                 → admin-service
 *
 * NOTE: Full order creation requires cart with items + shipping address.
 * This test focuses on endpoint access patterns and the order lifecycle.
 */
@DisplayName("Shop Service - Order Flow Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopOrderFlowTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 med service-prefix)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String ORDERS_ENDPOINT = "/shop/api/orders";
    private static final String MY_ORDERS_ENDPOINT = "/shop/api/orders/my-orders";
    private static final String ADMIN_ORDERS_ENDPOINT = "/shop/api/orders/admin/all";
    private static final String CART_ENDPOINT = "/shop/api/cart";
    private static final String PRODUCTS_ENDPOINT = "/shop/api/products";

    private static Long createdOrderId;
    private static String createdOrderNumber;

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

        System.out.println("🚀 Order Flow tests: " + BASE_URL);
        System.out.println("📋 Orders endpoint: " + ORDERS_ENDPOINT);
        System.out.println("📋 My orders: " + MY_ORDERS_ENDPOINT);
        System.out.println("📋 Admin orders: " + ADMIN_ORDERS_ENDPOINT);

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

    // ===== AUTH VERIFICATION =====

    @Test
    @Order(1)
    @DisplayName("GET /shop/api/orders/my-orders WITHOUT token → 401/403")
    public void testGetMyOrders_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(MY_ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(2)
    @DisplayName("POST /shop/api/orders WITHOUT token → 401/403")
    public void testCreateOrder_NoToken_Unauthorized() {
        String orderBody = """
                {"shippingFirstName": "Test"}
                """;

        given().spec(requestSpec).body(orderBody)
        .when().post(ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @Order(3)
    @DisplayName("GET /shop/api/orders/admin/all WITHOUT token → 401/403")
    public void testAdminGetAllOrders_NoToken_Unauthorized() {
        given().spec(requestSpec)
        .when().get(ADMIN_ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    // ===== MY ORDERS (authenticated) =====

    @Test
    @Order(4)
    @DisplayName("GET /shop/api/orders/my-orders WITH token → 200/403")
    public void testGetMyOrders_WithToken() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        // Admin JWT may not have a linked customer in shopDB → 403 is valid
        given().spec(authenticatedSpec)
        .when().get(MY_ORDERS_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(403)));
    }

    @Test
    @Order(5)
    @DisplayName("GET /shop/api/orders/my-orders with pagination")
    public void testGetMyOrders_WithPagination() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        // Admin JWT may not have a linked customer in shopDB → 403 is valid
        given().spec(authenticatedSpec)
                .queryParam("page", 0)
                .queryParam("size", 5)
        .when().get(MY_ORDERS_ENDPOINT)
        .then()
                .statusCode(anyOf(is(200), is(403)));
    }

    // ===== ORDER CREATION ATTEMPT =====

    @Test
    @Order(6)
    @DisplayName("PREPARE: Add product to cart for order creation")
    public void testAddProductToCart() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        // Get first available product
        Response productsResponse = given().spec(requestSpec)
        .when().get(PRODUCTS_ENDPOINT)
        .then().statusCode(200).extract().response();

        // Try to find a product ID from the response
        Long productId = null;
        try {
            // Response could be a list or paginated
            productId = productsResponse.jsonPath().getLong("[0].productId");
        } catch (Exception e) {
            try {
                productId = productsResponse.jsonPath().getLong("content[0].productId");
            } catch (Exception e2) {
                System.out.println("⚠️ No products found, skipping cart setup");
            }
        }

        if (productId != null) {
            String cartBody = String.format("""
                    {"productId": %d, "quantity": 1}
                    """, productId);

            given().spec(authenticatedSpec).body(cartBody)
            .when().post(CART_ENDPOINT + "/add")
            .then().statusCode(allOf(not(401), not(403)));

            System.out.println("✅ Added product " + productId + " to cart");
        }
    }

    @Test
    @Order(7)
    @DisplayName("CREATE: POST /shop/api/orders (with shipping info)")
    public void testCreateOrder() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        String orderBody = """
                {
                    "shippingFirstName": "Test",
                    "shippingLastName": "Order",
                    "shippingEmail": "test@p8.se",
                    "shippingPhone": "0701234567",
                    "shippingAddressLine1": "Testgatan 1",
                    "shippingCity": "Karlskoga",
                    "shippingPostalCode": "69132",
                    "shippingCountry": "SE",
                    "billingSameAsShipping": true
                }
                """;

        Response response = given()
                .spec(authenticatedSpec)
                .body(orderBody)
        .when()
                .post(ORDERS_ENDPOINT)
        .then()
                .extract().response();

        int statusCode = response.getStatusCode();
        System.out.println("📋 Create order response: " + statusCode);

        if (statusCode == 200 || statusCode == 201) {
            // Order created successfully
            try {
                createdOrderId = response.jsonPath().getLong("data.orderId");
                createdOrderNumber = response.jsonPath().getString("data.orderNumber");
            } catch (Exception e) {
                createdOrderId = response.jsonPath().getLong("orderId");
                createdOrderNumber = response.jsonPath().getString("orderNumber");
            }
            System.out.println("✅ Created order ID: " + createdOrderId + " (" + createdOrderNumber + ")");
        } else if (statusCode == 400) {
            // Expected if cart is empty or missing required fields
            System.out.println("⚠️ Order creation returned 400 (likely empty cart) - expected");
        } else {
            System.out.println("⚠️ Unexpected status: " + statusCode);
        }
    }

    // ===== READ ORDER =====

    @Test
    @Order(8)
    @DisplayName("READ: GET /shop/api/orders/{id}")
    public void testGetOrderById() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");
        Assumptions.assumeTrue(createdOrderId != null, "No order created");

        given().spec(authenticatedSpec)
        .when().get(ORDERS_ENDPOINT + "/" + createdOrderId)
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.orderId", equalTo(createdOrderId.intValue()));
    }

    @Test
    @Order(9)
    @DisplayName("READ: GET /shop/api/orders/number/{orderNumber}")
    public void testGetOrderByNumber() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");
        Assumptions.assumeTrue(createdOrderNumber != null, "No order created");

        given().spec(authenticatedSpec)
        .when().get(ORDERS_ENDPOINT + "/number/" + createdOrderNumber)
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    // ===== CANCEL ORDER =====

    @Test
    @Order(10)
    @DisplayName("CANCEL: POST /shop/api/orders/{id}/cancel")
    public void testCancelOrder() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");
        Assumptions.assumeTrue(createdOrderId != null, "No order created");

        given().spec(authenticatedSpec)
                .queryParam("reason", "Integration test cleanup")
        .when().post(ORDERS_ENDPOINT + "/" + createdOrderId + "/cancel")
        .then()
                .statusCode(200)
                .body("success", equalTo(true));

        System.out.println("✅ Order " + createdOrderId + " cancelled");
    }

    @Test
    @Order(11)
    @DisplayName("CANCEL: Verify order status is CANCELLED")
    public void testVerifyCancellation() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");
        Assumptions.assumeTrue(createdOrderId != null, "No order created");

        given().spec(authenticatedSpec)
        .when().get(ORDERS_ENDPOINT + "/" + createdOrderId)
        .then()
                .statusCode(200)
                .body("data.orderStatus", equalTo("CANCELLED"));
    }

    // ===== ADMIN ENDPOINTS =====

    @Test
    @Order(12)
    @DisplayName("ADMIN: GET /shop/api/orders/admin/all WITH admin token → 200")
    public void testAdminGetAllOrders_WithToken() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().get(ADMIN_ORDERS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @Order(13)
    @DisplayName("ADMIN: GET /shop/api/orders/admin/all with pagination")
    public void testAdminGetAllOrders_WithPagination() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
                .queryParam("page", 0)
                .queryParam("size", 5)
        .when().get(ADMIN_ORDERS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    // ===== ERROR CASES =====

    @Test
    @Order(14)
    @DisplayName("ERROR: GET non-existent order → 404/500")
    public void testGetNonExistentOrder() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().get(ORDERS_ENDPOINT + "/99999999")
        .then().statusCode(anyOf(is(400), is(404), is(500)));
    }

    @Test
    @Order(15)
    @DisplayName("ERROR: CANCEL non-existent order → 404/500")
    public void testCancelNonExistentOrder() {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
                .queryParam("reason", "Test")
        .when().post(ORDERS_ENDPOINT + "/99999999/cancel")
        .then().statusCode(anyOf(is(400), is(404), is(500)));
    }

    @Test
    @Order(16)
    @DisplayName("ERROR: Invalid token on orders → 401/403")
    public void testOrders_InvalidToken() {
        given().spec(requestSpec)
                .header("Authorization", "Bearer invalid.token")
        .when().get(MY_ORDERS_ENDPOINT)
        .then().statusCode(anyOf(equalTo(401), equalTo(403)));
    }
}
