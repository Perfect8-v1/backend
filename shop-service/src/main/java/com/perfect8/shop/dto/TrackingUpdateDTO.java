package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Tracking Updates
 * Version 1.0 - Lightweight tracking status updates
 *
 * Used for quick status updates from carriers or warehouse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tracking number for the shipment
     */
    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    private String trackingNumber;

    /**
     * New shipment status - Required
     */
    @NotBlank(message = "Status is required for tracking update")
    @Pattern(regexp = "^(PENDING|PROCESSING|SHIPPED|IN_TRANSIT|OUT_FOR_DELIVERY|DELIVERED|FAILED|RETURNED|CANCELLED|EXCEPTION)$",
            message = "Invalid status. Must be: PENDING, PROCESSING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED, CANCELLED, or EXCEPTION")
    private String status;

    /**
     * Current location/facility
     */
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String currentLocation;

    /**
     * Location type (for better tracking display)
     */
    @Pattern(regexp = "^(WAREHOUSE|DISTRIBUTION_CENTER|LOCAL_FACILITY|IN_VEHICLE|CUSTOMER_LOCATION|RETURN_CENTER)?$",
            message = "Invalid location type")
    private String locationType;

    /**
     * City where package currently is
     */
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-'.]*$", message = "Invalid city name")
    private String city;

    /**
     * State/Province code
     */
    @Pattern(regexp = "^[A-Z]{2}$|^$", message = "State code must be 2 uppercase letters")
    private String stateCode;

    /**
     * Country code
     */
    @Pattern(regexp = "^[A-Z]{2}$|^$", message = "Country code must be 2 uppercase letters")
    private String countryCode;

    /**
     * Notes about this update
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    /**
     * Timestamp of this tracking event
     */
    private LocalDateTime eventTimestamp;

    /**
     * Carrier-specific event code
     */
    @Size(max = 50, message = "Event code cannot exceed 50 characters")
    private String carrierEventCode;

    /**
     * Carrier-specific event description
     */
    @Size(max = 255, message = "Event description cannot exceed 255 characters")
    private String carrierEventDescription;

    /**
     * Exception/problem type (when status is EXCEPTION or FAILED)
     */
    @Pattern(regexp = "^(ADDRESS_ISSUE|WEATHER_DELAY|CARRIER_DELAY|CUSTOMS_HOLD|DAMAGED|LOST|REFUSED|UNDELIVERABLE|ACCESS_ISSUE|NO_RECIPIENT)?$",
            message = "Invalid exception type")
    private String exceptionType;

    /**
     * Exception details
     */
    @Size(max = 1000, message = "Exception details cannot exceed 1000 characters")
    private String exceptionDetails;

    /**
     * Required action (if any)
     */
    @Pattern(regexp = "^(CONTACT_CUSTOMER|UPDATE_ADDRESS|SCHEDULE_REDELIVERY|AWAIT_PICKUP|PROCESS_RETURN|INVESTIGATE)?$",
            message = "Invalid action type")
    private String requiredAction;

    /**
     * Delivery attempt number (for failed deliveries)
     */
    @Min(value = 1, message = "Attempt number must be at least 1")
    @Max(value = 10, message = "Attempt number seems unrealistic")
    private Integer attemptNumber;

    /**
     * Next action date (e.g., next delivery attempt)
     */
    private LocalDateTime nextActionDate;

    /**
     * Estimated delivery update (if changed)
     */
    private LocalDateTime estimatedDeliveryUpdate;

    /**
     * Is this a customer-visible update?
     */
    @Builder.Default
    private boolean customerVisible = true;

    /**
     * Priority level of this update
     */
    @Pattern(regexp = "^(LOW|NORMAL|HIGH|URGENT)?$",
            message = "Invalid priority level")
    @Builder.Default
    private String priority = "NORMAL";

    /**
     * Source of the update
     */
    @Pattern(regexp = "^(CARRIER_API|WEBHOOK|MANUAL|WAREHOUSE_SCAN|DRIVER_APP)?$",
            message = "Invalid update source")
    private String updateSource;

    /**
     * Person/system who created this update
     */
    @Size(max = 100, message = "Updated by cannot exceed 100 characters")
    private String updatedBy;

    // Alias methods for backward compatibility with ShippingService

    /**
     * Alias for getCurrentLocation() - used by ShippingService
     */
    public String getLocation() {
        return currentLocation;
    }

    /**
     * Alias setter for location
     */
    public void setLocation(String location) {
        this.currentLocation = location;
    }

    // Utility methods

    /**
     * Check if this is a delivery completion
     */
    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    /**
     * Check if this indicates a problem
     */
    public boolean isProblem() {
        return "FAILED".equals(status) ||
                "EXCEPTION".equals(status) ||
                "RETURNED".equals(status) ||
                exceptionType != null;
    }

    /**
     * Check if this requires customer notification
     */
    public boolean requiresCustomerNotification() {
        if (!customerVisible) {
            return false;
        }

        // Always notify for these statuses
        return "SHIPPED".equals(status) ||
                "OUT_FOR_DELIVERY".equals(status) ||
                "DELIVERED".equals(status) ||
                isProblem();
    }

    /**
     * Check if this requires immediate action
     */
    public boolean requiresImmediateAction() {
        return "URGENT".equals(priority) ||
                requiredAction != null ||
                "ADDRESS_ISSUE".equals(exceptionType) ||
                "LOST".equals(exceptionType);
    }

    /**
     * Get human-readable status message
     */
    public String getStatusMessage() {
        switch (status) {
            case "PENDING":
                return "Shipment is being prepared";
            case "PROCESSING":
                return "Package is being processed";
            case "SHIPPED":
                return "Package has been shipped";
            case "IN_TRANSIT":
                return currentLocation != null ?
                        "Package in transit - " + currentLocation :
                        "Package is in transit";
            case "OUT_FOR_DELIVERY":
                return "Out for delivery";
            case "DELIVERED":
                return "Package delivered";
            case "FAILED":
                return exceptionDetails != null ?
                        "Delivery failed: " + exceptionDetails :
                        "Delivery attempt failed";
            case "RETURNED":
                return "Package returned to sender";
            case "CANCELLED":
                return "Shipment cancelled";
            case "EXCEPTION":
                return exceptionDetails != null ?
                        "Delivery exception: " + exceptionDetails :
                        "Delivery exception occurred";
            default:
                return status;
        }
    }

    /**
     * Get location display text
     */
    public String getLocationDisplay() {
        if (currentLocation == null) {
            return null;
        }

        StringBuilder location = new StringBuilder(currentLocation);

        if (city != null) {
            location.append(", ").append(city);
        }
        if (stateCode != null) {
            location.append(", ").append(stateCode);
        }
        if (countryCode != null && !"US".equals(countryCode)) {
            location.append(", ").append(countryCode);
        }

        return location.toString();
    }

    /**
     * Get exception display text
     */
    public String getExceptionDisplay() {
        if (exceptionType == null) {
            return exceptionDetails;
        }

        switch (exceptionType) {
            case "ADDRESS_ISSUE":
                return "Address problem - verification needed";
            case "WEATHER_DELAY":
                return "Delayed due to weather conditions";
            case "CARRIER_DELAY":
                return "Carrier delay";
            case "CUSTOMS_HOLD":
                return "Held at customs";
            case "DAMAGED":
                return "Package damaged";
            case "LOST":
                return "Package lost - investigation in progress";
            case "REFUSED":
                return "Delivery refused by recipient";
            case "UNDELIVERABLE":
                return "Package undeliverable";
            case "ACCESS_ISSUE":
                return "Unable to access delivery location";
            case "NO_RECIPIENT":
                return "No one available to receive package";
            default:
                return exceptionDetails != null ? exceptionDetails : exceptionType;
        }
    }

    /**
     * Validate that exception fields are provided when status indicates problem
     */
    @AssertTrue(message = "Exception details required when status indicates a problem")
    private boolean isExceptionValid() {
        if (!"EXCEPTION".equals(status) && !"FAILED".equals(status)) {
            return true;
        }
        return exceptionType != null || exceptionDetails != null;
    }

    /**
     * Validate that delivered status doesn't have future timestamp
     */
    @AssertTrue(message = "Delivered timestamp cannot be in the future")
    private boolean isDeliveredTimestampValid() {
        if (!"DELIVERED".equals(status) || eventTimestamp == null) {
            return true;
        }
        return !eventTimestamp.isAfter(LocalDateTime.now());
    }
}