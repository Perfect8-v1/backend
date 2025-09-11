package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Shipment;
import com.perfect8.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shipment entity
 * Version 1.0 - Core shipment data access
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Find by tracking number
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    // Find shipments by order ID
    List<Shipment> findByOrderOrderId(Long orderId);

    // Alternative method name for compatibility
    List<Shipment> findByOrder_OrderId(Long orderId);

    // Find shipments by order
    List<Shipment> findByOrder(Order order);

    // Find shipments by status
    List<Shipment> findByStatus(String status);

    // Find shipments by carrier
    List<Shipment> findByCarrier(String carrier);

    // Find shipments by date range
    List<Shipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find shipments with expected delivery date
    List<Shipment> findByEstimatedDeliveryDate(LocalDate deliveryDate);

    // Find shipments delivered on specific date
    List<Shipment> findByActualDeliveryDate(LocalDate deliveryDate);

    // Find pending shipments
    @Query("SELECT s FROM Shipment s WHERE s.status = 'PENDING' OR s.status = 'PREPARING'")
    List<Shipment> findPendingShipments();

    // Find in-transit shipments
    @Query("SELECT s FROM Shipment s WHERE s.status = 'IN_TRANSIT'")
    List<Shipment> findInTransitShipments();

    // Find delivered shipments
    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED'")
    List<Shipment> findDeliveredShipments();

    // Find shipments by customer (through order)
    @Query("SELECT s FROM Shipment s WHERE s.order.customer.customerId = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);

    // Find shipments by customer email (through order)
    @Query("SELECT s FROM Shipment s WHERE s.order.shippingEmail = :email")
    List<Shipment> findByCustomerEmail(@Param("email") String email);

    // Count shipments by status
    Long countByStatus(String status);

    // Find overdue shipments
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :currentDate " +
            "AND s.status != 'DELIVERED' AND s.status != 'CANCELLED'")
    List<Shipment> findOverdueShipments(@Param("currentDate") LocalDate currentDate);

    // Find shipments needing tracking update
    @Query("SELECT s FROM Shipment s WHERE s.status = 'IN_TRANSIT' " +
            "AND s.updatedAt < :lastUpdateTime")
    List<Shipment> findShipmentsNeedingTrackingUpdate(@Param("lastUpdateTime") LocalDateTime lastUpdateTime);

    // Search shipments
    @Query("SELECT s FROM Shipment s WHERE " +
            "s.trackingNumber LIKE %:query% OR " +
            "s.carrier LIKE %:query% OR " +
            "s.order.orderNumber LIKE %:query%")
    Page<Shipment> searchShipments(@Param("query") String query, Pageable pageable);

    // Find shipments with pagination
    Page<Shipment> findByStatus(String status, Pageable pageable);

    // Find recent shipments
    List<Shipment> findTop10ByOrderByCreatedAtDesc();

    // Check if tracking number exists
    Boolean existsByTrackingNumber(String trackingNumber);

    // Delete shipments by order
    void deleteByOrder(Order order);

    // Version 2.0 - Commented out for future implementation
    /*
    // Analytics queries for v2.0
    @Query("SELECT COUNT(s), s.carrier FROM Shipment s " +
           "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY s.carrier")
    List<Object[]> getShipmentStatsByCarrier(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(DATEDIFF(s.actualDeliveryDate, s.createdAt)) FROM Shipment s " +
           "WHERE s.status = 'DELIVERED' AND s.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageDeliveryTime(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Performance metrics for v2.0
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.actualDeliveryDate > s.estimatedDeliveryDate")
    Long countLateDeliveries();
    */
}