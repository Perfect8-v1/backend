package com.perfect8.shop.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;
    private final String errorCode;

    public EmailAlreadyExistsException(String message) {
        super(message);
        this.email = null;
        this.errorCode = "EMAIL_ALREADY_EXISTS";
    }

    public EmailAlreadyExistsException(String email, String customMessage) {
        super(customMessage);
        this.email = email;
        this.errorCode = "EMAIL_ALREADY_EXISTS";
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.email = null;
        this.errorCode = "EMAIL_ALREADY_EXISTS";
    }

    public EmailAlreadyExistsException(String email, String customMessage, String errorCode) {
        super(customMessage);
        this.email = email;
        this.errorCode = errorCode != null ? errorCode : "EMAIL_ALREADY_EXISTS";
    }
}