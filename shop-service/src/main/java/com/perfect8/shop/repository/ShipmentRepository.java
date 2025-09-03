package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shipment entity.
 * Version 1.0 - Core shipment database operations
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // ===== CORE QUERIES (V1.0) =====

    /**
     * Find shipments by order ID
     */
    List<Shipment> findByOrderId(Long orderId);

    /**
     * Find shipment by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Find shipments by status
     */
    List<Shipment> findByStatus(String status);

    /**
     * Find shipments by status with pagination
     */
    Page<Shipment> findByStatus(String status, Pageable pageable);

    /**
     * Find shipments by carrier
     */
    List<Shipment> findByCarrier(String carrier);

    /**
     * Count shipments by status
     */
    long countByStatus(String status);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Find shipments by customer ID (through order relationship)
     */
    @Query("SELECT s FROM Shipment s WHERE s.order.customer.id = :customerId ORDER BY s.createdAt DESC")
    Page<Shipment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find pending shipments (ready to ship but not yet shipped)
     */
    @Query("SELECT s FROM Shipment s WHERE s.status IN ('PENDING', 'PREPARING', 'READY_TO_SHIP') " +
            "ORDER BY s.createdAt ASC")
    List<Shipment> findPendingShipments();

    /**
     * Find shipments in transit
     */
    @Query("SELECT s FROM Shipment s WHERE s.status IN ('SHIPPED', 'IN_TRANSIT') " +
            "ORDER BY s.createdAt ASC")
    List<Shipment> findShipmentsInTransit();

    /**
     * Find overdue shipments (past estimated delivery but not delivered)
     */
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :currentDate " +
            "AND s.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    List<Shipment> findOverdueShipments(@Param("currentDate") LocalDate currentDate);

    /**
     * Find delivered shipments in date range
     */
    @Query("SELECT s FROM Shipment s WHERE s.status = 'DELIVERED' " +
            "AND s.actualDeliveryDate BETWEEN :startDate AND :endDate")
    List<Shipment> findDeliveredShipmentsBetween(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // ===== UPDATE OPERATIONS =====

    /**
     * Update shipment status
     */
    @Modifying
    @Query("UPDATE Shipment s SET s.status = :status, s.updatedAt = :updatedAt WHERE s.id = :shipmentId")
    int updateStatus(@Param("shipmentId") Long shipmentId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update tracking number
     */
    @Modifying
    @Query("UPDATE Shipment s SET s.trackingNumber = :trackingNumber, s.updatedAt = :updatedAt " +
            "WHERE s.id = :shipmentId")
    int updateTrackingNumber(@Param("shipmentId") Long shipmentId,
                             @Param("trackingNumber") String trackingNumber,
                             @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Mark shipment as delivered
     */
    @Modifying
    @Query("UPDATE Shipment s SET s.actualDeliveryDate = :deliveryDate, " +
            "s.status = 'DELIVERED', s.updatedAt = :updatedAt WHERE s.id = :shipmentId")
    int markAsDelivered(@Param("shipmentId") Long shipmentId,
                        @Param("deliveryDate") LocalDate deliveryDate,
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Update current location
     */
    @Modifying
    @Query("UPDATE Shipment s SET s.currentLocation = :location, s.lastLocation = s.currentLocation, " +
            "s.updatedAt = :updatedAt WHERE s.id = :shipmentId")
    int updateLocation(@Param("shipmentId") Long shipmentId,
                       @Param("location") String location,
                       @Param("updatedAt") LocalDateTime updatedAt);

    // ===== BATCH OPERATIONS =====

    /**
     * Find shipments by order IDs
     */
    @Query("SELECT s FROM Shipment s WHERE s.order.id IN :orderIds")
    List<Shipment> findByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * Bulk update shipment status
     */
    @Modifying
    @Query("UPDATE Shipment s SET s.status = :status, s.updatedAt = :updatedAt WHERE s.id IN :shipmentIds")
    int updateStatusBulk(@Param("shipmentIds") List<Long> shipmentIds,
                         @Param("status") String status,
                         @Param("updatedAt") LocalDateTime updatedAt);

    // ===== SEARCH & FILTER =====

    /**
     * Find shipments created between dates
     */
    List<Shipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find shipments with estimated delivery date
     */
    List<Shipment> findByEstimatedDeliveryDate(LocalDate date);

    /**
     * Find shipments by last location
     */
    List<Shipment> findByLastLocation(String lastLocation);

    /**
     * Find returned shipments
     */
    @Query("SELECT s FROM Shipment s WHERE s.status = 'RETURNED'")
    List<Shipment> findReturnedShipments();

    /**
     * Count shipments by carrier
     */
    long countByCarrier(String carrier);

    /* VERSION 2.0 - ADVANCED QUERIES
    // Commented out for v1.0 - will be reimplemented in v2.0

    // Find shipments needing tracking update
    @Query("SELECT s FROM Shipment s WHERE s.status IN ('SHIPPED', 'IN_TRANSIT') " +
            "AND s.updatedAt < :lastUpdateTime")
    List<Shipment> findShipmentsNeedingTrackingUpdate(@Param("lastUpdateTime") LocalDateTime lastUpdateTime);

    // Get average delivery time for carrier
    @Query("SELECT AVG(DATEDIFF(DAY, s.shipmentDate, s.actualDeliveryDate)) FROM Shipment s " +
            "WHERE s.carrier = :carrier AND s.actualDeliveryDate IS NOT NULL")
    Double getAverageDeliveryTimeForCarrier(@Param("carrier") String carrier);

    // Find shipments by weight range
    @Query("SELECT s FROM Shipment s WHERE s.weight BETWEEN :minWeight AND :maxWeight")
    List<Shipment> findByWeightRange(@Param("minWeight") BigDecimal minWeight,
                                     @Param("maxWeight") BigDecimal maxWeight);

    // Complex search query
    @Query("SELECT s FROM Shipment s WHERE " +
           "(:trackingNumber IS NULL OR s.trackingNumber LIKE %:trackingNumber%) AND " +
           "(:carrier IS NULL OR s.carrier = :carrier) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:fromDate IS NULL OR s.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR s.createdAt <= :toDate)")
    Page<Shipment> searchShipments(@Param("trackingNumber") String trackingNumber,
                                   @Param("carrier") String carrier,
                                   @Param("status") String status,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate,
                                   Pageable pageable);
    */
}