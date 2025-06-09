// image-service/src/test/java/com/perfect8/image/service/ImageServiceTest.java
package com.perfect8.image.service;

import com.perfect8.image.config.StorageConfig;
import com.perfect8.image.exception.StorageException;
import com.perfect8.image.model.Image;
import com.perfect8.image.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private StorageConfig storageConfig;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        // Setup will be done in individual tests as needed
    }

    @Test
    void uploadImage_Success() throws Exception {
        // Arrange
        when(storageConfig.getMaxFileSize()).thenReturn(10485760L); // 10MB
        when(storageConfig.getAllowedExtensions()).thenReturn(new String[]{"jpg", "jpeg", "png", "gif"});

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image".getBytes()
        );

        Image savedImage = new Image();
        savedImage.setId("123");
        savedImage.setFilename("test.jpg");

        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // Act
        var result = imageService.uploadImage(file);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    void uploadImage_EmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[0]
        );

        // Act & Assert
        assertThrows(StorageException.class, () -> imageService.uploadImage(file));
    }
}