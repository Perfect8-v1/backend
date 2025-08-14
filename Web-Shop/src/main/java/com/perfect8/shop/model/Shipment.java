package com.perfect8.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@EntityListeners(AuditingEntityListener.class)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnore
    private Order order;

    @NotNull(message = "Shipping method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingMethod shippingMethod;

    @NotNull(message = "Shipment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    @Column(length = 100)
    private String trackingNumber;

    @Size(max = 50, message = "Carrier cannot exceed 50 characters")
    @Column(length = 50)
    private String carrier;

    @Size(max = 100, message = "Carrier service cannot exceed 100 characters")
    @Column(length = 100)
    private String carrierService;

    @DecimalMin(value = "0.0", message = "Shipping cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Shipping cost format invalid")
    @Column(precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Weight format invalid")
    @Column(precision = 7, scale = 2)
    private BigDecimal weight;

    @Size(max = 20, message = "Weight unit cannot exceed 20 characters")
    @Column(length = 20)
    private String weightUnit = "kg";

    // Package dimensions
    @DecimalMin(value = "0.0", message = "Length cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Length format invalid")
    @Column(precision = 7, scale = 2)
    private BigDecimal length;

    @DecimalMin(value = "0.0", message = "Width cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Width format invalid")
    @Column(precision = 7, scale = 2)
    private BigDecimal width;

    @DecimalMin(value = "0.0", message = "Height cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Height format invalid")
    @Column(precision = 7, scale = 2)
    private BigDecimal height;

    @Size(max = 10, message = "Dimension unit cannot exceed 10 characters")
    @Column(length = 10)
    private String dimensionUnit = "cm";

    // Shipping addresses (copied from order for historical accuracy)
    @NotBlank(message = "Shipping address is required")
    @Size(max = 255, message = "Shipping address cannot exceed 255 characters")
    @Column(nullable = false)
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String shippingCity;

    @Size(max = 20, message = "Shipping postal code cannot exceed 20 characters")
    @Column(length = 20)
    private String shippingPostalCode;

    @Size(max = 100, message = "Shipping state/province cannot exceed 100 characters")
    @Column(length = 100)
    private String shippingState;

    @NotBlank(message = "Shipping country is required")
    @Size(max = 100, message = "Shipping country cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String shippingCountry;

    // Recipient information
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String recipientName;

    @Size(max = 20, message = "Recipient phone cannot exceed 20 characters")
    @Column(length = 20)
    private String recipientPhone;

    @Email(message = "Recipient email should be valid")
    @Size(max = 100, message = "Recipient email cannot exceed 100 characters")
    @Column(length = 100)
    private String recipientEmail;

    // Delivery information
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;

    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Size(max = 100, message = "Delivered to cannot exceed 100 characters")
    @Column(length = 100)
    private String deliveredTo;

    @Size(max = 255, message = "Delivery notes cannot exceed 255 characters")
    private String deliveryNotes;

    // Insurance and special handling
    @Column(nullable = false)
    private Boolean insured = false;

    @DecimalMin(value = "0.0", message = "Insurance amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Insurance amount format invalid")
    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean signatureRequired = false;

    @Column(nullable = false)
    private Boolean fragile = false;

    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    // Timestamps
    private LocalDateTime preparedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime inTransitAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime returnedAt;
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Shipment() {}

    public Shipment(Order order, ShippingMethod shippingMethod) {
        this.order = order;
        this.shippingMethod = shippingMethod;
        copyAddressFromOrder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            copyAddressFromOrder();
        }
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
        updateStatusTimestamp(status);
    }

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

    public String getCarrierService() {
        return carrierService;
    }

    public void setCarrierService(String carrierService) {
        this.carrierService = carrierService;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public String getDimensionUnit() {
        return dimensionUnit;
    }

    public void setDimensionUnit(String dimensionUnit) {
        this.dimensionUnit = dimensionUnit;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public String getDeliveredTo() {
        return deliveredTo;
    }

    public void setDeliveredTo(String deliveredTo) {
        this.deliveredTo = deliveredTo;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public Boolean getInsured() {
        return insured;
    }

    public void setInsured(Boolean insured) {
        this.insured = insured;
    }

    public BigDecimal getInsuranceAmount() {
        return insuranceAmount;
    }

    public void setInsuranceAmount(BigDecimal insuranceAmount) {
        this.insuranceAmount = insuranceAmount;
    }

    public Boolean getSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(Boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public LocalDateTime getPreparedAt() {
        return preparedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getInTransitAt() {
        return inTransitAt;
    }

    public LocalDateTime getOutForDeliveryAt() {
        return outForDeliveryAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business Methods
    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    public boolean isInTransit() {
        return status == ShipmentStatus.IN_TRANSIT || status == ShipmentStatus.OUT_FOR_DELIVERY;
    }

    public boolean canBeCancelled() {
        return status == ShipmentStatus.PENDING || status == ShipmentStatus.PREPARED;
    }

    public boolean hasTrackingNumber() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }

    public void prepare() {
        if (status == ShipmentStatus.PENDING) {
            setStatus(ShipmentStatus.PREPARED);
        }
    }

    public void ship() {
        if (status == ShipmentStatus.PREPARED) {
            setStatus(ShipmentStatus.SHIPPED);
        }
    }

    public void markInTransit() {
        if (status == ShipmentStatus.SHIPPED) {
            setStatus(ShipmentStatus.IN_TRANSIT);
        }
    }

    public void markOutForDelivery() {
        if (status == ShipmentStatus.IN_TRANSIT) {
            setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        }
    }

    public void deliver(String deliveredTo) {
        if (status == ShipmentStatus.OUT_FOR_DELIVERY) {
            setStatus(ShipmentStatus.DELIVERED);
            this.deliveredTo = deliveredTo;
            this.actualDeliveryDate = LocalDate.now();
        }
    }

    public void returnToSender() {
        if (status == ShipmentStatus.OUT_FOR_DELIVERY || status == ShipmentStatus.IN_TRANSIT) {
            setStatus(ShipmentStatus.RETURNED);
        }
    }

    public void cancel() {
        if (canBeCancelled()) {
            setStatus(ShipmentStatus.CANCELLED);
        }
    }

    public BigDecimal calculateVolumetricWeight() {
        if (length != null && width != null && height != null) {
            // Volumetric weight = (L × W × H) / 5000 for cm³ to kg
            BigDecimal volume = length.multiply(width).multiply(height);
            return volume.divide(BigDecimal.valueOf(5000), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getBillableWeight() {
        BigDecimal volumetricWeight = calculateVolumetricWeight();
        if (weight != null && volumetricWeight.compareTo(weight) > 0) {
            return volumetricWeight;
        }
        return weight != null ? weight : BigDecimal.ZERO;
    }

    public boolean isDelayed() {
        if (estimatedDeliveryDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return today.isAfter(estimatedDeliveryDate) && !isDelivered();
    }

    public long getDaysUntilDelivery() {
        if (estimatedDeliveryDate == null || isDelivered()) {
            return 0;
        }

        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), estimatedDeliveryDate);
    }

    private void copyAddressFromOrder() {
        if (order != null) {
            this.shippingAddress = order.getShippingAddress();
            this.shippingCity = order.getShippingCity();
            this.shippingPostalCode = order.getShippingPostalCode();
            this.shippingCountry = order.getShippingCountry();
            this.recipientName = order.getCustomer().getFullName();
            this.recipientEmail = order.getCustomer().getEmail();
            this.recipientPhone = order.getCustomer().getPhoneNumber();
        }
    }

    private void updateStatusTimestamp(ShipmentStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case PREPARED -> this.preparedAt = now;
            case SHIPPED -> this.shippedAt = now;
            case IN_TRANSIT -> this.inTransitAt = now;
            case OUT_FOR_DELIVERY -> this.outForDeliveryAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case RETURNED -> this.returnedAt = now;
            case CANCELLED -> this.cancelledAt = now;
        }
    }

    public String getDisplayStatus() {
        return status.getDisplayName();
    }

    public String getFormattedWeight() {
        return weight != null ? String.format("%.2f %s", weight, weightUnit) : "N/A";
    }

    public String getFormattedDimensions() {
        if (length != null && width != null && height != null) {
            return String.format("%.1f × %.1f × %.1f %s", length, width, height, dimensionUnit);
        }
        return "N/A";
    }

    public String getTrackingUrl() {
        if (!hasTrackingNumber() || carrier == null) {
            return null;
        }

        return switch (carrier.toLowerCase()) {
            case "dhl" -> "https://www.dhl.com/track?AWB=" + trackingNumber;
            case "fedex" -> "https://www.fedex.com/track?tracknumber=" + trackingNumber;
            case "ups" -> "https://www.ups.com/track?tracknum=" + trackingNumber;
            case "usps" -> "https://tools.usps.com/go/TrackConfirmAction?qtc_tLabels1=" + trackingNumber;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "id=" + id +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", status=" + status +
                ", carrier='" + carrier + '\'' +
                ", shippingMethod=" + shippingMethod +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shipment shipment = (Shipment) o;
        return id != null && id.equals(shipment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}