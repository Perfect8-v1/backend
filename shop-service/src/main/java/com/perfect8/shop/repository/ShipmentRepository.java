package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Shipment Repository - Version 1.0
 * FIXED: updatedDate not lastUpdatedDate, createdDate not createdDate
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // ========== Order-relaterade queries ==========

    /**
     * Hitta leverans för en order
     * VIKTIGT: Order entity har fältet 'orderId', inte 'customerEmailDTOId'
     */
    Optional<Shipment> findByOrder_OrderId(Long orderId);

    /**
     * Hitta alla leveranser för en order
     */
    List<Shipment> findAllByOrder_OrderId(Long orderId);

    // ========== Tracking queries ==========

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    // ========== Status queries ==========

    List<Shipment> findByShipmentStatus(String shipmentStatus);

    Page<Shipment> findByShipmentStatus(String shipmentStatus, Pageable pageable);

    long countByShipmentStatus(String shipmentStatus);

    // ========== Carrier queries ==========

    List<Shipment> findByCarrier(String carrier);

    List<Shipment> findByCarrierAndShipmentStatus(String carrier, String shipmentStatus);

    @Query("SELECT s.carrier, COUNT(s) FROM Shipment s GROUP BY s.carrier")
    List<Object[]> countByCarrier();

    // ========== Date queries ==========

    List<Shipment> findByShippedDateAfter(LocalDateTime date);

    List<Shipment> findByShippedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Shipment> findByDeliveredDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate >= :startDate " +
            "AND s.estimatedDeliveryDate <= :endDate AND s.shipmentStatus != 'DELIVERED'")
    List<Shipment> findExpectedDeliveries(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // ========== Customer queries ==========

    @Query("SELECT s FROM Shipment s JOIN s.order o WHERE o.customer.customerId = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT s FROM Shipment s JOIN s.order o WHERE o.customer.customerId = :customerId")
    Page<Shipment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // ========== Problem/Issue queries ==========

    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :now " +
            "AND s.shipmentStatus NOT IN ('DELIVERED', 'CANCELLED')")
    List<Shipment> findDelayedShipments(@Param("now") LocalDateTime now);

    // FIXED: updatedDate not lastUpdatedDate
    @Query("SELECT s FROM Shipment s WHERE s.updatedDate < :cutoffDate " +
            "AND s.shipmentStatus IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    List<Shipment> findShipmentsNeedingUpdate(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== Basic queries ==========

    // FIXED: createdDate not createdDate
    List<Shipment> findTop10ByOrderByCreatedDateDesc();

    // ========== Address queries ==========

    List<Shipment> findByShippingPostalCode(String postalCode);

    List<Shipment> findByShippingCity(String city);

    List<Shipment> findByShippingCountry(String country);

    // ========== Search queries ==========

    @Query("SELECT s FROM Shipment s WHERE " +
            "LOWER(s.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.carrier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.recipientName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.shippingCity) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Shipment> searchShipments(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE " +
            "(:status IS NULL OR s.shipmentStatus = :status) AND " +
            "(:carrier IS NULL OR s.carrier = :carrier) AND " +
            "(:startDate IS NULL OR s.shippedDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.shippedDate <= :endDate)")
    Page<Shipment> findWithFilters(@Param("status") String status,
                                   @Param("carrier") String carrier,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    // ========== Statistics ==========

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.shipmentStatus " +
            "IN ('PENDING', 'PROCESSING', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    long countActiveShipments();

    /* ========== VERSION 2.0 - ANALYTICS (COMMENTED OUT) ==========
     *
     * These queries use advanced database functions for analytics
     */

    // @Query("SELECT s.carrier, AVG(FUNCTION('DATEDIFF', s.deliveredDate, s.shippedDate)) " +
    //         "FROM Shipment s WHERE s.deliveredDate IS NOT NULL " +
    //         "GROUP BY s.carrier")
    // List<Object[]> findAverageDeliveryTimeByCarrier();

    // @Query("SELECT DATE(s.shippedDate), COUNT(s) FROM Shipment s " +
    //         "WHERE s.shippedDate BETWEEN :startDate AND :endDate " +
    //         "GROUP BY DATE(s.shippedDate) ORDER BY DATE(s.shippedDate)")
    // List<Object[]> countShipmentsByDay(@Param("startDate") LocalDateTime startDate,
    //                                    @Param("endDate") LocalDateTime endDate);

    // @Query("SELECT COUNT(s) FROM Shipment s WHERE " +
    //         "YEAR(s.deliveredDate) = YEAR(CURRENT_DATE) AND " +
    //         "MONTH(s.deliveredDate) = MONTH(CURRENT_DATE)")
    // long countDeliveredThisMonth();
}