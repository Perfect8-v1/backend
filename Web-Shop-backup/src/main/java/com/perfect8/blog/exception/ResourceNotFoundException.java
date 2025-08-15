// blog-service/src/main/java/com/perfect8/blog/exception/ResourceNotFoundException.java

package com.perfect8.blog.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}