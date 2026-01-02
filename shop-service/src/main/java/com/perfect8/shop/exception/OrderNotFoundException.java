package com.perfect8.shop.exception;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {

    private final Long orderId;
    private final String orderNumber;
    private final String errorCode;

    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
        this.orderNumber = null;
        this.errorCode = "ORDER_NOT_FOUND";
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.orderNumber = null;
        this.errorCode = "ORDER_NOT_FOUND";
    }

    public OrderNotFoundException(Long orderId, String message) {
        super(message);
        this.orderId = orderId;
        this.orderNumber = null;
        this.errorCode = "ORDER_NOT_FOUND";
    }

    public OrderNotFoundException(String orderNumber, String message, boolean isOrderNumber) {
        super(message);
        this.orderId = null;
        this.orderNumber = orderNumber;
        this.errorCode = "ORDER_NOT_FOUND";
    }
}