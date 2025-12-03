package com.perfect8.admin.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Admin Service Health Check
 * Tests the /actuator/health endpoint
 */
@DisplayName("Admin Service - Health Check Tests")
public class HealthCheckTest extends BaseTest {

    private static final String HEALTH_ENDPOINT = "/actuator/health";

    @Test
    @DisplayName("Health endpoint should return UP status")
    public void testHealthEndpoint_ReturnsUp() {
        System.out.println("\n🧪 Testing: Health Check - Should return UP");

        Response response = given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .extract()
                .response();

        // Log response for debugging
        System.out.println("Response: " + response.asString());
        logTestResult("Health Check", true);
    }

    @Test
    @DisplayName("Health endpoint should return database status")
    public void testHealthEndpoint_ContainsDatabaseStatus() {
        System.out.println("\n🧪 Testing: Health Check - Should contain database info");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("components.db", notNullValue())
                .body("components.db.status", equalTo("UP"));

        logTestResult("Database Status Check", true);
    }

    @Test
    @DisplayName("Health endpoint should have disk space info")
    public void testHealthEndpoint_ContainsDiskSpaceStatus() {
        System.out.println("\n🧪 Testing: Health Check - Should contain disk space info");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .body("components.diskSpace", notNullValue())
                .body("components.diskSpace.status", equalTo("UP"));

        logTestResult("Disk Space Check", true);
    }

    @Test
    @DisplayName("Health endpoint should respond quickly")
    public void testHealthEndpoint_ResponseTime() {
        System.out.println("\n🧪 Testing: Health Check - Response time should be < 2 seconds");

        given()
                .spec(requestSpec)
        .when()
                .get(HEALTH_ENDPOINT)
        .then()
                .statusCode(200)
                .time(lessThan(2000L)); // Less than 2 seconds

        logTestResult("Response Time Check", true);
    }
}
