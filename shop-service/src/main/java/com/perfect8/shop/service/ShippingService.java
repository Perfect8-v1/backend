package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.entity.Address;
import com.perfect8.shop.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500.00");

    // Version 1.0 - Enkel implementation
    public List<ShippingOptionDTO> calculateShippingOptions(ShippingCalculationRequest request) {
        List<ShippingOptionDTO> options = new ArrayList<>();

        // Standard leverans
        BigDecimal standardPrice = request.getOrderAmount().compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ?
                BigDecimal.ZERO : new BigDecimal("49.00");

        options.add(ShippingOptionDTO.builder()
                .shippingOptionId(1L)
                .shippingMethod("STANDARD")
                .carrier("PostNord")
                .description("Standard leverans 3-5 arbetsdagar")
                .basePrice(standardPrice)
                .finalPrice(standardPrice)
                .estimatedDaysMin(3)
                .estimatedDaysMax(5)
                .available(true)
                .build());

        // Express leverans (Version 1.0 - håll det enkelt)
        options.add(ShippingOptionDTO.builder()
                .shippingOptionId(2L)
                .shippingMethod("EXPRESS")
                .carrier("DHL")
                .description("Express leverans 1-2 arbetsdagar")
                .basePrice(new BigDecimal("149.00"))
                .finalPrice(new BigDecimal("149.00"))
                .estimatedDaysMin(1)
                .estimatedDaysMax(2)
                .available(true)
                .build());

        return options;
    }

    public ShippingOptionResponse calculateShipping(ShippingCalculationRequest request) {
        List<ShippingOptionDTO> options = calculateShippingOptions(request);

        return ShippingOptionResponse.builder()
                .availableOptions(options)
                .defaultOption("STANDARD")
                .freeShippingThreshold(FREE_SHIPPING_THRESHOLD)
                .currentOrderValue(request.getOrderAmount())
                .freeShippingEligible(request.getOrderAmount().compareTo(FREE_SHIPPING_THRESHOLD) >= 0)
                .build();
    }

    /**
     * Get available shipping options
     * ADDED: Method called by CartController
     */
    public ShippingOptionResponse getShippingOptions(
            String customerId, 
            ShippingCalculationRequest request) {
        log.info("Getting shipping options for customer: {}", customerId);
        return calculateShipping(request);
    }

    /**
     * Calculate tax - Version 1.0 simplified
     * ADDED: Method called by CartController
     * TODO: Version 2.0 - Integrate with proper tax service
     */
    public TaxCalculationResponse calculateTax(TaxCalculationRequest request) {
        log.info("Calculating tax for order amount: {}", request.getOrderAmount());
        
        // V1.0: Simple percentage-based tax
        BigDecimal taxRate = new BigDecimal("0.25"); // 25% moms för Sverige
        BigDecimal taxAmount = request.getOrderAmount().multiply(taxRate);
        
        return TaxCalculationResponse.builder()
            .taxAmount(taxAmount)
            .taxRate(taxRate)
            .taxableAmount(request.getOrderAmount())
            .country(request.getCountry() != null ? request.getCountry() : "SE")
            .build();
    }

    @Transactional
    public Shipment createShipment(Order order, String shippingMethod) {
        // Använd order.getCustomer().getAddresses() för att hitta leveransadress
        Address shippingAddress = order.getCustomer().getAddresses().stream()
                .filter(addr -> "SHIPPING".equals(addr.getAddressType()))
                .findFirst()
                .orElse(order.getCustomer().getAddresses().stream()
                        .filter(addr -> "BILLING".equals(addr.getAddressType()))
                        .findFirst()
                        .orElse(null));

        if (shippingAddress == null) {
            throw new IllegalStateException("No shipping address found for order");
        }

        // FIXED: Use correct field names from Shipment entity (Magnum Opus principle)
        Shipment shipment = Shipment.builder()
                .order(order)
                .trackingNumber(generateTrackingNumber())
                .carrier("EXPRESS".equals(shippingMethod) ? "DHL" : "PostNord")
                .shippingMethod(shippingMethod)
                .recipientName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
                // REMOVED: .recipientPhone() - field doesn't exist in Shipment entity v1.0
                // REMOVED: .recipientEmail() - field doesn't exist in Shipment entity v1.0
                // FIXED: Use shippingStreet, shippingCity, etc (not addressLine1)
                .shippingAddress(shippingAddress.getStreet() + ", " + 
                                shippingAddress.getCity() + ", " + 
                                shippingAddress.getPostalCode())
                .shippingStreet(shippingAddress.getStreet())
                .shippingCity(shippingAddress.getCity())
                .shippingState(shippingAddress.getState())
                .shippingPostalCode(shippingAddress.getPostalCode())
                .shippingCountry(shippingAddress.getCountry())
                // FIXED: Use shipmentStatus (not status)
                .shipmentStatus("PENDING")
                // FIXED: Use shippedDate, actualDeliveryDate (not deliveryDate)
                .shippedDate(null)
                .actualDeliveryDate(null)
                // FIXED: Use createdAt, lastUpdated (not createdDate, updatedDate)
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        return shipmentRepository.save(shipment);
    }

    @Transactional
    public void updateShipmentStatus(Long shipmentId, String status, String notes) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        // FIXED: Use setShipmentStatus (or setStatus alias method)
        shipment.setStatus(status);
        
        if ("SHIPPED".equals(status)) {
            shipment.setShippedDate(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            // FIXED: Use setDeliveredDate and setActualDeliveryDate
            shipment.setDeliveredDate(LocalDateTime.now());
            shipment.setActualDeliveryDate(LocalDate.now());
        }

        // FIXED: Use setLastUpdated (not setUpdatedDate)
        shipment.setLastUpdated(LocalDateTime.now());
        shipmentRepository.save(shipment);

        log.info("Updated shipment {} status to {} with notes: {}", shipmentId, status, notes);
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis();
    }
}
