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
import java.util.Optional;

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
    public Shipment findById(Long shipmentId) {
        log.info("Finding shipment by ID: {}", shipmentId);

        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + shipmentId));
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
            return shipmentRepository.findByShipmentStatus(status);
        } catch (Exception e) {
            log.warn("Could not find shipments by status, returning empty list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find shipment by order ID
     */
    @Transactional(readOnly = true)
    public Optional<Shipment> findByOrderId(Long orderId) {
        log.info("Finding shipment by order ID: {}", orderId);

        try {
            return shipmentRepository.findByOrderId(orderId);
        } catch (Exception e) {
            log.warn("Could not find shipment by order ID: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Find all shipments by order ID
     */
    @Transactional(readOnly = true)
    public List<Shipment> findAllByOrderId(Long orderId) {
        log.info("Finding all shipments by order ID: {}", orderId);

        try {
            return shipmentRepository.findAllByOrderId(orderId);
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
            shipment.setShipmentStatus("PENDING");
            shipment.setShippedDate(LocalDateTime.now());
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setLastUpdated(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error creating shipment: {}", e.getMessage());
            throw new RuntimeException("Failed to create shipment: " + e.getMessage());
        }
    }

    /**
     * Update shipment status
     */
    public Shipment updateStatus(Long shipmentId, String status) {
        log.info("Updating shipment {} status to {}", shipmentId, status);

        try {
            Shipment shipment = findById(shipmentId);
            shipment.setShipmentStatus(status);
            shipment.setLastUpdated(LocalDateTime.now());

            // If delivered, set delivery date
            if ("DELIVERED".equals(status)) {
                shipment.setDeliveredDate(LocalDateTime.now());
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
    public Shipment updateTracking(Long shipmentId, String location, String status, String notes) {
        log.info("Updating tracking for shipment {}", shipmentId);

        try {
            Shipment shipment = findById(shipmentId);

            // Update location
            if (location != null) {
                shipment.setCurrentLocation(location);
            }

            // Update status if provided
            if (status != null) {
                shipment.setShipmentStatus(status);
            }

            // Add notes if provided
            if (notes != null) {
                String existingNotes = shipment.getTrackingNotes();
                shipment.setTrackingNotes(existingNotes != null
                        ? existingNotes + "\n" + LocalDateTime.now() + ": " + notes
                        : LocalDateTime.now() + ": " + notes);
            }

            shipment.setLastUpdated(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error updating shipment tracking: {}", e.getMessage());
            throw new RuntimeException("Failed to update tracking: " + e.getMessage());
        }
    }

    /**
     * Cancel shipment
     */
    public Shipment cancelShipment(Long shipmentId, String reason) {
        log.info("Cancelling shipment {}: {}", shipmentId, reason);

        try {
            Shipment shipment = findById(shipmentId);

            // Check if shipment can be cancelled
            if ("DELIVERED".equals(shipment.getShipmentStatus())) {
                throw new IllegalStateException("Cannot cancel delivered shipment");
            }

            shipment.setShipmentStatus("CANCELLED");
            String notes = shipment.getTrackingNotes();
            shipment.setTrackingNotes(notes != null
                    ? notes + "\nCancelled: " + reason
                    : "Cancelled: " + reason);
            shipment.setLastUpdated(LocalDateTime.now());

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
            List<Shipment> recent = shipmentRepository.findTop10ByOrderByCreatedAtDesc();
            if (limit < 10 && !recent.isEmpty()) {
                return recent.subList(0, Math.min(limit, recent.size()));
            }
            return recent;
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
            return shipmentRepository.countByShipmentStatus(status);
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
            return shipmentRepository.findByCustomerId(customerId, pageable);
        } catch (Exception e) {
            log.error("Error finding shipments for customer: {}", e.getMessage());
            return Page.empty();
        }
    }

    /**
     * Mark shipment as shipped
     */
    public Shipment markAsShipped(Long shipmentId) {
        log.info("Marking shipment {} as shipped", shipmentId);

        try {
            Shipment shipment = findById(shipmentId);

            if (!"PENDING".equals(shipment.getShipmentStatus())) {
                throw new IllegalStateException("Only pending shipments can be marked as shipped");
            }

            shipment.setShipmentStatus("SHIPPED");
            shipment.setShippedDate(LocalDateTime.now());
            shipment.setLastUpdated(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as shipped: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as shipped: " + e.getMessage());
        }
    }

    /**
     * Mark shipment as in transit
     */
    public Shipment markAsInTransit(Long shipmentId, String currentLocation) {
        log.info("Marking shipment {} as in transit at {}", shipmentId, currentLocation);

        try {
            Shipment shipment = findById(shipmentId);

            shipment.setShipmentStatus("IN_TRANSIT");
            if (currentLocation != null) {
                shipment.setCurrentLocation(currentLocation);
            }
            shipment.setLastUpdated(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as in transit: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as in transit: " + e.getMessage());
        }
    }

    /**
     * Mark shipment as delivered
     */
    public Shipment markAsDelivered(Long shipmentId, String deliveryNotes) {
        log.info("Marking shipment {} as delivered", shipmentId);

        try {
            Shipment shipment = findById(shipmentId);

            shipment.setShipmentStatus("DELIVERED");
            shipment.setDeliveredDate(LocalDateTime.now());

            if (deliveryNotes != null) {
                String notes = shipment.getTrackingNotes();
                shipment.setTrackingNotes(notes != null
                        ? notes + "\nDelivered: " + deliveryNotes
                        : "Delivered: " + deliveryNotes);
            }

            shipment.setLastUpdated(LocalDateTime.now());

            return shipmentRepository.save(shipment);

        } catch (Exception e) {
            log.error("Error marking shipment as delivered: {}", e.getMessage());
            throw new RuntimeException("Failed to mark as delivered: " + e.getMessage());
        }
    }
}