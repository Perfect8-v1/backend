package com.perfect8.shop.service;

import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public Shipment createShipment(Shipment shipment) {
        log.info("Creating shipment for order: {}", shipment.getOrder().getOrderId());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setLastUpdated(LocalDateTime.now());
        return shipmentRepository.save(shipment);
    }

    public Shipment updateShipment(Long shipmentId, Shipment updatedShipment) {
        log.info("Updating shipment: {}", shipmentId);
        Shipment existingShipment = getShipmentById(shipmentId);

        existingShipment.setShipmentStatus(updatedShipment.getShipmentStatus());
        existingShipment.setCarrier(updatedShipment.getCarrier());
        existingShipment.setTrackingNumber(updatedShipment.getTrackingNumber());
        existingShipment.setShippedDate(updatedShipment.getShippedDate());
        existingShipment.setEstimatedDeliveryDate(updatedShipment.getEstimatedDeliveryDate());
        existingShipment.setDeliveredDate(updatedShipment.getDeliveredDate());
        existingShipment.setLastUpdated(LocalDateTime.now());

        return shipmentRepository.save(existingShipment);
    }

    public Shipment updateTrackingInfo(Long shipmentId, String trackingNumber, String carrier) {
        log.info("Updating tracking info for shipment: {}", shipmentId);
        Shipment shipment = getShipmentById(shipmentId);

        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        shipment.setLastUpdated(LocalDateTime.now());

        return shipmentRepository.save(shipment);
    }

    public Shipment updateShipmentStatus(Long shipmentId, String newStatus) {
        log.info("Updating shipment {} status to: {}", shipmentId, newStatus);
        Shipment shipment = getShipmentById(shipmentId);

        shipment.setShipmentStatus(newStatus);
        shipment.setLastUpdated(LocalDateTime.now());

        if ("DELIVERED".equals(newStatus) && shipment.getDeliveredDate() == null) {
            shipment.setDeliveredDate(LocalDateTime.now());
        }

        return shipmentRepository.save(shipment);
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
    }

    @Transactional(readOnly = true)
    public Optional<Shipment> getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrder_OrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getAllShipmentsByOrderId(Long orderId) {
        return shipmentRepository.findAllByOrder_OrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByStatus(String status) {
        return shipmentRepository.findByShipmentStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<Shipment> getShipmentsByStatus(String status, Pageable pageable) {
        return shipmentRepository.findByShipmentStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByCarrier(String carrier) {
        return shipmentRepository.findByCarrier(carrier);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getDelayedShipments() {
        return shipmentRepository.findDelayedShipments(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<Shipment> searchShipments(String searchTerm, Pageable pageable) {
        return shipmentRepository.searchShipments(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Shipment> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAll(pageable);
    }

    public void deleteShipment(Long shipmentId) {
        log.info("Deleting shipment: {}", shipmentId);
        shipmentRepository.deleteById(shipmentId);
    }

    @Transactional(readOnly = true)
    public long countActiveShipments() {
        return shipmentRepository.countActiveShipments();
    }
}