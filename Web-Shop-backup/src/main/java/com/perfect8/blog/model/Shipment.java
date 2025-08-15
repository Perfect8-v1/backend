package com.perfect8.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order_1 order;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "ship_date")
    private LocalDateTime shipDate;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    // --- Generate Getters and Setters here --
}
