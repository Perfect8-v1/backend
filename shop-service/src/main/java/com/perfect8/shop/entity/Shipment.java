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
    @Column(name = "shipment_id")
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "tracking_number", unique = true, nullable = false, length = 100)
    private String trackingNumber;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String carrier = "PostNord";

    @Column(name = "shipment_status", nullable = false, length = 20)
    @Builder.Default
    private String shipmentStatus = "PENDING";

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    // Recipient information
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

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
    @Builder.Default
    private String shippingCountry = "SE";

    @Column(name = "shipping_method", length = 50)
    @Builder.Default
    private String shippingMethod = "STANDARD";

    // Weight and dimensions (moved from v2.0 to v1.0)
    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight; // in kg

    @Column(name = "dimensions", length = 50)
    private String dimensions; // e.g., "30x20x15"

    // Delivery options (moved from v2.0 to v1.0)
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;

    @Column(name = "signature_required", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean signatureRequired = false;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount;

    // Label management (moved from v2.0 to v1.0)
    @Column(name = "label_url", length = 500)
    private String labelUrl;

    // Current location tracking (moved from v2.0 to v1.0)
    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @Column(name = "last_updated")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ShipmentTracking> trackingHistory = new ArrayList<>();

    /* ========== VERSION 2.0 FEATURES - COMMENTED OUT ==========
     * These advanced features will be implemented in version 2.0
     */

    // Advanced tracking (v2.0)
    // @Column(name = "last_location", length = 255)
    // private String lastLocation;
    //
    // @Column(name = "tracking_notes", columnDefinition = "TEXT")
    // private String trackingNotes;

    // Advanced delivery (v2.0)
    // @Column(name = "delivery_notes", length = 500)
    // private String deliveryNotes;
    //
    // @Column(name = "number_of_packages")
    // private Integer numberOfPackages;
    //
    // @Column(name = "package_type", length = 50)
    // private String packageType;

    // Special handling flags (v2.0)
    // @Column(name = "fragile", columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean fragile;
    //
    // @Column(name = "hazardous", columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean hazardous;
    //
    // @Column(name = "perishable", columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean perishable;
    //
    // @Column(name = "priority_handling", columnDefinition = "BOOLEAN DEFAULT FALSE")
    // private boolean priorityHandling;

    // Advanced options (v2.0)
    // @Column(name = "confirmation_type", length = 50)
    // private String confirmationType;
    //
    // @Column(name = "delivery_attempts")
    // private Integer deliveryAttempts;
    //
    // @Column(name = "exception_description", columnDefinition = "TEXT")
    // private String exceptionDescription;
    //
    // @Column(name = "return_tracking_number", length = 100)
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
    // Lombok generates: getUpdatedDate() / setUpdatedDate()
    // REMOVED: getStatus() / setStatus() - alias methods
    // REMOVED: getUpdatedAt() / setUpdatedAt() - alias methods

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
