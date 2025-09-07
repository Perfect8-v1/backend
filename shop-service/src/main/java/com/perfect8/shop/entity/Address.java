package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "address_type", length = 20)
    @Builder.Default
    private String addressType = "BOTH"; // SHIPPING, BILLING, BOTH

    @Column(name = "address_line1", nullable = false, length = 100)
    private String addressLine1;

    @Column(name = "address_line2", length = 100)
    private String addressLine2;

    @Column(name = "street", length = 100)
    private String street;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(name = "state_province", length = 50)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    @Builder.Default
    private String country = "US";

    @Column(name = "is_default_shipping")
    @Builder.Default
    private Boolean defaultShipping = false;

    @Column(name = "is_default_billing")
    @Builder.Default
    private Boolean defaultBilling = false;

    @Column(name = "recipient_first_name", length = 50)
    private String recipientFirstName;

    @Column(name = "recipient_last_name", length = 50)
    private String recipientLastName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 100)
    private String recipientEmail;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "is_business_address")
    @Builder.Default
    private Boolean isBusinessAddress = false;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (defaultShipping == null) {
            defaultShipping = false;
        }
        if (defaultBilling == null) {
            defaultBilling = false;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (isBusinessAddress == null) {
            isBusinessAddress = false;
        }
        if (country == null) {
            country = "US";
        }
        if (addressType == null) {
            addressType = "BOTH";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) {
            sb.append(addressLine1);
        }
        if (addressLine2 != null && !addressLine2.isEmpty()) {
            sb.append(", ").append(addressLine2);
        }
        if (city != null) {
            sb.append(", ").append(city);
        }
        if (state != null) {
            sb.append(", ").append(state);
        }
        if (postalCode != null) {
            sb.append(" ").append(postalCode);
        }
        if (country != null) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }

    public String getRecipientFullName() {
        if (recipientFirstName != null && recipientLastName != null) {
            return recipientFirstName + " " + recipientLastName;
        } else if (recipientFirstName != null) {
            return recipientFirstName;
        } else if (recipientLastName != null) {
            return recipientLastName;
        } else if (customer != null) {
            return customer.getFullName();
        }
        return "";
    }

    public boolean isDefaultShipping() {
        return Boolean.TRUE.equals(defaultShipping);
    }

    public void setDefaultShipping(boolean defaultShipping) {
        this.defaultShipping = defaultShipping;
    }

    public boolean isDefaultBilling() {
        return Boolean.TRUE.equals(defaultBilling);
    }

    public void setDefaultBilling(boolean defaultBilling) {
        this.defaultBilling = defaultBilling;
    }

    public boolean canBeUsedForShipping() {
        return isActive() && ("SHIPPING".equals(addressType) || "BOTH".equals(addressType));
    }

    public boolean canBeUsedForBilling() {
        return isActive() && ("BILLING".equals(addressType) || "BOTH".equals(addressType));
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isVerified);
    }

    public boolean isBusinessAddress() {
        return Boolean.TRUE.equals(isBusinessAddress);
    }

    public void markAsVerified() {
        this.isVerified = true;
        this.verificationDate = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.defaultShipping = false;
        this.defaultBilling = false;
    }

    // Backwards compatibility - some code may use "street" instead of addressLine1
    public String getStreet() {
        return street != null ? street : addressLine1;
    }

    public void setStreet(String street) {
        this.street = street;
        if (this.addressLine1 == null) {
            this.addressLine1 = street;
        }
    }

    @Override
    public String toString() {
        return "Address{" +
                "addressId=" + addressId +
                ", addressLine1='" + addressLine1 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", isDefaultShipping=" + defaultShipping +
                ", isDefaultBilling=" + defaultBilling +
                '}';
    }
}