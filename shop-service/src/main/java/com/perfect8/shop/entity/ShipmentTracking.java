package com.perfect8.shop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_tracking")
public class ShipmentTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, length = 50)
    private String status; // PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION, etc.

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "event_code", length = 20)
    private String eventCode; // Carrier-specific event codes

    @Column(name = "event_details", length = 1000)
    private String eventDetails;

    @Column(name = "delivery_confirmation", length = 100)
    private String deliveryConfirmation; // Signature, left at door, etc.

    @Column(name = "exception_type", length = 50)
    private String exceptionType; // WEATHER_DELAY, ADDRESS_ISSUE, etc.

    @Column(name = "next_scheduled_delivery")
    private LocalDateTime nextScheduledDelivery;

    // Default constructor
    public ShipmentTracking() {}

    // Constructor
    public ShipmentTracking(Shipment shipment, String status, String location, String description) {
        this.shipment = shipment;
        this.status = status;
        this.location = location;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getDeliveryConfirmation() {
        return deliveryConfirmation;
    }

    public void setDeliveryConfirmation(String deliveryConfirmation) {
        this.deliveryConfirmation = deliveryConfirmation;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public LocalDateTime getNextScheduledDelivery() {
        return nextScheduledDelivery;
    }

    public void setNextScheduledDelivery(LocalDateTime nextScheduledDelivery) {
        this.nextScheduledDelivery = nextScheduledDelivery;
    }

    // Business methods
    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    public boolean isException() {
        return "EXCEPTION".equals(status);
    }

    public boolean isInTransit() {
        return "IN_TRANSIT".equals(status) || "OUT_FOR_DELIVERY".equals(status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipmentTracking)) return false;
        ShipmentTracking that = (ShipmentTracking) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShipmentTracking{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}