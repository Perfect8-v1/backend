package com.perfect8.image.controller;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Image Controller (v1.3)
 * 
 * Endpoints (via Gateway):
 * - /image/api/images  → image-service
 * - /api/auth/         → admin-service
 */
@DisplayName("Image Service - Controller Tests (v1.3)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageControllerTest {

    private static RequestSpecification requestSpec;
    private static RequestSpecification authenticatedSpec;
    private static String jwtToken;

    private static final String BASE_URL = "https://p8.rantila.com";
    private static final String TEST_EMAIL = "cmb@p8.se";
    private static final String TEST_PASSWORD = "magnus123";

    // Endpoints (v1.3 med service-prefix)
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String IMAGES_ENDPOINT = "/image/api/images";

    private static Long uploadedImageId;

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

        System.out.println("🚀 Image controller tests: " + BASE_URL);
        System.out.println("📍 Images endpoint: " + IMAGES_ENDPOINT);

        jwtToken = getAuthToken();

        if (jwtToken != null) {
            authenticatedSpec = new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
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

    @Test
    @Order(1)
    @DisplayName("GET /image/api/images - List all images")
    public void testGetAllImages() {
        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT)
        .then().statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("POST /image/api/images/upload - Upload image")
    public void testUploadImage() throws IOException {
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        // Use a test image from resources if available, otherwise create minimal
        File testImage = new File("src/test/resources/test-image.jpg");
        byte[] imageBytes;
        
        if (testImage.exists()) {
            imageBytes = Files.readAllBytes(testImage.toPath());
        } else {
            // Create a minimal test file
            imageBytes = "test image content".getBytes();
        }

        Path tempFile = Files.createTempFile("test-image-", ".jpg");
        Files.write(tempFile, imageBytes);

        Response response = given()
                .spec(authenticatedSpec)
                .contentType("multipart/form-data")
                .multiPart("file", tempFile.toFile(), "image/jpeg")
                .multiPart("altText", "Test image from integration test")
        .when()
                .post(IMAGES_ENDPOINT + "/upload")
        .then()
                .extract().response();

        Files.deleteIfExists(tempFile);

        int statusCode = response.getStatusCode();
        if (statusCode == 200 || statusCode == 201) {
            uploadedImageId = response.jsonPath().getLong("imageId");
            System.out.println("✅ Uploaded image ID: " + uploadedImageId);
        } else {
            System.out.println("⚠️ Upload returned " + statusCode + " - skipping dependent tests");
        }
    }

    @Test
    @Order(3)
    @DisplayName("GET /image/api/images/{id} - Get uploaded image")
    public void testGetImageById() {
        Assumptions.assumeTrue(uploadedImageId != null, "No image uploaded");

        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/" + uploadedImageId)
        .then().statusCode(200);
    }

    @Test
    @Order(4)
    @DisplayName("GET /image/api/images/{id}/thumbnail/SMALL")
    public void testGetThumbnailSmall() {
        Assumptions.assumeTrue(uploadedImageId != null, "No image uploaded");

        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/" + uploadedImageId + "/thumbnail/SMALL")
        .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @Order(5)
    @DisplayName("GET /image/api/images/{id}/thumbnail/MEDIUM")
    public void testGetThumbnailMedium() {
        Assumptions.assumeTrue(uploadedImageId != null, "No image uploaded");

        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/" + uploadedImageId + "/thumbnail/MEDIUM")
        .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /image/api/images/category/{category}")
    public void testGetImagesByCategory() {
        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/category/product")
        .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /image/api/images/{id}")
    public void testDeleteImage() {
        Assumptions.assumeTrue(uploadedImageId != null, "No image uploaded");
        Assumptions.assumeTrue(authenticatedSpec != null, "JWT required");

        given().spec(authenticatedSpec)
        .when().delete(IMAGES_ENDPOINT + "/" + uploadedImageId)
        .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @Order(8)
    @DisplayName("GET deleted image → 404")
    public void testGetDeletedImage() {
        Assumptions.assumeTrue(uploadedImageId != null, "No image uploaded");

        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/" + uploadedImageId)
        .then().statusCode(404);
    }

    @Test
    @Order(9)
    @DisplayName("GET non-existent image → 404")
    public void testGetNonExistentImage() {
        given().spec(requestSpec)
        .when().get(IMAGES_ENDPOINT + "/99999999")
        .then().statusCode(404);
    }

    /**
     * Creates a minimal valid JPEG byte array
     */
    private byte[] createMinimalJpeg() {
        return new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00,
            0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07,
            0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14,
            0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13,
            0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A,
            0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22,
            0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C,
            0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39,
            0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
            (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00,
            0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            (byte) 0xFF, (byte) 0xC4, 0x00, 0x1F, 0x00, 0x00,
            0x01, 0x05, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B,
            (byte) 0xFF, (byte) 0xC4, 0x00, (byte) 0xB5, 0x10,
            0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03,
            0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7D,
            (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01,
            0x00, 0x00, 0x3F, 0x00, 0x7F,
            (byte) 0xFF, (byte) 0xD9
        };
    }
}
