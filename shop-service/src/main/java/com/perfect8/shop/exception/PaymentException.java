package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PaymentException extends RuntimeException {

    private String errorCode;
    private String paymentMethod;
    private String transactionId;
    private String providerErrorCode;
    private String providerErrorMessage;

    // Default constructor
    public PaymentException() {
        super();
    }

    // Constructor with message
    public PaymentException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with error details
    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    // Constructor with payment context
    public PaymentException(String message, String errorCode, String paymentMethod, String transactionId) {
        super(message);
        this.errorCode = errorCode;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }

    // Constructor with provider error details
    public PaymentException(String message, String errorCode, String paymentMethod,
                            String transactionId, String providerErrorCode, String providerErrorMessage) {
        super(message);
        this.errorCode = errorCode;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.providerErrorCode = providerErrorCode;
        this.providerErrorMessage = providerErrorMessage;
    }

    // Static factory methods for common payment errors
    public static PaymentException insufficientFunds(String transactionId) {
        return new PaymentException(
                "Insufficient funds for payment",
                "INSUFFICIENT_FUNDS",
                null,
                transactionId
        );
    }

    public static PaymentException invalidCard(String cardNumber) {
        String maskedCard = maskCardNumber(cardNumber);
        return new PaymentException(
                "Invalid card details for card ending in " + maskedCard,
                "INVALID_CARD"
        );
    }

    public static PaymentException paymentDeclined(String reason) {
        return new PaymentException(
                "Payment was declined: " + reason,
                "PAYMENT_DECLINED"
        );
    }

    public static PaymentException processingError(String message) {
        return new PaymentException(
                "Payment processing error: " + message,
                "PROCESSING_ERROR"
        );
    }

    public static PaymentException providerError(String provider, String providerCode, String providerMessage) {
        return new PaymentException(
                "Payment provider error: " + providerMessage,
                "PROVIDER_ERROR",
                provider,
                null,
                providerCode,
                providerMessage
        );
    }

    public static PaymentException expiredCard() {
        return new PaymentException(
                "Credit card has expired",
                "EXPIRED_CARD"
        );
    }

    public static PaymentException invalidAmount(String amount) {
        return new PaymentException(
                "Invalid payment amount: " + amount,
                "INVALID_AMOUNT"
        );
    }

    public static PaymentException duplicateTransaction(String transactionId) {
        return new PaymentException(
                "Duplicate transaction detected",
                "DUPLICATE_TRANSACTION",
                null,
                transactionId
        );
    }

    public static PaymentException timeoutError() {
        return new PaymentException(
                "Payment processing timed out",
                "TIMEOUT_ERROR"
        );
    }

    public static PaymentException unsupportedPaymentMethod(String paymentMethod) {
        return new PaymentException(
                "Unsupported payment method: " + paymentMethod,
                "UNSUPPORTED_METHOD",
                paymentMethod,
                null
        );
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public void setProviderErrorCode(String providerErrorCode) {
        this.providerErrorCode = providerErrorCode;
    }

    public String getProviderErrorMessage() {
        return providerErrorMessage;
    }

    public void setProviderErrorMessage(String providerErrorMessage) {
        this.providerErrorMessage = providerErrorMessage;
    }

    // Utility methods
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }

    public boolean hasTransactionId() {
        return transactionId != null && !transactionId.trim().isEmpty();
    }

    public boolean hasProviderError() {
        return providerErrorCode != null || providerErrorMessage != null;
    }

    public boolean isRetryable() {
        if (errorCode == null) {
            return false;
        }

        // Define which errors are retryable
        switch (errorCode) {
            case "TIMEOUT_ERROR":
            case "PROCESSING_ERROR":
            case "PROVIDER_ERROR":
            case "NETWORK_ERROR":
                return true;
            case "INSUFFICIENT_FUNDS":
            case "INVALID_CARD":
            case "EXPIRED_CARD":
            case "PAYMENT_DECLINED":
            case "DUPLICATE_TRANSACTION":
                return false;
            default:
                return false;
        }
    }

    public boolean isCardRelated() {
        if (errorCode == null) {
            return false;
        }

        switch (errorCode) {
            case "INVALID_CARD":
            case "EXPIRED_CARD":
            case "INSUFFICIENT_FUNDS":
                return true;
            default:
                return false;
        }
    }

    public boolean isProviderRelated() {
        return "PROVIDER_ERROR".equals(errorCode) || hasProviderError();
    }

    public String getUserFriendlyMessage() {
        if (errorCode == null) {
            return getMessage();
        }

        switch (errorCode) {
            case "INSUFFICIENT_FUNDS":
                return "Your payment was declined due to insufficient funds. Please try a different payment method.";
            case "INVALID_CARD":
                return "The card details you entered are invalid. Please check and try again.";
            case "EXPIRED_CARD":
                return "Your card has expired. Please use a different card.";
            case "PAYMENT_DECLINED":
                return "Your payment was declined by your bank. Please contact your bank or try a different payment method.";
            case "TIMEOUT_ERROR":
                return "Payment processing timed out. Please try again.";
            case "PROCESSING_ERROR":
                return "There was an error processing your payment. Please try again.";
            case "DUPLICATE_TRANSACTION":
                return "This transaction has already been processed.";
            case "UNSUPPORTED_METHOD":
                return "This payment method is not supported. Please choose a different method.";
            default:
                return "Payment could not be processed. Please try again or contact support.";
        }
    }

    // Helper method to mask card numbers
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public String toString() {
        return "PaymentException{" +
                "message='" + getMessage() + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", providerErrorCode='" + providerErrorCode + '\'' +
                ", providerErrorMessage='" + providerErrorMessage + '\'' +
                '}';
    }
}