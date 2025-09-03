package com.perfect8.shop.controller;

import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.ShipmentDTO;
import com.perfect8.shop.dto.ShipmentUpdateDTO;
import com.perfect8.shop.dto.TrackingUpdateDTO;
import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.service.ShipmentService;
import com.perfect8.shop.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Shipment management
 * Version 1.0 - Core tracking and delivery functionality
 *
 * Critical: Accurate tracking prevents customer anxiety!
 */
@Slf4j
@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShippingService shippingService;  // For creating shipments

    /**
     * Track shipment by tracking number - PUBLIC endpoint
     * Most important endpoint for customers!
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<Shipment>> trackShipment(
            @PathVariable String trackingNumber) {
        try {
            log.info("Tracking shipment with number: {}", trackingNumber);

            Shipment shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipment tracking information retrieved successfully",
                    shipment
            ));

        } catch (Exception e) {
            log.error("Failed to track shipment {}: {}", trackingNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Shipment not found: " + e.getMessage()));
        }
    }

    /**
     * Get shipments by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shipment>>> getShipmentsByOrderId(
            @PathVariable Long orderId,
            Principal principal) {
        try {
            List<Shipment> shipments = shippingService.getShipmentsByOrder(orderId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipments for order retrieved successfully",
                    shipments
            ));

        } catch (Exception e) {
            log.error("Failed to retrieve shipments for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to retrieve shipments: " + e.getMessage()));
        }
    }

    /**
     * Get shipment by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> getShipmentById(
            @PathVariable Long id,
            Principal principal) {
        try {
            Shipment shipment = shipmentService.findById(id);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipment retrieved successfully",
                    shipment
            ));

        } catch (Exception e) {
            log.error("Failed to retrieve shipment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Shipment not found: " + e.getMessage()));
        }
    }

    // ===== ADMIN ENDPOINTS =====

    /**
     * Update shipment status (Admin only)
     * Critical for accurate tracking!
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            log.info("Updating status for shipment {}: {}", id, status);

            Shipment updatedShipment = shippingService.updateShipmentStatus(id, status);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipment status updated successfully",
                    updatedShipment
            ));

        } catch (Exception e) {
            log.error("Failed to update shipment status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update status: " + e.getMessage()));
        }
    }

    /**
     * Cancel shipment (Admin only)
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cancelShipment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Cancelling shipment {}: {}", id, reason);

            shippingService.cancelShipment(id);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipment cancelled successfully",
                    "Shipment " + id + " has been cancelled"
            ));

        } catch (Exception e) {
            log.error("Failed to cancel shipment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel shipment: " + e.getMessage()));
        }
    }

    /**
     * Get all shipments (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Shipment>>> getAllShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Shipment> shipments = shipmentService.findAll(pageable);

            return ResponseEntity.ok(ApiResponse.success(
                    "Shipments retrieved successfully",
                    shipments
            ));

        } catch (Exception e) {
            log.error("Failed to retrieve shipments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve shipments: " + e.getMessage()));
        }
    }

    /**
     * Get shipments by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shipment>>> getShipmentsByStatus(
            @PathVariable String status) {
        try {
            List<Shipment> shipments = shipmentService.findByStatus(status.toUpperCase());

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Shipments with status %s retrieved", status),
                    shipments
            ));

        } catch (Exception e) {
            log.error("Failed to retrieve shipments by status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve shipments: " + e.getMessage()));
        }
    }

    /**
     * Update tracking information (Admin only)
     */
    @PutMapping("/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateTrackingInfo(
            @PathVariable Long id,
            @Valid @RequestBody TrackingUpdateDTO trackingUpdate) {
        try {
            log.info("Updating tracking for shipment {}", id);

            shippingService.updateTrackingInfo(id, trackingUpdate);

            return ResponseEntity.ok(ApiResponse.success(
                    "Tracking information updated successfully",
                    "Tracking updated for shipment " + id
            ));

        } catch (Exception e) {
            log.error("Failed to update tracking info: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update tracking: " + e.getMessage()));
        }
    }

    /* VERSION 2.0 - ADVANCED FEATURES
    // Commented out for v1.0 - will be reimplemented in v2.0

    // Customer's shipments with pagination
    @GetMapping("/my-shipments")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<Shipment>>> getMyShipments() {
        // Needs ShipmentService.getShipmentsByCustomerId() implementation
    }

    // Create new shipment
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> createShipment() {
        // Needs ShipmentService.createShipment() implementation
    }

    // Update shipment details
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> updateShipment() {
        // Needs ShipmentService.updateShipment() implementation
    }

    // Mark as delivered
    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> markAsDelivered() {
        // Needs ShipmentService.markAsDelivered() implementation
    }

    // Report delivery problem
    @PostMapping("/{id}/delivery-problem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Shipment>> reportDeliveryProblem() {
        // Needs ShipmentService.reportDeliveryProblem() implementation
    }

    // Get pending shipments
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shipment>>> getPendingShipments() {
        // Needs ShipmentService.getPendingShipments() implementation
    }

    // Get overdue shipments
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shipment>>> getOverdueShipments() {
        // Needs ShipmentService.getOverdueShipments() implementation
    }

    // Search shipments
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Shipment>>> searchShipments() {
        // Needs ShipmentService.searchShipments() implementation
    }

    // Generate shipping label
    @PostMapping("/{id}/label")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> generateShippingLabel() {
        // Needs ShipmentService.generateShippingLabel() implementation
    }

    // Handle carrier webhook
    @PostMapping("/webhook/tracking-update")
    public ResponseEntity<String> handleTrackingWebhook() {
        // Needs ShipmentService webhook processing implementation
    }

    // Statistics and metrics - Version 2.0
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getShipmentStatistics() {
        // Implementation for version 2.0
    }

    // Bulk operations - Version 2.0
    @PostMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Shipment>>> bulkUpdateShipments() {
        // Implementation for version 2.0
    }

    // Analytics - Version 2.0
    @GetMapping("/delivery-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getDeliveryPerformance() {
        // Implementation for version 2.0
    }
    */
}