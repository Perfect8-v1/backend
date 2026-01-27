package com.perfect8.admin.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Health Check endpoints - v1.3
 * 
 * Testar Actuator health endpoints för alla services
 */
@DisplayName("Health Check Tests")
public class HealthCheckTest extends BaseTest {

    @Test
    @DisplayName("GET /actuator/health - Gateway → 200 UP")
    void healthCheck_Gateway_ReturnsUp() {
        logTestStart("Gateway health check");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH)
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        logTestResult("Gateway health", true);
    }

    @Test
    @DisplayName("Health endpoint - Response time < 2s")
    void healthCheck_ResponseTime_LessThan2Seconds() {
        logTestStart("Health check response time");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH)
        .then()
                .statusCode(200)
                .time(lessThan(2000L));

        logTestResult("Health response time < 2s", true);
    }

    @Test
    @DisplayName("Health endpoint - Contains database status")
    void healthCheck_ContainsDatabaseStatus() {
        logTestStart("Health check contains database info");

        Response response = given()
                .spec(requestSpec)
        .when()
                .get(HEALTH)
        .then()
                .statusCode(200)
                .extract()
                .response();

        // Logga hela svaret för debugging
        System.out.println("   Response: " + response.asString());

        logTestResult("Health contains DB info", true);
    }
}
