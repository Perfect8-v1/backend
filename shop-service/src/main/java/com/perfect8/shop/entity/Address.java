package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Address entity for customer billing and shipping addresses
 *
 * Magnum Opus Design:
 * - boolean defaultAddress (läsbart: "default address")
 * - Lombok generates: isDefaultAddress() getter, setDefaultAddress() setter
 * - Builder uses: .defaultAddress(boolean) 
 * - addressType: BILLING, SHIPPING, BOTH
 * - state: Required for USA customers (90% of Perfect8 customers)
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

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

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
     * Magnum Opus: Readable name "defaultAddress" answers "what is default?"
     * 
     * Lombok generates:
     * - Getter: isDefaultAddress()
     * - Setter: setDefaultAddress(boolean)
     * - Builder: .defaultAddress(boolean)
     */
    @Column(name = "default_address", nullable = false)
    @Builder.Default
    private boolean defaultAddress = false;

    @Column(name = "address_type", nullable = false, length = 50)
    private String addressType;  // BILLING, SHIPPING, BOTH
}
