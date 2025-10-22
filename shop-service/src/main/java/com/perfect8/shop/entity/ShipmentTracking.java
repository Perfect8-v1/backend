package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Shipment Tracking Entity - Version 1.0
 * Magnum Opus Compliant: Lombok annotations, no manual getters/setters
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * Tracks shipment status updates and location history
 */
@Entity
@Table(name = "shipment_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"shipment"})
@ToString(exclude = {"shipment"})
public class ShipmentTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentTrackingId;  // → DB: shipment_tracking_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, length = 50)
    private String status;  // → DB: status (PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION)

    @Column(length = 255)
    private String location;  // → DB: location

    @Column(length = 500)
    private String description;  // → DB: description

    @Column(nullable = false)
    private LocalDateTime timestamp;  // → DB: timestamp

    @Column(length = 20)
    private String eventCode;  // → DB: event_code (Carrier-specific event codes)

    @Column(length = 1000)
    private String eventDetails;  // → DB: event_details

    @Column(length = 100)
    private String deliveryConfirmation;  // → DB: delivery_confirmation (Signature, left at door, etc.)

    @Column(length = 50)
    private String exceptionType;  // → DB: exception_type (WEATHER_DELAY, ADDRESS_ISSUE, etc.)

    private LocalDateTime nextScheduledDelivery;  // → DB: next_scheduled_delivery

    // ========== JPA LIFECYCLE CALLBACKS ==========

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates ALL getters/setters:
    // getShipmentTrackingId() / setShipmentTrackingId()
    // getShipment() / setShipment()
    // getStatus() / setStatus()
    // etc.
    // NO manual getters/setters - Lombok handles everything

    // ========== BUSINESS METHODS ==========

    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    public boolean isException() {
        return "EXCEPTION".equals(status);
    }

    public boolean isInTransit() {
        return "IN_TRANSIT".equals(status) || "OUT_FOR_DELIVERY".equals(status);
    }

    public boolean isPickedUp() {
        return "PICKED_UP".equals(status);
    }

    public String getDisplayDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }

        // Generate description based on status if none provided
        switch (status) {
            case "PICKED_UP":
                return "Package picked up from sender";
            case "IN_TRANSIT":
                return "Package in transit" + (location != null ? " at " + location : "");
            case "OUT_FOR_DELIVERY":
                return "Out for delivery" + (location != null ? " in " + location : "");
            case "DELIVERED":
                return "Package delivered" + (deliveryConfirmation != null ? " (" + deliveryConfirmation + ")" : "");
            case "EXCEPTION":
                return "Delivery exception" + (exceptionType != null ? ": " + exceptionType : "");
            default:
                return status;
        }
    }

    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    public boolean hasException() {
        return exceptionType != null && !exceptionType.trim().isEmpty();
    }

    public boolean hasDeliveryConfirmation() {
        return deliveryConfirmation != null && !deliveryConfirmation.trim().isEmpty();
    }

    public boolean isRescheduled() {
        return nextScheduledDelivery != null;
    }
}