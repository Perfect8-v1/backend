package com.perfect8.shop.service;

import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ShipmentService - Version 1.0
 * CRUD operations for Shipment entities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Transactional(readOnly = true)
    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getAllShipmentsByOrderId(Long orderId) {
        // FIXED: Repository method - multiple shipments per order
        return shipmentRepository.findAllByOrder_OrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
    }

    @Transactional
    public void updateShipmentStatus(Long shipmentId, String status) {
        Shipment shipment = getShipmentById(shipmentId);
        shipment.setStatus(status);
        shipmentRepository.save(shipment);
    }

    @Transactional(readOnly = true)
    public Page<Shipment> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByStatus(String status) {
        return shipmentRepository.findByShipmentStatus(status);
    }

    @Transactional
    public Shipment updateTrackingInfo(Long shipmentId, String trackingNumber, String carrier) {
        Shipment shipment = getShipmentById(shipmentId);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        return shipmentRepository.save(shipment);
    }
}
