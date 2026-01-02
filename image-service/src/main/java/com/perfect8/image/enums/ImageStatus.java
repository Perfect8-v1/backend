package com.perfect8.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageStatus {

    PENDING("Image is pending processing"),
    PROCESSING("Image is being processed"),
    ACTIVE("Image is active and available"),
    FAILED("Image processing failed"),
    DELETED("Image has been deleted");

    private final String description;

    public boolean isAvailable() {
        return this == ACTIVE;
    }

    public boolean isProcessing() {
        return this == PROCESSING || this == PENDING;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}