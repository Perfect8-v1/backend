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
 * Hanterar databasoperationer för leveranser
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // ========== Order-relaterade queries ==========

    /**
     * Hitta leverans för en order
     */
    Optional<Shipment> findByOrderId(Long orderId);

    /**
     * Hitta alla leveranser för en order (om flera finns)
     */
    List<Shipment> findAllByOrderId(Long orderId);

    // ========== Tracking queries ==========

    /**
     * Hitta leverans via tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Kontrollera om tracking number existerar
     */
    boolean existsByTrackingNumber(String trackingNumber);

    // ========== Status queries ==========

    /**
     * Hitta leveranser efter status
     */
    List<Shipment> findByShipmentStatus(String shipmentStatus);

    /**
     * Hitta leveranser efter status med paginering
     */
    Page<Shipment> findByShipmentStatus(String shipmentStatus, Pageable pageable);

    /**
     * Räkna leveranser per status
     */
    long countByShipmentStatus(String shipmentStatus);

    // ========== Carrier queries ==========

    /**
     * Hitta leveranser efter transportör
     */
    List<Shipment> findByCarrier(String carrier);

    /**
     * Hitta leveranser efter transportör och status
     */
    List<Shipment> findByCarrierAndShipmentStatus(String carrier, String shipmentStatus);

    /**
     * Räkna leveranser per transportör
     */
    @Query("SELECT s.carrier, COUNT(s) FROM Shipment s GROUP BY s.carrier")
    List<Object[]> countByCarrier();

    // ========== Date queries ==========

    /**
     * Hitta leveranser skickade efter datum
     */
    List<Shipment> findByShippedDateAfter(LocalDateTime date);

    /**
     * Hitta leveranser skickade mellan datum
     */
    List<Shipment> findByShippedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Hitta leveranser levererade mellan datum
     */
    List<Shipment> findByDeliveredDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Hitta förväntade leveranser
     */
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate >= :startDate " +
            "AND s.estimatedDeliveryDate <= :endDate AND s.shipmentStatus != 'DELIVERED'")
    List<Shipment> findExpectedDeliveries(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // ========== Customer queries ==========

    /**
     * Hitta leveranser för en kund
     */
    @Query("SELECT s FROM Shipment s JOIN s.order o WHERE o.customer.customerId = :customerId")
    List<Shipment> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Hitta leveranser för en kund med paginering
     */
    @Query("SELECT s FROM Shipment s JOIN s.order o WHERE o.customer.customerId = :customerId")
    Page<Shipment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // ========== Problem/Issue queries ==========

    /**
     * Hitta försenade leveranser
     */
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :now " +
            "AND s.shipmentStatus NOT IN ('DELIVERED', 'CANCELLED')")
    List<Shipment> findDelayedShipments(@Param("now") LocalDateTime now);

    /**
     * Hitta leveranser som behöver uppdatering
     */
    @Query("SELECT s FROM Shipment s WHERE s.lastUpdated < :cutoffDate " +
            "AND s.shipmentStatus IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    List<Shipment> findShipmentsNeedingUpdate(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== Analytics queries ==========

    /**
     * Hitta senaste leveranser
     */
    List<Shipment> findTop10ByOrderByCreatedAtDesc();

    /**
     * Genomsnittlig leveranstid per transportör
     */
    @Query("SELECT s.carrier, AVG(FUNCTION('DATEDIFF', s.deliveredDate, s.shippedDate)) " +
            "FROM Shipment s WHERE s.deliveredDate IS NOT NULL " +
            "GROUP BY s.carrier")
    List<Object[]> findAverageDeliveryTimeByCarrier();

    /**
     * Leveranser per dag
     */
    @Query("SELECT DATE(s.shippedDate), COUNT(s) FROM Shipment s " +
            "WHERE s.shippedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.shippedDate) ORDER BY DATE(s.shippedDate)")
    List<Object[]> countShipmentsByDay(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // ========== Address queries ==========

    /**
     * Hitta leveranser till postnummer
     */
    List<Shipment> findByShippingPostalCode(String postalCode);

    /**
     * Hitta leveranser till stad
     */
    List<Shipment> findByShippingCity(String city);

    /**
     * Hitta leveranser till land
     */
    List<Shipment> findByShippingCountry(String country);

    // ========== Complex queries ==========

    /**
     * Sök leveranser
     */
    @Query("SELECT s FROM Shipment s WHERE " +
            "LOWER(s.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.carrier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.recipientName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.shippingCity) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Shipment> searchShipments(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Hitta leveranser med filter
     */
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

    /**
     * Räkna aktiva leveranser
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.shipmentStatus " +
            "IN ('PENDING', 'PROCESSING', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    long countActiveShipments();

    /**
     * Räkna levererade denna månad
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE " +
            "YEAR(s.deliveredDate) = YEAR(CURRENT_DATE) AND " +
            "MONTH(s.deliveredDate) = MONTH(CURRENT_DATE)")
    long countDeliveredThisMonth();
}