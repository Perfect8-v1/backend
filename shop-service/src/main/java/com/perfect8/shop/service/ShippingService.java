package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.repository.OrderRepository;
import com.perfect8.shop.repository.ShipmentRepository;
import com.perfect8.shop.exception.ShippingException;
import com.perfect8.shop.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    /**
     * Get shipping options for an address
     */
    public ShippingOptionResponse getShippingOptions(String address, ShippingCalculationRequest request) {
        try {
            log.info("Calculating shipping options for address: {}", address);

            List<ShippingOptionDTO> options = new ArrayList<>();

            // Standard shipping
            ShippingOptionDTO standard = ShippingOptionDTO.builder()
                    .shippingOptionId(1L)
                    .name("Standard Shipping")
                    .description("5-7 business days")
                    .price(calculateStandardShipping(request))
                    .estimatedDays(7)
                    .carrier("USPS")
                    .build();
            options.add(standard);

            // Express shipping
            ShippingOptionDTO express = ShippingOptionDTO.builder()
                    .shippingOptionId(2L)
                    .name("Express Shipping")
                    .description("2-3 business days")
                    .price(calculateExpressShipping(request))
                    .estimatedDays(3)
                    .carrier("FedEx")
                    .isExpress(true)
                    .build();
            options.add(express);

            // Overnight shipping
            ShippingOptionDTO overnight = ShippingOptionDTO.builder()
                    .shippingOptionId(3L)
                    .name("Overnight Shipping")
                    .description("Next business day")
                    .price(calculateOvernightShipping(request))
                    .estimatedDays(1)
                    .carrier("UPS")
                    .isExpress(true)
                    .build();
            options.add(overnight);

            return ShippingOptionResponse.builder()
                    .address(address)
                    .options(options)
                    .estimatedDeliveryDate(LocalDate.now().plusDays(7))
                    .build();

        } catch (Exception e) {
            log.error("Error calculating shipping options: {}", e.getMessage(), e);
            throw new ShippingException("Failed to calculate shipping options: " + e.getMessage());
        }
    }

    /**
     * Calculate tax for an order
     */
    public TaxCalculationResponse calculateTax(TaxCalculationRequest request) {
        try {
            log.info("Calculating tax for subtotal: {}", request.getSubtotal());

            BigDecimal subtotal = request.getSubtotal();
            BigDecimal taxRate = getTaxRateForAddress(request.getShippingAddress());
            BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(taxAmount);

            return TaxCalculationResponse.builder()
                    .subtotal(subtotal)
                    .taxRate(taxRate)
                    .totalTaxAmount(taxAmount)
                    .totalAmount(total)
                    .taxJurisdiction(getTaxJurisdiction(request.getShippingAddress()))
                    .currency("USD")
                    .build();

        } catch (Exception e) {
            log.error("Error calculating tax: {}", e.getMessage(), e);
            throw new ShippingException("Failed to calculate tax: " + e.getMessage());
        }
    }

    /**
     * Estimate tax by zip code
     */
    public TaxEstimationResponse estimateTax(String zipCode, BigDecimal amount) {
        try {
            BigDecimal taxRate = getTaxRateForZipCode(zipCode);
            BigDecimal estimatedTax = amount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

            return TaxEstimationResponse.builder()
                    .postalCode(zipCode)
                    .subtotal(amount)
                    .totalTaxAmount(estimatedTax)
                    .totalAmount(amount.add(estimatedTax))
                    .currency("USD")
                    .isEstimate(true)
                    .disclaimerText("This is an estimate. Final tax will be calculated at checkout.")
                    .build();

        } catch (Exception e) {
            log.error("Error estimating tax: {}", e.getMessage(), e);
            throw new ShippingException("Failed to estimate tax: " + e.getMessage());
        }
    }

    /**
     * Update tracking information for a shipment
     */
    public void updateTrackingInfo(Long shipmentId, TrackingUpdateDTO trackingUpdate) {
        try {
            log.info("Updating tracking info for shipment: {}", shipmentId);

            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ShippingException("Shipment not found with ID: " + shipmentId));

            shipment.setTrackingNumber(trackingUpdate.getTrackingNumber());
            shipment.setStatus(trackingUpdate.getStatus());
            shipment.setLastLocation(trackingUpdate.getLocation());
            shipment.setUpdatedAt(LocalDateTime.now());

            shipmentRepository.save(shipment);

            log.info("Tracking updated - Status: {}, Location: {}",
                    trackingUpdate.getStatus(), trackingUpdate.getLocation());

        } catch (Exception e) {
            log.error("Error updating tracking info: {}", e.getMessage(), e);
            throw new ShippingException("Failed to update tracking info: " + e.getMessage());
        }
    }

    /**
     * Create a new shipment for an order
     */
    public Shipment createShipment(Order order, ShippingOptionDTO shippingOption) {
        try {
            log.info("Creating shipment for order: {}", order.getOrderId());

            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setCarrier(shippingOption.getCarrier());
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setShippingCost(shippingOption.getPrice());
            shipment.setEstimatedDeliveryDate(LocalDate.now().plusDays(shippingOption.getEstimatedDays()));
            shipment.setStatus("PREPARING");
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error creating shipment: {}", e.getMessage(), e);
            throw new ShippingException("Failed to create shipment: " + e.getMessage());
        }
    }

    /**
     * Get shipments for an order
     * FIXED: Using correct repository method name!
     */
    public List<Shipment> getShipmentsByOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

            // FIXED: Using findAllByOrderId instead of findByOrderOrderId
            return shipmentRepository.findAllByOrderId(orderId);

        } catch (Exception e) {
            log.error("Error fetching shipments: {}", e.getMessage(), e);
            throw new ShippingException("Failed to fetch shipments: " + e.getMessage());
        }
    }

    /**
     * Get shipment by tracking number
     */
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShippingException("Shipment not found with tracking number: " + trackingNumber));
    }

    /**
     * Update shipment status
     */
    public Shipment updateShipmentStatus(Long shipmentId, String status) {
        try {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ShippingException("Shipment not found with ID: " + shipmentId));

            shipment.setStatus(status);
            shipment.setUpdatedAt(LocalDateTime.now());

            // Update actual delivery date if delivered
            if ("DELIVERED".equals(status)) {
                shipment.setActualDeliveryDate(LocalDate.now());
            }

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error updating shipment status: {}", e.getMessage(), e);
            throw new ShippingException("Failed to update shipment status: " + e.getMessage());
        }
    }

    /**
     * Cancel shipment
     */
    public void cancelShipment(Long shipmentId) {
        try {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ShippingException("Shipment not found with ID: " + shipmentId));

            if ("DELIVERED".equals(shipment.getStatus()) || "IN_TRANSIT".equals(shipment.getStatus())) {
                throw new ShippingException("Cannot cancel shipment in status: " + shipment.getStatus());
            }

            shipment.setStatus("CANCELLED");
            shipment.setUpdatedAt(LocalDateTime.now());
            shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error cancelling shipment: {}", e.getMessage(), e);
            throw new ShippingException("Failed to cancel shipment: " + e.getMessage());
        }
    }

    // Private helper methods

    private BigDecimal calculateStandardShipping(ShippingCalculationRequest request) {
        // Base rate for standard shipping
        BigDecimal baseRate = new BigDecimal("5.99");

        // Add weight-based charges
        if (request.getTotalWeight() != null && request.getTotalWeight().compareTo(new BigDecimal("5.0")) > 0) {
            baseRate = baseRate.add(new BigDecimal("2.00"));
        }

        return baseRate;
    }

    private BigDecimal calculateExpressShipping(ShippingCalculationRequest request) {
        // Express shipping is 2.5x standard rate
        return calculateStandardShipping(request).multiply(new BigDecimal("2.5"));
    }

    private BigDecimal calculateOvernightShipping(ShippingCalculationRequest request) {
        // Overnight shipping is 4x standard rate
        return calculateStandardShipping(request).multiply(new BigDecimal("4.0"));
    }

    private BigDecimal getTaxRateForAddress(AddressDTO address) {
        if (address == null || address.getState() == null) {
            return new BigDecimal("0.05"); // Default 5%
        }

        // Simplified tax calculation based on state
        String state = address.getState().toUpperCase();

        switch (state) {
            case "CA": return new BigDecimal("0.0875"); // California
            case "NY": return new BigDecimal("0.08");    // New York
            case "TX": return new BigDecimal("0.0625");  // Texas
            case "FL": return new BigDecimal("0.06");    // Florida
            case "WA": return new BigDecimal("0.065");   // Washington
            case "OR": return BigDecimal.ZERO;           // Oregon (no sales tax)
            case "MT": return BigDecimal.ZERO;           // Montana (no sales tax)
            case "NH": return BigDecimal.ZERO;           // New Hampshire (no sales tax)
            case "DE": return BigDecimal.ZERO;           // Delaware (no sales tax)
            case "AK": return BigDecimal.ZERO;           // Alaska (no state sales tax)
            default: return new BigDecimal("0.05");      // Default rate
        }
    }

    private BigDecimal getTaxRateForZipCode(String zipCode) {
        if (zipCode == null || zipCode.length() < 1) {
            return new BigDecimal("0.05");
        }

        // Simplified tax rate lookup by zip code prefix
        char firstChar = zipCode.charAt(0);
        switch (firstChar) {
            case '9': return new BigDecimal("0.0875"); // CA/WA
            case '1': return new BigDecimal("0.08");    // NY/MA
            case '7': return new BigDecimal("0.0625");  // TX
            case '3': return new BigDecimal("0.06");    // FL
            default: return new BigDecimal("0.05");     // Default
        }
    }

    private String getTaxJurisdiction(AddressDTO address) {
        if (address == null) {
            return "Unknown";
        }
        return (address.getState() != null ? address.getState() : "Unknown") + " - " +
                (address.getCity() != null ? address.getCity() : "Unknown");
    }

    private String generateTrackingNumber() {
        // Generate a simple tracking number
        return "PS8-" + System.currentTimeMillis();
    }
}