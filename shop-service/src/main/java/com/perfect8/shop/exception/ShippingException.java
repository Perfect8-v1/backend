package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when shipping-related operations fail.
 * This includes calculation errors, delivery issues, carrier problems, etc.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ShippingException extends RuntimeException {

    private String trackingNumber;
    private String carrier;
    private String shipmentId;
    private ShippingErrorType errorType;

    /**
     * Enum for categorizing shipping errors
     */
    public enum ShippingErrorType {
        ADDRESS_INVALID,
        ADDRESS_INCOMPLETE,
        CALCULATION_ERROR,
        CARRIER_UNAVAILABLE,
        SERVICE_UNAVAILABLE,
        TRACKING_NOT_FOUND,
        SHIPMENT_NOT_FOUND,
        DELIVERY_FAILED,
        PACKAGE_LOST,
        PACKAGE_DAMAGED,
        CUSTOMS_ISSUE,
        WEIGHT_EXCEEDED,
        DIMENSION_EXCEEDED,
        RESTRICTED_ITEM,
        INVALID_ZIP_CODE,
        CANNOT_CANCEL,
        ALREADY_DELIVERED,
        UNKNOWN
    }

    /**
     * Default constructor
     */
    public ShippingException() {
        super("Shipping operation failed");
        this.errorType = ShippingErrorType.UNKNOWN;
    }

    /**
     * Constructor with message
     */
    public ShippingException(String message) {
        super(message);
        this.errorType = ShippingErrorType.UNKNOWN;
    }

    /**
     * Constructor with message and cause
     */
    public ShippingException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ShippingErrorType.UNKNOWN;
    }

    /**
     * Constructor with message and error type
     */
    public ShippingException(String message, ShippingErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Constructor with message, cause, and error type
     */
    public ShippingException(String message, Throwable cause, ShippingErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * Full constructor with all details
     */
    public ShippingException(String message, String trackingNumber, String carrier,
                             String shipmentId, ShippingErrorType errorType) {
        super(message);
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shipmentId = shipmentId;
        this.errorType = errorType;
    }

    // Static factory methods for common scenarios

    /**
     * Create exception for invalid address
     */
    public static ShippingException invalidAddress(String details) {
        return new ShippingException(
                "Invalid shipping address: " + details,
                ShippingErrorType.ADDRESS_INVALID
        );
    }

    /**
     * Create exception for incomplete address
     */
    public static ShippingException incompleteAddress(String missingFields) {
        return new ShippingException(
                "Incomplete shipping address. Missing: " + missingFields,
                ShippingErrorType.ADDRESS_INCOMPLETE
        );
    }

    /**
     * Create exception for calculation errors
     */
    public static ShippingException calculationError(String reason) {
        return new ShippingException(
                "Shipping calculation failed: " + reason,
                ShippingErrorType.CALCULATION_ERROR
        );
    }

    /**
     * Create exception for carrier unavailability
     */
    public static ShippingException carrierUnavailable(String carrier) {
        return new ShippingException(
                "Carrier unavailable: " + carrier,
                ShippingErrorType.CARRIER_UNAVAILABLE
        );
    }

    /**
     * Create exception for tracking not found
     */
    public static ShippingException trackingNotFound(String trackingNumber) {
        ShippingException ex = new ShippingException(
                "Tracking number not found: " + trackingNumber,
                ShippingErrorType.TRACKING_NOT_FOUND
        );
        ex.trackingNumber = trackingNumber;
        return ex;
    }

    /**
     * Create exception for shipment not found
     */
    public static ShippingException shipmentNotFound(String shipmentId) {
        ShippingException ex = new ShippingException(
                "Shipment not found with ID: " + shipmentId,
                ShippingErrorType.SHIPMENT_NOT_FOUND
        );
        ex.shipmentId = shipmentId;
        return ex;
    }

    /**
     * Create exception for delivery failure
     */
    public static ShippingException deliveryFailed(String trackingNumber, String reason) {
        ShippingException ex = new ShippingException(
                "Delivery failed: " + reason,
                ShippingErrorType.DELIVERY_FAILED
        );
        ex.trackingNumber = trackingNumber;
        return ex;
    }

    /**
     * Create exception for weight exceeded
     */
    public static ShippingException weightExceeded(Double weight, Double maxWeight) {
        return new ShippingException(
                String.format("Package weight %.2f exceeds maximum allowed weight %.2f", weight, maxWeight),
                ShippingErrorType.WEIGHT_EXCEEDED
        );
    }

    /**
     * Create exception for dimension exceeded
     */
    public static ShippingException dimensionExceeded(String dimensions) {
        return new ShippingException(
                "Package dimensions exceed carrier limits: " + dimensions,
                ShippingErrorType.DIMENSION_EXCEEDED
        );
    }

    /**
     * Create exception for restricted items
     */
    public static ShippingException restrictedItem(String itemDescription) {
        return new ShippingException(
                "Cannot ship restricted item: " + itemDescription,
                ShippingErrorType.RESTRICTED_ITEM
        );
    }

    /**
     * Create exception for invalid zip code
     */
    public static ShippingException invalidZipCode(String zipCode) {
        return new ShippingException(
                "Invalid or unsupported ZIP code: " + zipCode,
                ShippingErrorType.INVALID_ZIP_CODE
        );
    }

    /**
     * Create exception for cannot cancel
     */
    public static ShippingException cannotCancel(String reason) {
        return new ShippingException(
                "Cannot cancel shipment: " + reason,
                ShippingErrorType.CANNOT_CANCEL
        );
    }

    /**
     * Create exception for already delivered
     */
    public static ShippingException alreadyDelivered(String trackingNumber) {
        ShippingException ex = new ShippingException(
                "Package already delivered",
                ShippingErrorType.ALREADY_DELIVERED
        );
        ex.trackingNumber = trackingNumber;
        return ex;
    }

    // Getters and setters

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public ShippingErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ShippingErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Check if error is address-related
     */
    public boolean isAddressError() {
        return errorType == ShippingErrorType.ADDRESS_INVALID ||
                errorType == ShippingErrorType.ADDRESS_INCOMPLETE ||
                errorType == ShippingErrorType.INVALID_ZIP_CODE;
    }

    /**
     * Check if error is package-related
     */
    public boolean isPackageError() {
        return errorType == ShippingErrorType.WEIGHT_EXCEEDED ||
                errorType == ShippingErrorType.DIMENSION_EXCEEDED ||
                errorType == ShippingErrorType.RESTRICTED_ITEM ||
                errorType == ShippingErrorType.PACKAGE_LOST ||
                errorType == ShippingErrorType.PACKAGE_DAMAGED;
    }

    /**
     * Check if error is retryable
     */
    public boolean isRetryable() {
        return errorType == ShippingErrorType.CARRIER_UNAVAILABLE ||
                errorType == ShippingErrorType.SERVICE_UNAVAILABLE ||
                errorType == ShippingErrorType.CALCULATION_ERROR;
    }

    /**
     * Get user-friendly error message
     */
    public String getUserFriendlyMessage() {
        switch (errorType) {
            case ADDRESS_INVALID:
                return "The shipping address provided is invalid. Please verify and update your address.";
            case ADDRESS_INCOMPLETE:
                return "The shipping address is incomplete. Please provide all required information.";
            case CALCULATION_ERROR:
                return "We couldn't calculate shipping costs. Please try again.";
            case CARRIER_UNAVAILABLE:
                return "The selected shipping carrier is temporarily unavailable.";
            case SERVICE_UNAVAILABLE:
                return "Shipping service is temporarily unavailable. Please try again later.";
            case TRACKING_NOT_FOUND:
                return "We couldn't find tracking information for this shipment.";
            case SHIPMENT_NOT_FOUND:
                return "Shipment information not found.";
            case DELIVERY_FAILED:
                return "Delivery attempt failed. We'll try again or contact you for instructions.";
            case PACKAGE_LOST:
                return "Unfortunately, the package appears to be lost. Please contact customer service.";
            case PACKAGE_DAMAGED:
                return "The package was damaged during shipping. Please contact customer service.";
            case CUSTOMS_ISSUE:
                return "There's a customs issue with your shipment. Additional information may be required.";
            case WEIGHT_EXCEEDED:
                return "The package weight exceeds shipping limits.";
            case DIMENSION_EXCEEDED:
                return "The package size exceeds shipping limits.";
            case RESTRICTED_ITEM:
                return "This item cannot be shipped due to restrictions.";
            case INVALID_ZIP_CODE:
                return "The ZIP code provided is invalid or not in our delivery area.";
            case CANNOT_CANCEL:
                return "This shipment can no longer be cancelled.";
            case ALREADY_DELIVERED:
                return "This package has already been delivered.";
            default:
                return "A shipping error occurred. Please contact customer service.";
        }
    }

    @Override
    public String toString() {
        return "ShippingException{" +
                "message='" + getMessage() + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", carrier='" + carrier + '\'' +
                ", shipmentId='" + shipmentId + '\'' +
                ", errorType=" + errorType +
                ", retryable=" + isRetryable() +
                '}';
    }
}