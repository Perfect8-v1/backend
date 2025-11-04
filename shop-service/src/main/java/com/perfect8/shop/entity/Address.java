package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Address Entity - Version 1.0
 * Magnum Opus Compliant: No @Column(name=...) overrides needed
 * Hibernate automatically maps camelCase → snake_case
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
    private Long addressId;  // → DB: address_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)  // KEEP: explicit foreign key name
    private Customer customer;

    @Column(nullable = false, length = 50)
    private String addressType;  // → DB: address_type (BILLING, SHIPPING)

    @Column(nullable = false, length = 255)
    private String street;  // → DB: street

    @Column(length = 100)
    private String apartment;  // → DB: apartment

    @Column(nullable = false, length = 100)
    private String city;  // → DB: city

    @Column(length = 100)
    private String state;  // → DB: state

    @Column(nullable = false, length = 20)
    private String postalCode;  // → DB: postal_code

    @Column(nullable = false, length = 100)
    private String country;  // → DB: country

    @Column(nullable = false)
    private Boolean isDefault = false;  // → DB: is_default
}