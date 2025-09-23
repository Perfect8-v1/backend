package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shipment Entity - Version 1.0
 * Core shipment tracking and management
 */
@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "tracking_number", unique = true, nullable = false, length = 100)
    private String trackingNumber;

    @Column(nullable = false, length = 50)
    private String carrier; // UPS, FedEx, DHL, USPS, etc.

    @Column(name = "shipment_status", nullable = false, length = 20)
    private String shipmentStatus; // PENDING, SHIPPED, IN_TRANSIT, DELIVERED, CANCELLED

    // Alias for backward compatibility
    public String getStatus() {
        return shipmentStatus;
    }

    public void setStatus(String status) {
        this.shipmentStatus = status;
    }

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight; // in kg

    @Column(length = 500)
    private String dimensions; // e.g., "30x20x15 cm"

    // Recipient information
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shipping_street", length = 255)
    private String shippingStreet;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", length = 50)
    private String shippingState;

    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;

    @Column(name = "shipping_country", length = 50)
    private String shippingCountry;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT, etc.

    // Tracking information
    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Column(name = "last_location", length = 255)
    private String lastLocation;

    @Column(name = "tracking_notes", columnDefinition = "TEXT")
    private String trackingNotes;

    // Delivery information
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;

    @Column(name = "delivery_notes", length = 500)
    private String deliveryNotes;

    @Column(name = "signature_required", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean signatureRequired;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "label_url", length = 500)
    private String labelUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Alias for backward compatibility
    public LocalDateTime getUpdatedAt() {
        return lastUpdated;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.lastUpdated = updatedAt;
    }

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShipmentTracking> trackingHistory = new ArrayList<>();

    // Constructor with essential fields
    public Shipment(Order order, String trackingNumber, String carrier) {
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shipmentStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.trackingHistory = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (shipmentStatus == null) {
            shipmentStatus = "PENDING";
        }
        if (trackingHistory == null) {
            trackingHistory = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Business methods
    public boolean isDelivered() {
        return "DELIVERED".equals(shipmentStatus);
    }

    public boolean isInTransit() {
        return "IN_TRANSIT".equals(shipmentStatus) || "SHIPPED".equals(shipmentStatus);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(shipmentStatus);
    }

    public boolean isOverdue() {
        if (estimatedDeliveryDate == null || isDelivered()) {
            return false;
        }
        return LocalDate.now().isAfter(estimatedDeliveryDate);
    }

    public int getDaysInTransit() {
        if (shippedDate == null) {
            return 0;
        }

        LocalDateTime endDate = actualDeliveryDate != null
                ? actualDeliveryDate.atStartOfDay()
                : LocalDateTime.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(shippedDate, endDate);
    }

    public void addTrackingUpdate(String status, String location, String description) {
        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setShipment(this);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setDescription(description);
        tracking.setTimestamp(LocalDateTime.now());

        this.trackingHistory.add(tracking);

        // Update shipment status and location
        this.shipmentStatus = status;
        if (this.currentLocation != null) {
            this.lastLocation = this.currentLocation;
        }
        this.currentLocation = location;

        // Mark as delivered if status is DELIVERED
        if ("DELIVERED".equals(status) && actualDeliveryDate == null) {
            this.actualDeliveryDate = LocalDate.now();
            this.deliveredDate = LocalDateTime.now();
        }
    }

    public ShipmentTracking getLatestTracking() {
        if (trackingHistory == null || trackingHistory.isEmpty()) {
            return null;
        }
        return trackingHistory.stream()
                .max((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()))
                .orElse(null);
    }

    // Utility method to convert LocalDate to LocalDateTime if needed
    public LocalDateTime getEstimatedDeliveryDateTime() {
        return estimatedDeliveryDate != null ? estimatedDeliveryDate.atStartOfDay() : null;
    }

    public void setEstimatedDeliveryDateTime(LocalDateTime dateTime) {
        this.estimatedDeliveryDate = dateTime != null ? dateTime.toLocalDate() : null;
    }

    public LocalDateTime getActualDeliveryDateTime() {
        return actualDeliveryDate != null ? actualDeliveryDate.atStartOfDay() : null;
    }

    public void setActualDeliveryDateTime(LocalDateTime dateTime) {
        this.actualDeliveryDate = dateTime != null ? dateTime.toLocalDate() : null;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "shipmentId=" + shipmentId +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", carrier='" + carrier + '\'' +
                ", status='" + shipmentStatus + '\'' +
                ", currentLocation='" + currentLocation + '\'' +
                ", estimatedDelivery=" + estimatedDeliveryDate +
                '}';
    }
}