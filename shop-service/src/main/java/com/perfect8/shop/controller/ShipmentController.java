package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.service.ShipmentService;
import com.perfect8.shop.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShippingService shippingService;

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> trackShipment(@PathVariable String trackingNumber) {
        log.info("Tracking shipment: {}", trackingNumber);

        return shipmentService.getShipmentByTrackingNumber(trackingNumber)
                .map(shipment -> ResponseEntity.ok(convertToDTO(shipment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ShipmentDTO>> getOrderShipments(@PathVariable Long orderId) {
        log.info("Getting shipments for order: {}", orderId);

        List<Shipment> shipments = shipmentService.getAllShipmentsByOrderId(orderId);
        List<ShipmentDTO> dtos = shipments.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentDTO> getShipment(@PathVariable Long shipmentId) {
        log.info("Getting shipment: {}", shipmentId);

        try {
            Shipment shipment = shipmentService.getShipmentById(shipmentId);
            return ResponseEntity.ok(convertToDTO(shipment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{shipmentId}/status")
    public ResponseEntity<ShipmentDTO> updateStatus(
            @PathVariable Long shipmentId,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "") String notes) {

        log.info("Updating shipment {} status to: {} with notes: {}", shipmentId, status, notes);

        // Använd ShippingService för att uppdatera med notes
        shippingService.updateShipmentStatus(shipmentId, status, notes);

        // Hämta uppdaterad shipment
        Shipment updated = shipmentService.getShipmentById(shipmentId);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<Void> cancelShipment(@PathVariable Long shipmentId) {
        log.info("Cancelling shipment: {}", shipmentId);

        Shipment shipment = shipmentService.getShipmentById(shipmentId);
        shipmentService.updateShipmentStatus(shipmentId, "CANCELLED");

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentDTO>> getAllShipments(Pageable pageable) {
        log.info("Getting all shipments - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Shipment> shipments = shipmentService.getAllShipments(pageable);
        Page<ShipmentDTO> dtos = shipments.map(this::convertToDTO);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ShipmentDTO>> getShipmentsByStatus(@PathVariable String status) {
        log.info("Getting shipments with status: {}", status);

        List<Shipment> shipments = shipmentService.getShipmentsByStatus(status);
        List<ShipmentDTO> dtos = shipments.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentDTO> updateTracking(
            @PathVariable Long shipmentId,
            @Valid @RequestBody TrackingUpdateDTO trackingUpdate) {

        log.info("Updating tracking for shipment: {}", shipmentId);

        Shipment updated = shipmentService.updateTrackingInfo(
                shipmentId,
                trackingUpdate.getTrackingNumber(),
                trackingUpdate.getCarrier()
        );

        return ResponseEntity.ok(convertToDTO(updated));
    }

    /**
     * Convert Shipment entity to DTO
     * MAGNUM OPUS COMPLIANT: SAMMA fältnamn som entity och DTO
     * UPDATED: Includes all v1.0 fields including moved v2.0 fields
     */
    private ShipmentDTO convertToDTO(Shipment shipment) {
        return ShipmentDTO.builder()
                // Core shipment info
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrder().getOrderId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .shippingMethod(shipment.getShippingMethod())
                .status(shipment.getShipmentStatus())

                // Dates
                .shippedDate(shipment.getShippedDate())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .deliveredDate(shipment.getDeliveredDate())

                // Costs
                .shippingCost(shipment.getShippingCost())

                // Recipient info (v1.0)
                .recipientName(shipment.getRecipientName())
                .recipientPhone(shipment.getRecipientPhone() != null ? shipment.getRecipientPhone() : "")
                .recipientEmail(shipment.getRecipientEmail())

                // Address fields
                .shippingAddress(shipment.getShippingAddress())
                .shippingStreet(shipment.getShippingStreet())
                .shippingCity(shipment.getShippingCity())
                .shippingState(shipment.getShippingState())
                .shippingPostalCode(shipment.getShippingPostalCode())
                .shippingCountry(shipment.getShippingCountry())

                // Physical properties (moved from v2.0 to v1.0)
                .weight(shipment.getWeight() != null ? shipment.getWeight() : BigDecimal.ZERO)
                .dimensions(shipment.getDimensions() != null ? shipment.getDimensions() : "")

                // Delivery options (moved from v2.0 to v1.0)
                .deliveryInstructions(shipment.getDeliveryInstructions())
                .signatureRequired(shipment.isSignatureRequired())
                .insuranceAmount(shipment.getInsuranceAmount())

                // Tracking
                .currentLocation(shipment.getCurrentLocation())
                .lastUpdated(shipment.getUpdatedDate())

                // Label and notes
                .labelUrl(shipment.getLabelUrl())
                .notes(shipment.getNotes())

                // Default values for DTO-only fields (v2.0)
                .numberOfPackages(1)
                .packageType("BOX")
                .fragile(false)
                .hazardous(false)
                .perishable(false)
                .priorityHandling(false)
                .confirmationType("NONE")
                .deliveryAttempts(0)

                .build();
    }
}