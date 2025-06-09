// image-service/src/main/java/com/perfect8/image/exception/StorageException.java
//

package com.perfect8.image.exception;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}