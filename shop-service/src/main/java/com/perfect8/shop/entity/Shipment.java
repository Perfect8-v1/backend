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
 * Magnum Opus Compliant: No alias methods, consistent naming
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * UPDATED: Moved essential v2.0 fields to v1.0 for facilitate frontend compatibility
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
    private Long shipmentId;  // → DB: shipment_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(unique = true, nullable = false, length = 100)
    private String trackingNumber;  // → DB: tracking_number

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String carrier = "PostNord";  // → DB: carrier

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String shipmentStatus = "PENDING";  // → DB: shipment_status

    private LocalDateTime shippedDate;  // → DB: shipped_date

    private LocalDate estimatedDeliveryDate;  // → DB: estimated_delivery_date

    private LocalDate actualDeliveryDate;  // → DB: actual_delivery_date

    private LocalDateTime deliveredDate;  // → DB: delivered_date

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;  // → DB: shipping_cost

    // Recipient information
    @Column(length = 100)
    private String recipientName;  // → DB: recipient_name

    @Column(length = 20)
    private String recipientPhone;  // → DB: recipient_phone

    @Column(length = 100)
    private String recipientEmail;  // → DB: recipient_email

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;  // → DB: shipping_address

    @Column(length = 255)
    private String shippingStreet;  // → DB: shipping_street

    @Column(length = 100)
    private String shippingCity;  // → DB: shipping_city

    @Column(length = 50)
    private String shippingState;  // → DB: shipping_state

    @Column(length = 20)
    private String shippingPostalCode;  // → DB: shipping_postal_code

    @Column(length = 50)
    @Builder.Default
    private String shippingCountry = "SE";  // → DB: shipping_country

    @Column(length = 50)
    @Builder.Default
    private String shippingMethod = "STANDARD";  // → DB: shipping_method

    // Weight and dimensions (moved from v2.0 to v1.0)
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;  // → DB: weight (in kg)

    @Column(length = 50)
    private String dimensions;  // → DB: dimensions (e.g., "30x20x15")

    // Delivery options (moved from v2.0 to v1.0)
    @Column(length = 500)
    private String deliveryInstructions;  // → DB: delivery_instructions

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean signatureRequired = false;  // → DB: signature_required

    @Column(precision = 10, scale = 2)
    private BigDecimal insuranceAmount;  // → DB: insurance_amount

    // Label management (moved from v2.0 to v1.0)
    @Column(length = 500)
    private String labelUrl;  // → DB: label_url

    // Current location tracking (moved from v2.0 to v1.0)
    @Column(length = 255)
    private String currentLocation;  // → DB: current_location

    @Column(columnDefinition = "TEXT")
    private String notes;  // → DB: notes

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShipmentTracking> trackingHistory = new ArrayList<>();

    /* ========== VERSION 2.0 FEATURES - COMMENTED OUT ==========
     * These advanced features will be implemented in version 2.0
     */

    // Advanced tracking (v2.0)
    // @Column(length = 255)
    // private String lastLocation;
    //
    // @Column(columnDefinition = "TEXT")
    // private String trackingNotes;

    // Advanced delivery (v2.0)
    // @Column(length = 500)
    // private String deliveryNotes;
    //
    // private Integer numberOfPackages;
    //
    // @Column(length = 50)
    // private String packageType;

    // Special handling flags (v2.0)
    // @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean fragile;
    //
    // @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean hazardous;
    //
    // @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean perishable;
    //
    // @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean priorityHandling;

    // Advanced options (v2.0)
    // @Column(length = 50)
    // private String confirmationType;
    //
    // private Integer deliveryAttempts;
    //
    // @Column(columnDefinition = "TEXT")
    // private String exceptionDescription;
    //
    // @Column(length = 100)
    // private String returnTrackingNumber;

    /* ========== END VERSION 2.0 FEATURES ========== */

    // Constructor with essential fields
    public Shipment(Order order, String trackingNumber, String carrier) {
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shipmentStatus = "PENDING";
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.trackingHistory = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (updatedDate == null) {
            updatedDate = LocalDateTime.now();
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
        updatedDate = LocalDateTime.now();
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates: getShipmentStatus() / setShipmentStatus()
    // Lombok generates: getCreatedDate() / setCreatedDate()
    // Lombok generates: getLastUpdated() / setLastUpdated()
    // No alias methods

    // ========== BUSINESS METHODS ==========

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

        // Update shipment status
        this.shipmentStatus = status;

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

    // Utility methods for LocalDate/LocalDateTime conversion
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
                ", estimatedDelivery=" + estimatedDeliveryDate +
                '}';
    }
}