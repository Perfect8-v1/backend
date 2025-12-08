package com.perfect8.image.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured tests for ImageController
 *
 * Tests run against production server: p8.rantila.com
 * Requires a test image file in src/test/resources/
 *
 * Run in IntelliJ: Right-click -> Run
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageControllerTest {

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String API_PATH = "/api/v1/images";
    private static final String AUTH_PATH = "/api/v1/auth";

    private static String jwtToken;
    private static Long uploadedImageId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        jwtToken = getAuthToken();
    }

    private static String getAuthToken() {
        String loginJson = "{\"email\":\"admin@perfect8.com\",\"password\":\"Admin123!\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(AUTH_PATH + "/login")
                .then()
                .statusCode(200)
                .extract().response();

        String token = response.jsonPath().getString("token");
        System.out.println("✅ JWT-token hämtad");
        return token;
    }

    @Test
    @Order(1)
    @DisplayName("Health check - should return 200")
    public void testHealthCheck() {
        given()
                .when()
                .get(API_PATH + "/health")
                .then()
                .statusCode(200)
                .body(containsString("running"));
    }

    @Test
    @Order(2)
    @DisplayName("Upload image - should return 201 with ImageDto")
    public void testUploadImage() {
        File testImage = new File("src/test/resources/test-image.jpg");

        if (!testImage.exists()) {
            System.out.println("⚠️ Skipping upload test - no test image found");
            System.out.println("   Add a test-image.jpg to src/test/resources/");
            return;
        }

        Response response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .multiPart("file", testImage)
                .multiPart("altText", "REST Assured Test Image")
                .multiPart("category", "test")
                .when()
                .post(API_PATH + "/upload")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("imageId", notNullValue())
                .body("originalFilename", equalTo("test-image.jpg"))
                .body("category", equalTo("test"))
                .body("altText", equalTo("REST Assured Test Image"))
                .body("imageStatus", equalTo("ACTIVE"))
                .body("thumbnailUrl", notNullValue())
                .body("smallUrl", notNullValue())
                .body("mediumUrl", notNullValue())
                .body("largeUrl", notNullValue())
                .extract().response();

        uploadedImageId = response.jsonPath().getLong("imageId");
        System.out.println("✅ Uploaded image with ID: " + uploadedImageId);
    }

    @Test
    @Order(3)
    @DisplayName("Get image by ID - should return 200 with ImageDto")
    public void testGetImageById() {
        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
                .when()
                .get(API_PATH + "/" + uploadedImageId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("imageId", equalTo(uploadedImageId.intValue()))
                .body("category", equalTo("test"))
                .body("imageStatus", equalTo("ACTIVE"));
    }

    @Test
    @Order(4)
    @DisplayName("Get images by category - should return list")
    public void testGetImagesByCategory() {
        given()
                .when()
                .get(API_PATH + "/category/test")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test
    @Order(5)
    @DisplayName("Update image metadata - should return 200")
    public void testUpdateImageMetadata() {
        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .param("altText", "Updated Alt Text")
                .param("category", "updated-test")
                .when()
                .put(API_PATH + "/" + uploadedImageId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("altText", equalTo("Updated Alt Text"))
                .body("category", equalTo("updated-test"));
    }

    @Test
    @Order(6)
    @DisplayName("Get non-existent image - should return 404")
    public void testGetNonExistentImage() {
        given()
                .when()
                .get(API_PATH + "/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    @DisplayName("Upload invalid file type - should return 400")
    public void testUploadInvalidFileType() {
        File invalidFile = new File("src/test/resources/test-file.txt");

        if (!invalidFile.exists()) {
            System.out.println("⚠️ Skipping invalid file test - no test-file.txt found");
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
    @DisplayName("Delete image - should return 204")
    public void testDeleteImage() {
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

        System.out.println("✅ Deleted image with ID: " + uploadedImageId);
    }

    @Test
    @Order(9)
    @DisplayName("Verify deleted image is gone - should return 404")
    public void testVerifyImageDeleted() {
        if (uploadedImageId == null) {
            System.out.println("⚠️ Skipping - no image uploaded");
            return;
        }

        given()
                .when()
                .get(API_PATH + "/" + uploadedImageId)
                .then()
                .statusCode(404);
    }
}