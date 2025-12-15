package com.perfect8.admin.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Admin Service Authentication
 * Tests the /api/auth/* endpoints
 */
@DisplayName("Admin Service - Authentication Tests")
public class AuthControllerTest extends BaseTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";

    @Test
    @DisplayName("Login with valid credentials should return JWT token")
    public void testLogin_ValidCredentials_ReturnsToken() {
        System.out.println("\n🧪 Testing: Login - Valid credentials should return token");

        // Login request body
        String loginRequest = """
                {
                    "email": "newadmin@perfect8.com",
                    "password": "admin123"
                }
                """;

        Response response = given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("token", not(emptyString()))
                .extract()
                .response();

        // Extract and log token (for debugging)
        String token = response.jsonPath().getString("token");
        System.out.println("✅ Token received: " + token.substring(0, 20) + "...");
        
        logTestResult("Login with valid credentials", true);
    }

    @Test
    @DisplayName("Login with invalid password should return 401")
    public void testLogin_InvalidPassword_Returns401() {
        System.out.println("\n🧪 Testing: Login - Invalid password should return 401");

        String loginRequest = """
                {
                    "email": "admin@perfect8.com",
                    "password": "wrongpassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(401);

        logTestResult("Login with invalid password", true);
    }

    @Test
    @DisplayName("Login with non-existent email should return 401")
    public void testLogin_NonExistentEmail_Returns401() {
        System.out.println("\n🧪 Testing: Login - Non-existent email should return 401");

        String loginRequest = """
                {
                    "email": "doesnotexist@perfect8.com",
                    "password": "somepassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(401);

        logTestResult("Login with non-existent email", true);
    }

    @Test
    @DisplayName("Login with missing email should return 400")
    public void testLogin_MissingEmail_Returns400() {
        System.out.println("\n🧪 Testing: Login - Missing email should return 400");

        String loginRequest = """
                {
                    "password": "somepassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(400);

        logTestResult("Login with missing email", true);
    }

    @Test
    @DisplayName("Login with missing password should return 400")
    public void testLogin_MissingPassword_Returns400() {
        System.out.println("\n🧪 Testing: Login - Missing password should return 400");

        String loginRequest = """
                {
                    "email": "admin@perfect8.com"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(400);

        logTestResult("Login with missing password", true);
    }

    @Test
    @DisplayName("Login with invalid JSON should return 400")
    public void testLogin_InvalidJson_Returns400() {
        System.out.println("\n🧪 Testing: Login - Invalid JSON should return 400");

        String invalidJson = "{ this is not valid json }";

        given()
                .spec(requestSpec)
                .body(invalidJson)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(400);

        logTestResult("Login with invalid JSON", true);
    }

    @Test
    @DisplayName("Login endpoint should respond quickly")
    public void testLogin_ResponseTime() {
        System.out.println("\n🧪 Testing: Login - Response time should be < 3 seconds");

        String loginRequest = """
                {
                    "email": "newadmin@perfect8.com",
                    "password": "admin123!"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginRequest)
        .when()
                .post(LOGIN_ENDPOINT)
        .then()
                .statusCode(200)
                .time(lessThan(3000L)); // Less than 3 seconds

        logTestResult("Login response time", true);
    }
}
