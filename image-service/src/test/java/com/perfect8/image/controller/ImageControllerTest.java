package com.perfect8.image.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured tests for ImageController (v1.3)
 * 
 * Endpoints:
 * - /api/images  → image-service
 * - /api/auth/   → admin-service
 */
@DisplayName("Image Service - Controller Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageControllerTest {

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String API_PATH = "/api/images";
    private static final String AUTH_PATH = "/api/auth";

    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    private static String jwtToken;
    private static Long uploadedImageId;

    @BeforeAll
    public static void setup() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("🚀 Image Controller tests: " + BASE_URL);

        jwtToken = getAuthToken();
        if (jwtToken != null) {
            System.out.println("✅ JWT acquired");
        }
    }

    private static String getAuthToken() {
        try {
            String loginJson = """
                    {"email": "%s", "password": "%s"}
                    """.formatted(TEST_EMAIL, TEST_PASSWORD);

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(loginJson)
            .when()
                    .post(AUTH_PATH + "/login")
            .then()
                    .statusCode(200)
                    .extract().response();

            return response.jsonPath().getString("accessToken");
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Health check")
    public void testHealthCheck() {
        given()
        .when().get(API_PATH + "/health")
        .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @Order(2)
    @DisplayName("Upload image → 201")
    public void testUploadImage() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        File testImage = new File("src/test/resources/test-image.jpg");

        if (!testImage.exists()) {
            System.out.println("⚠️ Skipping - no test image");
            return;
        }

        Response response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .multiPart("file", testImage)
                .multiPart("altText", "REST Assured Test (v1.3)")
                .multiPart("category", "test")
        .when()
                .post(API_PATH + "/upload")
        .then()
                .statusCode(201)
                .body("imageId", notNullValue())
                .extract().response();

        uploadedImageId = response.jsonPath().getLong("imageId");
        System.out.println("✅ Uploaded image ID: " + uploadedImageId);
    }

    @Test
    @Order(3)
    @DisplayName("Get image by ID → 200")
    public void testGetImageById() {
        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
        .when().get(API_PATH + "/" + uploadedImageId)
        .then().statusCode(200).body("imageId", equalTo(uploadedImageId.intValue()));
    }

    @Test
    @Order(4)
    @DisplayName("Get images by category")
    public void testGetImagesByCategory() {
        given()
        .when().get(API_PATH + "/category/test")
        .then().statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test
    @Order(5)
    @DisplayName("Update image metadata → 200")
    public void testUpdateImageMetadata() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .param("altText", "Updated Alt Text (v1.3)")
                .param("category", "updated-test")
        .when()
                .put(API_PATH + "/" + uploadedImageId)
        .then()
                .statusCode(200)
                .body("altText", equalTo("Updated Alt Text (v1.3)"));
    }

    @Test
    @Order(6)
    @DisplayName("Get non-existent image → 404")
    public void testGetNonExistentImage() {
        given()
        .when().get(API_PATH + "/999999")
        .then().statusCode(404);
    }

    @Test
    @Order(7)
    @DisplayName("Upload invalid file type → 400")
    public void testUploadInvalidFileType() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        File invalidFile = new File("src/test/resources/test-file.txt");

        if (!invalidFile.exists()) {
            System.out.println("⚠️ Skipping - no test-file.txt");
            return;
        }

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .multiPart("file", invalidFile)
        .when()
                .post(API_PATH + "/upload")
        .then()
                .statusCode(400);
    }

    @Test
    @Order(8)
    @DisplayName("Delete image → 204")
    public void testDeleteImage() {
        Assumptions.assumeTrue(jwtToken != null, "JWT required");

        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
                .header("Authorization", "Bearer " + jwtToken)
        .when()
                .delete(API_PATH + "/" + uploadedImageId)
        .then()
                .statusCode(204);

        System.out.println("✅ Deleted image ID: " + uploadedImageId);
    }

    @Test
    @Order(9)
    @DisplayName("Verify deleted image → 404")
    public void testVerifyImageDeleted() {
        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
        .when().get(API_PATH + "/" + uploadedImageId)
        .then().statusCode(404);
    }
}
