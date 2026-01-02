package com.perfect8.shop.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class InvalidOrderStatusException extends RuntimeException {

    private final Long orderId;
    private final String currentStatus;
    private final String attemptedStatus;
    private final List<String> validStatuses;
    private final String errorCode;

    public InvalidOrderStatusException(String message) {
        super(message);
        this.orderId = null;
        this.currentStatus = null;
        this.attemptedStatus = null;
        this.validStatuses = null;
        this.errorCode = "INVALID_ORDER_STATUS";
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.currentStatus = null;
        this.attemptedStatus = null;
        this.validStatuses = null;
        this.errorCode = "INVALID_ORDER_STATUS";
    }

    public InvalidOrderStatusException(Long orderId, String currentStatus, String attemptedStatus) {
        super(String.format("Cannot change order %d status from %s to %s", orderId, currentStatus, attemptedStatus));
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
        this.validStatuses = null;
        this.errorCode = "INVALID_STATUS_TRANSITION";
    }

    public InvalidOrderStatusException(Long orderId, String currentStatus, String attemptedStatus, List<String> validStatuses) {
        super(String.format("Cannot change order %d status from %s to %s. Valid transitions: %s",
                orderId, currentStatus, attemptedStatus, String.join(", ", validStatuses)));
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
        this.validStatuses = validStatuses;
        this.errorCode = "INVALID_STATUS_TRANSITION";
    }

    public InvalidOrderStatusException(Long orderId, String currentStatus, String attemptedStatus, String customMessage) {
        super(customMessage);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
        this.validStatuses = null;
        this.errorCode = "INVALID_STATUS_TRANSITION";
    }
}