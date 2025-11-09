package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Address entity for customer billing and shipping addresses
 * 
 * Design decisions:
 * - boolean defaultAddress (not Boolean isDefault) - primitiv för Magnum Opus
 * - Hibernate maps: defaultAddress → default_address (undviker MySQL reserverat ord "default")
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

    @Column(nullable = false)
    private boolean defaultAddress = false;  // Primitiv boolean - Lombok genererar isDefaultAddress()

    @Column(nullable = false, length = 50)
    private String addressType;  // BILLING, SHIPPING
}
