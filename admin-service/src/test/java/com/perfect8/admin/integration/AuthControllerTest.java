package com.perfect8.admin.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Authentication - v1.3
 * 
 * Endpoints:
 * - POST /api/auth/login     - Login med email + password
 * - POST /api/auth/refresh   - Förnya access token
 * - POST /api/auth/logout    - Revoke refresh token
 * 
 * Industristandard: Lösenord skickas över HTTPS, backend hashar med BCrypt
 */
@DisplayName("Auth Controller - v1.3 Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest extends BaseTest {

    // Spara tokens mellan tester
    private static String testAccessToken;
    private static String testRefreshToken;

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    @Order(1)
    @DisplayName("POST /login - Valid credentials → 200 + tokens")
    void login_ValidCredentials_ReturnsTokens() {
        logTestStart("Login with valid credentials");

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
                .post(AUTH_LOGIN)
        .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0))
                .body("user.email", equalTo(TEST_EMAIL))
                .extract()
                .response();

        // Spara för efterföljande tester
        testAccessToken = response.jsonPath().getString("accessToken");
        testRefreshToken = response.jsonPath().getString("refreshToken");

        System.out.println("   Access token: " + testAccessToken.substring(0, 30) + "...");
        System.out.println("   Refresh token: " + testRefreshToken);

        logTestResult("Login valid credentials", true);
    }

    @Test
    @Order(2)
    @DisplayName("POST /login - Wrong password → 401")
    void login_WrongPassword_Returns401() {
        logTestStart("Login with wrong password");

        String loginBody = """
                {
                    "email": "%s",
                    "password": "wrong_password"
                }
                """.formatted(TEST_EMAIL);

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(AUTH_LOGIN)
        .then()
                .statusCode(401);

        logTestResult("Login wrong password → 401", true);
    }

    @Test
    @Order(3)
    @DisplayName("POST /login - Unknown email → 401")
    void login_UnknownEmail_Returns401() {
        logTestStart("Login with unknown email");

        String loginBody = """
                {
                    "email": "unknown@example.com",
                    "password": "anypassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(AUTH_LOGIN)
        .then()
                .statusCode(401);

        logTestResult("Login unknown email → 401", true);
    }

    @Test
    @Order(4)
    @DisplayName("POST /login - Missing email → 400")
    void login_MissingEmail_Returns400() {
        logTestStart("Login with missing email");

        String loginBody = """
                {
                    "password": "somepassword"
                }
                """;

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(AUTH_LOGIN)
        .then()
                .statusCode(400);

        logTestResult("Login missing email → 400", true);
    }

    @Test
    @Order(5)
    @DisplayName("POST /login - Missing password → 400")
    void login_MissingPassword_Returns400() {
        logTestStart("Login with missing password");

        String loginBody = """
                {
                    "email": "%s"
                }
                """.formatted(TEST_EMAIL);

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(AUTH_LOGIN)
        .then()
                .statusCode(400);

        logTestResult("Login missing password → 400", true);
    }

    @Test
    @Order(6)
    @DisplayName("POST /login - Response time < 3s")
    void login_ResponseTime_LessThan3Seconds() {
        logTestStart("Login response time");

        String loginBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_EMAIL, TEST_PASSWORD);

        given()
                .spec(requestSpec)
                .body(loginBody)
        .when()
                .post(AUTH_LOGIN)
        .then()
                .statusCode(200)
                .time(lessThan(3000L));

        logTestResult("Login response time < 3s", true);
    }

    // ==========================================
    // REFRESH TOKEN TESTS
    // ==========================================

    @Test
    @Order(10)
    @DisplayName("POST /refresh - Valid token → 200 + new tokens")
    void refresh_ValidToken_ReturnsNewTokens() {
        logTestStart("Refresh with valid token");

        // Säkerställ att vi har en refresh token
        Assumptions.assumeTrue(testRefreshToken != null, "Requires login test to run first");

        String refreshBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(testRefreshToken);

        Response response = given()
                .spec(requestSpec)
                .body(refreshBody)
        .when()
                .post(AUTH_REFRESH)
        .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract()
                .response();

        String newAccessToken = response.jsonPath().getString("accessToken");
        String newRefreshToken = response.jsonPath().getString("refreshToken");

        // Token rotation: nya tokens ska vara annorlunda
        Assertions.assertNotEquals(testAccessToken, newAccessToken, "Access token should be rotated");
        Assertions.assertNotEquals(testRefreshToken, newRefreshToken, "Refresh token should be rotated");

        // Uppdatera för efterföljande tester
        testAccessToken = newAccessToken;
        testRefreshToken = newRefreshToken;

        System.out.println("   New access token: " + newAccessToken.substring(0, 30) + "...");
        System.out.println("   New refresh token: " + newRefreshToken);

        logTestResult("Refresh valid token", true);
    }

    @Test
    @Order(11)
    @DisplayName("POST /refresh - Invalid token → 401/403")
    void refresh_InvalidToken_ReturnsUnauthorized() {
        logTestStart("Refresh with invalid token");

        String refreshBody = """
                {
                    "refreshToken": "invalid-token-12345"
                }
                """;

        given()
                .spec(requestSpec)
                .body(refreshBody)
        .when()
                .post(AUTH_REFRESH)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));

        logTestResult("Refresh invalid token → 401/403", true);
    }

    @Test
    @Order(12)
    @DisplayName("POST /refresh - Missing token → 400")
    void refresh_MissingToken_Returns400() {
        logTestStart("Refresh with missing token");

        String refreshBody = """
                {
                }
                """;

        given()
                .spec(requestSpec)
                .body(refreshBody)
        .when()
                .post(AUTH_REFRESH)
        .then()
                .statusCode(400);

        logTestResult("Refresh missing token → 400", true);
    }

    // ==========================================
    // LOGOUT TESTS
    // ==========================================

    @Test
    @Order(20)
    @DisplayName("POST /logout - Valid token → 200")
    void logout_ValidToken_Returns200() {
        logTestStart("Logout with valid token");

        // Säkerställ att vi har en refresh token
        Assumptions.assumeTrue(testRefreshToken != null, "Requires login/refresh test to run first");

        String logoutBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(testRefreshToken);

        given()
                .spec(requestSpec)
                .body(logoutBody)
        .when()
                .post(AUTH_LOGOUT)
        .then()
                .statusCode(200);

        logTestResult("Logout valid token → 200", true);
    }

    @Test
    @Order(21)
    @DisplayName("POST /refresh - After logout → 401/403 (token revoked)")
    void refresh_AfterLogout_ReturnsUnauthorized() {
        logTestStart("Refresh after logout (revoked token)");

        // Säkerställ att vi har den gamla (nu revokerade) refresh token
        Assumptions.assumeTrue(testRefreshToken != null, "Requires logout test to run first");

        String refreshBody = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(testRefreshToken);

        given()
                .spec(requestSpec)
                .body(refreshBody)
        .when()
                .post(AUTH_REFRESH)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));

        logTestResult("Refresh after logout → 401/403", true);
    }

    // ==========================================
    // ACCESS TOKEN VALIDATION TESTS
    // ==========================================

    @Test
    @Order(30)
    @DisplayName("Protected endpoint - No token → 401/403")
    void protectedEndpoint_NoToken_ReturnsUnauthorized() {
        logTestStart("Protected endpoint without token");

        given()
                .spec(requestSpec)
        .when()
                .get(ADMIN_USERS)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));

        logTestResult("Protected endpoint no token → 401/403", true);
    }

    @Test
    @Order(31)
    @DisplayName("Protected endpoint - Invalid token → 401/403")
    void protectedEndpoint_InvalidToken_ReturnsUnauthorized() {
        logTestStart("Protected endpoint with invalid token");

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer invalid.jwt.token")
        .when()
                .get(ADMIN_USERS)
        .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));

        logTestResult("Protected endpoint invalid token → 401/403", true);
    }

    @Test
    @Order(32)
    @DisplayName("Protected endpoint - Valid token → 200/2xx")
    void protectedEndpoint_ValidToken_ReturnsSuccess() {
        logTestStart("Protected endpoint with valid token");

        // Logga in på nytt (efter logout)
        login();

        given()
                .spec(withAuth())
        .when()
                .get(ADMIN_USERS)
        .then()
                .statusCode(allOf(
                        greaterThanOrEqualTo(200),
                        lessThan(300)
                ));

        logTestResult("Protected endpoint valid token → 2xx", true);
    }
}
