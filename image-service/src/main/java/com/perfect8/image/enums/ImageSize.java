package com.perfect8.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageSize {

    THUMBNAIL(150, 150, "thumbnail"),
    SMALL(400, 400, "small"),
    MEDIUM(800, 800, "medium"),
    LARGE(1600, 1600, "large");

    private final int width;
    private final int height;
    private final String directory;

    public boolean maintainsAspectRatio() {
        return true; // All sizes maintain aspect ratio
    }

    public String getFilePath(String baseDir, String filename) {
        return baseDir + "/" + directory + "/" + filename;
    }

    public static ImageSize fromString(String size) {
        if (size == null) return null;

        try {
            return ImageSize.valueOf(size.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by directory name
            for (ImageSize imageSize : ImageSize.values()) {
                if (imageSize.getDirectory().equalsIgnoreCase(size)) {
                    return imageSize;
                }
            }
            return null;
        }
    }
}