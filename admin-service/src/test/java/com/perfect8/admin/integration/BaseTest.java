package com.perfect8.admin.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base test class for REST Assured integration tests - v1.3
 * 
 * Testar mot produktionsservern via Gateway:
 * - https://p8.rantila.com (HTTPS)
 * - Industristandard auth (email + password)
 * - Automatisk token-hantering
 */
public abstract class BaseTest {

    protected static RequestSpecification requestSpec;
    protected static String accessToken;
    protected static String refreshToken;

    // ===========================================
    // KONFIGURATION
    // ===========================================
    
    protected static final String BASE_URL = "https://p8.rantila.com";
    
    // Test credentials (måste finnas i databasen)
    protected static final String TEST_EMAIL = "cmb@p8.se";
    protected static final String TEST_PASSWORD = "magnus123";

    // ===========================================
    // GATEWAY ROUTES
    // ===========================================
    
    // Auth (direkt på Gateway)
    protected static final String AUTH_LOGIN = "/api/auth/login";
    protected static final String AUTH_REGISTER = "/api/auth/register";
    protected static final String AUTH_REFRESH = "/api/auth/refresh";
    protected static final String AUTH_LOGOUT = "/api/auth/logout";
    
    // Admin
    protected static final String ADMIN_USERS = "/api/admin/users";
    
    // Blog (via /blog prefix)
    protected static final String BLOG_POSTS = "/blog/api/posts";
    
    // Shop (via /shop prefix)
    protected static final String SHOP_PRODUCTS = "/shop/api/products";
    protected static final String SHOP_CATEGORIES = "/shop/api/categories";
    protected static final String SHOP_CART = "/shop/api/cart";
    protected static final String SHOP_ORDERS = "/shop/api/orders";
    protected static final String SHOP_CUSTOMERS = "/shop/api/customers";
    
    // Image (via /image prefix)
    protected static final String IMAGE_IMAGES = "/image/api/images";
    
    // Email (via /email prefix)
    protected static final String EMAIL_SEND = "/email/api/email/send";
    protected static final String EMAIL_LOGS = "/email/api/email/logs";
    
    // Health
    protected static final String HEALTH = "/actuator/health";

    // ===========================================
    // SETUP
    // ===========================================

    @BeforeAll
    public static void setup() {
        // Tillåt self-signed certificates (om nödvändigt)
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        System.out.println("🚀 REST Assured configured for: " + BASE_URL);
    }

    // ===========================================
    // HJÄLPMETODER - AUTHENTICATION
    // ===========================================

    /**
     * Loggar in och sparar tokens för användning i andra tester
     */
    protected static void login() {
        login(TEST_EMAIL, TEST_PASSWORD);
    }

    /**
     * Loggar in med specifika credentials
     */
    protected static void login(String email, String password) {
        String loginBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        Response response = RestAssured.given()
                .spec(requestSpec)
                .body(loginBody)
                .when()
                .post(AUTH_LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .response();

        accessToken = response.jsonPath().getString("accessToken");
        refreshToken = response.jsonPath().getString("refreshToken");

        System.out.println("🔑 Logged in as: " + email);
        System.out.println("   Access token: " + accessToken.substring(0, 30) + "...");
        System.out.println("   Refresh token: " + refreshToken);
    }

    /**
     * Skapar en RequestSpecification med Bearer token
     */
    protected static RequestSpecification withAuth() {
        if (accessToken == null) {
            login();
        }
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    /**
     * Förnyar access token med refresh token
     */
    protected static void refreshAccessToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available - login first");
        }

        String refreshBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        Response response = RestAssured.given()
                .spec(requestSpec)
                .body(refreshBody)
                .when()
                .post(AUTH_REFRESH)
                .then()
                .statusCode(200)
                .extract()
                .response();

        accessToken = response.jsonPath().getString("accessToken");
        refreshToken = response.jsonPath().getString("refreshToken");

        System.out.println("🔄 Token refreshed");
        System.out.println("   New access token: " + accessToken.substring(0, 30) + "...");
    }

    /**
     * Loggar ut (revoke refresh token)
     */
    protected static void logout() {
        if (refreshToken == null) {
            return;
        }

        String logoutBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        RestAssured.given()
                .spec(requestSpec)
                .body(logoutBody)
                .when()
                .post(AUTH_LOGOUT)
                .then()
                .statusCode(200);

        accessToken = null;
        refreshToken = null;

        System.out.println("👋 Logged out");
    }

    // ===========================================
    // HJÄLPMETODER - LOGGING
    // ===========================================

    protected void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }

    protected void logTestStart(String testName) {
        System.out.println("\n🧪 Testing: " + testName);
    }
}
