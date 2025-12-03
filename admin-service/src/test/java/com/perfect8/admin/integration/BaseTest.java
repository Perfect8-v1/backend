package com.perfect8.admin.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base test class for all REST Assured integration tests
 * Contains common configuration and setup
 */
public abstract class BaseTest {

    protected static RequestSpecification requestSpec;

    // Base URLs for different environments
    protected static final String PROD_BASE_URL = "http://p8.rantila.com:8081";
    protected static final String LOCAL_BASE_URL = "http://localhost:8081";

    // Choose which environment to test
    protected static final String BASE_URL = PROD_BASE_URL;

    @BeforeAll
    public static void setup() {
        // Configure REST Assured
        RestAssured.baseURI = BASE_URL;

        // Build request specification with common settings
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        System.out.println("🚀 REST Assured configured for: " + BASE_URL);
    }

    /**
     * Helper method to print test results
     */
    protected void logTestResult(String testName, boolean passed) {
        String status = passed ? "✅ PASSED" : "❌ FAILED";
        System.out.println(status + " - " + testName);
    }
}
