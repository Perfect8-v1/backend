package com.perfect8.shop.service;

import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.repository.ShipmentRepository;
import com.perfect8.shop.repository.OrderRepository;
import com.perfect8.shop.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for Shipment management - Version 1.0
 * Core shipment functionality only - integrates with ShippingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    /**
     * Find shipment by ID
     */
    @Transactional(readOnly = true)
    public Shipment findById(Long id) {
        log.info("Finding shipment by ID: {}", id);

        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + id));
    }

    /**
     * Get all shipments with pagination
     */
    @Transactional(readOnly = true)
    public Page<Shipment> findAll(Pageable pageable) {
        log.info("Getting all shipments with pagination");

        return shipmentRepository.findAll(pageable);
    }

    /**
     * Find shipments by status
     */
    @Transactional(readOnly = true)
    public List<Shipment> findByStatus(String status) {
        log.info("Finding shipments by status: {}", status);

        try {
            return shipmentRepository.findByStatus(status);
        } catch (Exception e) {
            log.warn("Could not find shipments by status, returning empty list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find shipments by order ID
     */
    @Transactional(readOnly = true)
    public List<Shipment> findByOrderId(Long orderId) {
        log.info("Finding shipments by order ID: {}", orderId);

        try {
            return shipmentRepository.findByOrderId(orderId);
        } catch (Exception e) {
            log.warn("Could not find shipments by order ID, returning empty list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find shipment by tracking number
     */
    @Transactional(readOnly = true)
    public Shipment findByTrackingNumber(String trackingNumber) {
        log.info("Finding shipment by tracking number: {}", trackingNumber);

        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking number: " + trackingNumber));
    }

    /**
     * Create a new shipment
     */
    public Shipment createShipment(Long orderId, String carrier, String trackingNumber) {
        log.info("Creating shipment for order: {}", orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

            Shipment shipment = new Shipment(order, trackingNumber, carrier);
            shipment.setStatus("PENDING");
            shipment.setShipmentDate(LocalDateTime.now());
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error creating shipment: {}", e.getMessage());
            throw new RuntimeException("Failed to create shipment: " + e.getMessage());
        }
    }

    /**
     * Update shipment status
     */
    public Shipment updateStatus(Long id, String status) {
        log.info("Updating shipment {} status to {}", id, status);

        try {
            Shipment shipment = findById(id);
            shipment.setStatus(status);
            shipment.setUpdatedAt(LocalDateTime.now());

            // If delivered, set actual delivery date
            if ("DELIVERED".equals(status)) {
                shipment.setActualDeliveryDate(java.time.LocalDate.now());
            }

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error updating shipment status: {}", e.getMessage());
            throw new RuntimeException("Failed to update shipment status: " + e.getMessage());
        }
    }

    /**
     * Update shipment tracking information
     */
    public Shipment updateTracking(Long id, String location, String status, String notes) {
        log.info("Updating tracking for shipment {}", id);

        try {
            Shipment shipment = findById(id);

            // Update location
            if (location != null) {
                shipment.setLastLocation(shipment.getCurrentLocation());
                shipment.setCurrentLocation(location);
            }

            // Update status if provided
            if (status != null) {
                shipment.setStatus(status);
            }

            // Add notes if provided
            if (notes != null) {
                String existingNotes = shipment.getNotes();
                shipment.setNotes(existingNotes != null
                        ? existingNotes + "\n" + LocalDateTime.now() + ": " + notes
                        : LocalDateTime.now() + ": " + notes);
            }

            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error updating shipment tracking: {}", e.getMessage());
            throw new RuntimeException("Failed to update tracking: " + e.getMessage());
        }
    }

    /**
     * Cancel shipment
     */
    public Shipment cancelShipment(Long id, String reason) {
        log.info("Cancelling shipment {}: {}", id, reason);

        try {
            Shipment shipment = findById(id);

            // Check if shipment can be cancelled
            if ("DELIVERED".equals(shipment.getStatus())) {
                throw new IllegalStateException("Cannot cancel delivered shipment");
            }

            shipment.setStatus("CANCELLED");
            shipment.setNotes(shipment.getNotes() != null
                    ? shipment.getNotes() + "\nCancelled: " + reason
                    : "Cancelled: " + reason);
            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error cancelling shipment: {}", e.getMessage());
            throw new RuntimeException("Failed to cancel shipment: " + e.getMessage());
        }
    }

    /**
     * Get recent shipments
     */
    @Transactional(readOnly = true)
    public List<Shipment> getRecentShipments(int limit) {
        log.info("Getting {} recent shipments", limit);

        try {
            return shipmentRepository.findAll().stream()
                    .sorted((s1, s2) -> {
                        LocalDateTime date1 = s1.getCreatedAt() != null ? s1.getCreatedAt() : LocalDateTime.MIN;
                        LocalDateTime date2 = s2.getCreatedAt() != null ? s2.getCreatedAt() : LocalDateTime.MIN;
                        return date2.compareTo(date1); // Descending order
                    })
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent shipments: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Count shipments by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        log.info("Counting shipments with status: {}", status);

        try {
            return shipmentRepository.countByStatus(status);
        } catch (Exception e) {
            log.warn("Could not count shipments by status: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Check if tracking number exists
     */
    @Transactional(readOnly = true)
    public boolean existsByTrackingNumber(String trackingNumber) {
        log.info("Checking if tracking number exists: {}", trackingNumber);

        try {
            return shipmentRepository.existsByTrackingNumber(trackingNumber);
        } catch (Exception e) {
            log.warn("Error checking tracking number existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get shipments for customer
     */
    @Transactional(readOnly = true)
    public Page<Shipment> findByCustomerId(Long customerId, Pageable pageable) {
        log.info("Finding shipments for customer: {}", customerId);

        try {
            // This would need a custom query in repository to join through Order -> Customer
            // For now, returning empty page
            log.warn("findByCustomerId not fully implemented - needs repository method");
            return Page.empty();
        } catch (Exception e) {
            log.error("Error finding shipments for customer: {}", e.getMessage());
            return Page.empty();
        }
    }

    /**
     * Mark shipment as shipped
     */
    public Shipment markAsShipped(Long id) {
        log.info("Marking shipment {} as shipped", id);

        try {
            Shipment shipment = findById(id);

            if (!"PENDING".equals(shipment.getStatus())) {
                throw new IllegalStateException("Only pending shipments can be marked as shipped");
            }

            shipment.setStatus("SHIPPED");
            shipment.setShipmentDate(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as shipped: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as shipped: " + e.getMessage());
        }
    }

    /**
     * Mark shipment as in transit
     */
    public Shipment markAsInTransit(Long id, String currentLocation) {
        log.info("Marking shipment {} as in transit at {}", id, currentLocation);

        try {
            Shipment shipment = findById(id);

            shipment.setStatus("IN_TRANSIT");
            if (currentLocation != null) {
                shipment.setLastLocation(shipment.getCurrentLocation());
                shipment.setCurrentLocation(currentLocation);
            }
            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as in transit: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as in transit: " + e.getMessage());
        }
    }

    /**
     * Mark shipment as delivered
     */
    public Shipment markAsDelivered(Long id, String deliveryNotes) {
        log.info("Marking shipment {} as delivered", id);

        try {
            Shipment shipment = findById(id);

            shipment.setStatus("DELIVERED");
            shipment.setActualDeliveryDate(java.time.LocalDate.now());

            if (deliveryNotes != null) {
                shipment.setNotes(shipment.getNotes() != null
                        ? shipment.getNotes() + "\nDelivered: " + deliveryNotes
                        : "Delivered: " + deliveryNotes);
            }

            shipment.setUpdatedAt(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as delivered: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as delivered: " + e.getMessage());
        }
    }

    /* VERSION 2.0 - ADVANCED FEATURES
    // Commented out for v1.0 - will be reimplemented in v2.0

    public Page<Shipment> searchShipments(String trackingNumber, String recipientName,
                                         String carrier, String status,
                                         LocalDate fromDate, LocalDate toDate,
                                         Pageable pageable) {
        // Complex search implementation for v2.0
    }

    public List<Shipment> getPendingShipments(int limit) {
        // Implementation for v2.0
    }

    public List<Shipment> getOverdueShipments() {
        // Implementation for v2.0
    }

    public String generateShippingLabel(Long id) {
        // Integration with shipping providers for v2.0
    }

    public boolean verifyWebhookSignature(String carrier, String data, String signature) {
        // Webhook security for v2.0
    }

    public void processTrackingWebhook(String carrier, String trackingData) {
        // Webhook processing for v2.0
    }

    public Object getShipmentStatistics() {
        // Analytics for v2.0
    }
    */
}