package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when payment processing fails.
 * This includes payment authorization failures, gateway errors,
 * invalid payment data, and other payment-related issues.
 */
@ResponseStatus(value = HttpStatus.PAYMENT_REQUIRED)
public class PaymentProcessingException extends RuntimeException {

    private String paymentId;
    private String transactionId;
    private String errorCode;
    private PaymentErrorType errorType;

    /**
     * Enum for categorizing payment errors
     */
    public enum PaymentErrorType {
        INSUFFICIENT_FUNDS,
        INVALID_CARD,
        EXPIRED_CARD,
        GATEWAY_ERROR,
        NETWORK_ERROR,
        INVALID_AMOUNT,
        DUPLICATE_TRANSACTION,
        FRAUD_SUSPECTED,
        AUTHENTICATION_FAILED,
        AUTHORIZATION_FAILED,
        CAPTURE_FAILED,
        REFUND_FAILED,
        UNKNOWN
    }

    /**
     * Default constructor
     */
    public PaymentProcessingException() {
        super("Payment processing failed");
        this.errorType = PaymentErrorType.UNKNOWN;
    }

    /**
     * Constructor with message
     */
    public PaymentProcessingException(String message) {
        super(message);
        this.errorType = PaymentErrorType.UNKNOWN;
    }

    /**
     * Constructor with message and cause
     */
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = PaymentErrorType.UNKNOWN;
    }

    /**
     * Constructor with message and error type
     */
    public PaymentProcessingException(String message, PaymentErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Constructor with message, cause, and error type
     */
    public PaymentProcessingException(String message, Throwable cause, PaymentErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * Full constructor with all details
     */
    public PaymentProcessingException(String message, String paymentId, String transactionId,
                                      String errorCode, PaymentErrorType errorType) {
        super(message);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    /**
     * Full constructor with cause
     */
    public PaymentProcessingException(String message, Throwable cause, String paymentId,
                                      String transactionId, String errorCode, PaymentErrorType errorType) {
        super(message, cause);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    // Static factory methods for common scenarios

    /**
     * Create exception for insufficient funds
     */
    public static PaymentProcessingException insufficientFunds(String paymentId) {
        return new PaymentProcessingException(
                "Payment failed due to insufficient funds",
                paymentId,
                null,
                "INSUFFICIENT_FUNDS",
                PaymentErrorType.INSUFFICIENT_FUNDS
        );
    }

    /**
     * Create exception for invalid card
     */
    public static PaymentProcessingException invalidCard(String message) {
        return new PaymentProcessingException(
                "Invalid card information: " + message,
                PaymentErrorType.INVALID_CARD
        );
    }

    /**
     * Create exception for expired card
     */
    public static PaymentProcessingException expiredCard() {
        return new PaymentProcessingException(
                "Card has expired",
                PaymentErrorType.EXPIRED_CARD
        );
    }

    /**
     * Create exception for gateway error
     */
    public static PaymentProcessingException gatewayError(String gatewayMessage) {
        return new PaymentProcessingException(
                "Payment gateway error: " + gatewayMessage,
                PaymentErrorType.GATEWAY_ERROR
        );
    }

    /**
     * Create exception for network error
     */
    public static PaymentProcessingException networkError(Throwable cause) {
        return new PaymentProcessingException(
                "Network error during payment processing",
                cause,
                PaymentErrorType.NETWORK_ERROR
        );
    }

    /**
     * Create exception for invalid amount
     */
    public static PaymentProcessingException invalidAmount(String amount) {
        return new PaymentProcessingException(
                "Invalid payment amount: " + amount,
                PaymentErrorType.INVALID_AMOUNT
        );
    }

    /**
     * Create exception for duplicate transaction
     */
    public static PaymentProcessingException duplicateTransaction(String transactionId) {
        return new PaymentProcessingException(
                "Duplicate transaction detected",
                null,
                transactionId,
                "DUPLICATE_TRANSACTION",
                PaymentErrorType.DUPLICATE_TRANSACTION
        );
    }

    /**
     * Create exception for suspected fraud
     */
    public static PaymentProcessingException fraudSuspected(String reason) {
        return new PaymentProcessingException(
                "Payment blocked due to suspected fraud: " + reason,
                PaymentErrorType.FRAUD_SUSPECTED
        );
    }

    /**
     * Create exception for authentication failure
     */
    public static PaymentProcessingException authenticationFailed() {
        return new PaymentProcessingException(
                "Payment authentication failed",
                PaymentErrorType.AUTHENTICATION_FAILED
        );
    }

    /**
     * Create exception for authorization failure
     */
    public static PaymentProcessingException authorizationFailed(String reason) {
        return new PaymentProcessingException(
                "Payment authorization failed: " + reason,
                PaymentErrorType.AUTHORIZATION_FAILED
        );
    }

    /**
     * Create exception for capture failure
     */
    public static PaymentProcessingException captureFailed(String paymentId, String reason) {
        return new PaymentProcessingException(
                "Failed to capture payment: " + reason,
                paymentId,
                null,
                "CAPTURE_FAILED",
                PaymentErrorType.CAPTURE_FAILED
        );
    }

    /**
     * Create exception for refund failure
     */
    public static PaymentProcessingException refundFailed(String paymentId, String reason) {
        return new PaymentProcessingException(
                "Refund failed: " + reason,
                paymentId,
                null,
                "REFUND_FAILED",
                PaymentErrorType.REFUND_FAILED
        );
    }

    // Getters and setters

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public PaymentErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(PaymentErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Check if this is a retryable error
     */
    public boolean isRetryable() {
        return errorType == PaymentErrorType.NETWORK_ERROR ||
                errorType == PaymentErrorType.GATEWAY_ERROR;
    }

    /**
     * Check if this requires customer action
     */
    public boolean requiresCustomerAction() {
        return errorType == PaymentErrorType.INSUFFICIENT_FUNDS ||
                errorType == PaymentErrorType.INVALID_CARD ||
                errorType == PaymentErrorType.EXPIRED_CARD ||
                errorType == PaymentErrorType.AUTHENTICATION_FAILED;
    }

    /**
     * Get user-friendly error message
     */
    public String getUserFriendlyMessage() {
        switch (errorType) {
            case INSUFFICIENT_FUNDS:
                return "Your payment could not be processed due to insufficient funds. Please use a different payment method.";
            case INVALID_CARD:
                return "The card information provided is invalid. Please check and try again.";
            case EXPIRED_CARD:
                return "Your card has expired. Please use a different payment method.";
            case GATEWAY_ERROR:
            case NETWORK_ERROR:
                return "We're experiencing technical difficulties. Please try again later.";
            case INVALID_AMOUNT:
                return "The payment amount is invalid. Please contact support.";
            case DUPLICATE_TRANSACTION:
                return "This transaction has already been processed.";
            case FRAUD_SUSPECTED:
                return "This transaction could not be completed. Please contact your bank.";
            case AUTHENTICATION_FAILED:
                return "Payment authentication failed. Please verify your payment details.";
            case AUTHORIZATION_FAILED:
                return "Payment authorization failed. Please contact your bank or try a different payment method.";
            case CAPTURE_FAILED:
                return "Payment could not be completed. Please try again or contact support.";
            case REFUND_FAILED:
                return "Refund could not be processed. Please contact support.";
            default:
                return "Payment processing failed. Please try again or contact support.";
        }
    }

    @Override
    public String toString() {
        return "PaymentProcessingException{" +
                "message='" + getMessage() + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorType=" + errorType +
                ", retryable=" + isRetryable() +
                '}';
    }
}