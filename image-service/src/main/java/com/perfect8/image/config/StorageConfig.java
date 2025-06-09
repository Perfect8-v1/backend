// image-service/src/main/java/com/perfect8/image/config/StorageConfig.java
//

        package com.perfect8.image.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.max-file-size}")
    private Long maxFileSize;

    @Value("${storage.allowed-extensions}")
    private String[] allowedExtensions;

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }
}