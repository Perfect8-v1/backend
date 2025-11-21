package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Address entity for customer billing and shipping addresses
 *
 * UPDATED (2025-11-20):
 * - Changed defaultAddress to isDefault (Magnum Opus naming)
 * - Lombok generates: isDefault() getter, isDefault() builder method
 * 
 * Design decisions:
 * - boolean isDefault (not Boolean) - primitive for Magnum Opus
 * - addressType: BILLING eller SHIPPING
 * - state: Required for USA customers (90% of Perfect8 customers)
 * - ManyToOne with Customer for address history
 */
@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "customer")
@ToString(exclude = "customer")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(length = 100)
    private String apartment;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;  // Required for USA (90% of Perfect8 customers)

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Is this the default address for the customer?
     * Lombok generates: isDefault() getter, isDefault(boolean) builder method
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "address_type", nullable = false, length = 50)
    private String addressType;  // BILLING, SHIPPING, BOTH
}
