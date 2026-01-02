package com.perfect8.shop.exception;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends RuntimeException {

    private final Long customerId;
    private final String email;
    private final String errorCode;

    public CustomerNotFoundException(String message) {
        super(message);
        this.customerId = null;
        this.email = null;
        this.errorCode = "CUSTOMER_NOT_FOUND";
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.customerId = null;
        this.email = null;
        this.errorCode = "CUSTOMER_NOT_FOUND";
    }

    public CustomerNotFoundException(Long customerId, String message) {
        super(message);
        this.customerId = customerId;
        this.email = null;
        this.errorCode = "CUSTOMER_NOT_FOUND";
    }

    public CustomerNotFoundException(String email, String message, boolean isEmail) {
        super(message);
        this.customerId = null;
        this.email = email;
        this.errorCode = "CUSTOMER_NOT_FOUND";
    }
}