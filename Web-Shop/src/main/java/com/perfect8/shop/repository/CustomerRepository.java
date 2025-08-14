package com.perfect8.shop.repository;

import com.perfect8.shop.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity
 *
 * Som Lansen's radar - precision tracking av customers från första kontakt till lojalitet!
 * Swedish engineering för customer relationship management.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ============================================================================
    // BASIC AUTHENTICATION & SECURITY (Som Lansen's IFF-system)
    // ============================================================================

    /**
     * Find customer by email (primary identifier)
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find active customer by email
     */
    Optional<Customer> findByEmailAndActiveTrue(String email);

    /**
     * Find customer by email verification token
     */
    @Query("SELECT c FROM Customer c WHERE c.emailVerificationToken = :token")
    Optional<Customer> findByEmailVerificationToken(@Param("token") String token);

    /**
     * Find customer by password reset token
     */
    @Query("SELECT c FROM Customer c WHERE c.passwordResetToken = :token AND c.passwordResetExpiry > CURRENT_TIMESTAMP")
    Optional<Customer> findByPasswordResetToken(@Param("token") String token);

    // ============================================================================
    // ACCOUNT STATUS MANAGEMENT (Mission status tracking)
    // ============================================================================

    /**
     * Find all active customers
     */
    Page<Customer> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find locked accounts (security alert)
     */
    List<Customer> findByAccountLockedTrueAndActiveTrue();

    /**
     * Find unverified customers
     */
    List<Customer> findByEmailVerifiedFalseAndActiveTrue();

    /**
     * Find customers with failed login attempts
     */
    @Query("SELECT c FROM Customer c WHERE c.failedLoginAttempts >= :threshold AND c.active = true")
    List<Customer> findCustomersWithFailedLogins(@Param("threshold") Integer threshold);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE Customer c SET c.lastLoginAt = CURRENT_TIMESTAMP, c.failedLoginAttempts = 0 WHERE c.email = :email")
    int updateLastLogin(@Param("email") String email);

    /**
     * Increment failed login attempts
     */
    @Modifying
    @Query("UPDATE Customer c SET c.failedLoginAttempts = c.failedLoginAttempts + 1 WHERE c.email = :email")
    int incrementFailedLoginAttempts(@Param("email") String email);

    // ============================================================================
    // CUSTOMER SEGMENTATION (Radar classification)
    // ============================================================================

    /**
     * Find customers by tier (BRONZE, SILVER, GOLD, PLATINUM)
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "CASE " +
            "WHEN c.totalSpent >= 5000 THEN 'PLATINUM' " +
            "WHEN c.totalSpent >= 1000 THEN 'GOLD' " +
            "WHEN c.totalSpent >= 100 THEN 'SILVER' " +
            "ELSE 'BRONZE' " +
            "END = :tier")
    List<Customer> findCustomersByTier(@Param("tier") String tier);

    /**
     * Find VIP customers (high value)
     */
    @Query("SELECT c FROM Customer c WHERE c.totalSpent >= 1000 OR c.totalOrders >= 10")
    List<Customer> findVipCustomers();

    /**
     * Find new customers (registered recently)
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :since AND c.active = true ORDER BY c.createdAt DESC")
    List<Customer> findNewCustomers(@Param("since") LocalDateTime since);

    /**
     * Find returning customers (multiple orders)
     */
    @Query("SELECT c FROM Customer c WHERE c.totalOrders > 1 AND c.active = true")
    List<Customer> findReturningCustomers();

    /**
     * Find dormant customers (no recent activity)
     */
    @Query("SELECT c FROM Customer c WHERE c.lastLoginAt < :cutoffDate AND c.active = true")
    List<Customer> findDormantCustomers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ============================================================================
    // ADVANCED SEARCH & FILTERING (Precision targeting)
    // ============================================================================

    /**
     * Search customers by name or email
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Customer> searchCustomers(@Param("query") String query, Pageable pageable);

    /**
     * Advanced customer filtering
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "(:active IS NULL OR c.active = :active) AND " +
            "(:emailVerified IS NULL OR c.emailVerified = :emailVerified) AND " +
            "(:registeredAfter IS NULL OR c.createdAt >= :registeredAfter) AND " +
            "(:minSpent IS NULL OR c.totalSpent >= :minSpent) AND " +
            "(:maxSpent IS NULL OR c.totalSpent <= :maxSpent) " +
            "ORDER BY c.createdAt DESC")
    Page<Customer> findCustomersWithFilters(
            @Param("active") Boolean active,
            @Param("emailVerified") Boolean emailVerified,
            @Param("registeredAfter") LocalDateTime registeredAfter,
            @Param("minSpent") BigDecimal minSpent,
            @Param("maxSpent") BigDecimal maxSpent,
            Pageable pageable
    );

    /**
     * Find customers by country
     */
    Page<Customer> findByCountryAndActiveTrueOrderByCreatedAtDesc(String country, Pageable pageable);

    /**
     * Find customers by age range
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.dateOfBirth IS NOT NULL AND " +
            "DATEDIFF(CURRENT_DATE, c.dateOfBirth) / 365 BETWEEN :minAge AND :maxAge")
    List<Customer> findCustomersByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    // ============================================================================
    // CUSTOMER ANALYTICS (Business intelligence radar)
    // ============================================================================

    /**
     * Get customer registration metrics by date
     */
    @Query("SELECT DATE(c.createdAt), COUNT(c), " +
            "SUM(CASE WHEN c.emailVerified = true THEN 1 ELSE 0 END) " +
            "FROM Customer c WHERE c.createdAt >= :startDate " +
            "GROUP BY DATE(c.createdAt) ORDER BY DATE(c.createdAt)")
    List<Object[]> getRegistrationMetrics(@Param("startDate") LocalDateTime startDate);

    /**
     * Get customer tier distribution
     */
    @Query("SELECT " +
            "SUM(CASE WHEN c.totalSpent >= 5000 THEN 1 ELSE 0 END) as platinum, " +
            "SUM(CASE WHEN c.totalSpent >= 1000 AND c.totalSpent < 5000 THEN 1 ELSE 0 END) as gold, " +
            "SUM(CASE WHEN c.totalSpent >= 100 AND c.totalSpent < 1000 THEN 1 ELSE 0 END) as silver, " +
            "SUM(CASE WHEN c.totalSpent < 100 THEN 1 ELSE 0 END) as bronze " +
            "FROM Customer c WHERE c.active = true")
    Object[] getCustomerTierDistribution();

    /**
     * Get top customers by spending
     */
    @Query("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.totalSpent DESC")
    List<Customer> getTopCustomersBySpending(Pageable pageable);

    /**
     * Get most frequent customers
     */
    @Query("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.totalOrders DESC")
    List<Customer> getMostFrequentCustomers(Pageable pageable);

    /**
     * Get customer lifetime value statistics
     */
    @Query("SELECT AVG(c.totalSpent), MIN(c.totalSpent), MAX(c.totalSpent), " +
            "AVG(c.averageOrderValue), COUNT(c) " +
            "FROM Customer c WHERE c.active = true AND c.totalOrders > 0")
    Object[] getCustomerLifetimeValueStats();

    /**
     * Get geographic distribution
     */
    @Query("SELECT c.country, COUNT(c), SUM(c.totalSpent) " +
            "FROM Customer c WHERE c.country IS NOT NULL AND c.active = true " +
            "GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> getCustomersByCountry();

    // ============================================================================
    // MARKETING & COMMUNICATION (Communication systems)
    // ============================================================================

    /**
     * Find customers subscribed to newsletter
     */
    List<Customer> findByNewsletterSubscribedTrueAndActiveTrue();

    /**
     * Find customers accepting marketing emails
     */
    List<Customer> findByMarketingEmailsTrueAndActiveTrueAndEmailVerifiedTrue();

    /**
     * Find customers by preferred language
     */
    List<Customer> findByPreferredLanguageAndActiveTrue(String language);

    /**
     * Update customer marketing preferences
     */
    @Modifying
    @Query("UPDATE Customer c SET c.newsletterSubscribed = :newsletter, c.marketingEmails = :marketing " +
            "WHERE c.id = :customerId")
    int updateMarketingPreferences(
            @Param("customerId") Long customerId,
            @Param("newsletter") Boolean newsletter,
            @Param("marketing") Boolean marketing
    );

    // ============================================================================
    // CUSTOMER JOURNEY ANALYTICS (Flight path analysis)
    // ============================================================================

    /**
     * Customer acquisition cohort analysis
     */
    @Query("SELECT YEAR(c.createdAt), MONTH(c.createdAt), COUNT(c), " +
            "AVG(c.totalSpent), AVG(c.totalOrders) " +
            "FROM Customer c WHERE c.createdAt >= :startDate " +
            "GROUP BY YEAR(c.createdAt), MONTH(c.createdAt) " +
            "ORDER BY YEAR(c.createdAt), MONTH(c.createdAt)")
    List<Object[]> getCustomerCohortData(@Param("startDate") LocalDateTime startDate);

    /**
     * Find customers at risk of churning
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.lastLoginAt < :inactiveThreshold AND " +
            "c.totalOrders > 0 AND c.active = true " +
            "ORDER BY c.lastLoginAt ASC")
    List<Customer> findCustomersAtRiskOfChurning(@Param("inactiveThreshold") LocalDateTime inactiveThreshold);

    /**
     * Customer engagement metrics
     */
    @Query("SELECT " +
            "COUNT(c) as totalCustomers, " +
            "SUM(CASE WHEN c.lastLoginAt >= :activeThreshold THEN 1 ELSE 0 END) as activeCustomers, " +
            "SUM(CASE WHEN c.emailVerified = true THEN 1 ELSE 0 END) as verifiedCustomers, " +
            "SUM(CASE WHEN c.totalOrders > 0 THEN 1 ELSE 0 END) as purchasingCustomers " +
            "FROM Customer c WHERE c.active = true")
    Object[] getCustomerEngagementMetrics(@Param("activeThreshold") LocalDateTime activeThreshold);

    // ============================================================================
    // BUSINESS INTELLIGENCE (Strategic radar)
    // ============================================================================

    /**
     * Customer retention rate calculation
     */
    @Query("SELECT " +
            "COUNT(DISTINCT CASE WHEN c.createdAt < :periodStart THEN c.id END) as existingCustomers, " +
            "COUNT(DISTINCT CASE WHEN c.createdAt < :periodStart AND c.lastLoginAt >= :periodStart THEN c.id END) as retainedCustomers " +
            "FROM Customer c WHERE c.active = true")
    Object[] getCustomerRetentionMetrics(
            @Param("periodStart") LocalDateTime periodStart
    );

    /**
     * Customer acquisition cost analysis
     */
    @Query("SELECT DATE(c.createdAt), COUNT(c), " +
            "SUM(CASE WHEN c.totalOrders > 0 THEN 1 ELSE 0 END), " +
            "AVG(c.totalSpent) " +
            "FROM Customer c WHERE c.createdAt >= :startDate " +
            "GROUP BY DATE(c.createdAt) ORDER BY DATE(c.createdAt)")
    List<Object[]> getCustomerAcquisitionMetrics(@Param("startDate") LocalDateTime startDate);

    /**
     * Customer satisfaction indicators
     */
    @Query("SELECT " +
            "AVG(c.totalOrders) as avgOrders, " +
            "AVG(c.averageOrderValue) as avgOrderValue, " +
            "COUNT(CASE WHEN c.totalOrders > 1 THEN 1 END) * 100.0 / COUNT(c) as returnRate " +
            "FROM Customer c WHERE c.active = true AND c.totalOrders > 0")
    Object[] getCustomerSatisfactionMetrics();

    // ============================================================================
    // OPERATIONAL QUERIES (Mission critical operations)
    // ============================================================================

    /**
     * Update customer metrics after order
     */
    @Modifying
    @Query("UPDATE Customer c SET " +
            "c.totalOrders = c.totalOrders + 1, " +
            "c.totalSpent = c.totalSpent + :orderAmount " +
            "WHERE c.id = :customerId")
    int updateCustomerMetricsAfterOrder(
            @Param("customerId") Long customerId,
            @Param("orderAmount") BigDecimal orderAmount
    );

    /**
     * Recalculate average order value
     */
    @Modifying
    @Query("UPDATE Customer c SET c.averageOrderValue = " +
            "CASE WHEN c.totalOrders > 0 THEN c.totalSpent / c.totalOrders ELSE 0 END " +
            "WHERE c.id = :customerId")
    int recalculateAverageOrderValue(@Param("customerId") Long customerId);

    /**
     * Bulk update customer tier based on spending
     */
    @Modifying
    @Query("UPDATE Customer c SET c.customerTier = " +
            "CASE " +
            "WHEN c.totalSpent >= 5000 THEN 'PLATINUM' " +
            "WHEN c.totalSpent >= 1000 THEN 'GOLD' " +
            "WHEN c.totalSpent >= 100 THEN 'SILVER' " +
            "ELSE 'BRONZE' " +
            "END")
    int updateAllCustomerTiers();

    // ============================================================================
    // PERFORMANCE OPTIMIZED QUERIES (Afterburner speed)
    // ============================================================================

    /**
     * Find customer with orders (fetch join)
     */
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.email = :email")
    Optional<Customer> findByEmailWithOrders(@Param("email") String email);

    /**
     * Batch find customers by IDs
     */
    @Query("SELECT c FROM Customer c WHERE c.id IN :customerIds ORDER BY c.lastName, c.firstName")
    List<Customer> findCustomersByIds(@Param("customerIds") List<Long> customerIds);

    /**
     * Find customers for export (lightweight)
     */
    @Query("SELECT c.id, c.email, c.firstName, c.lastName, c.createdAt, c.totalSpent, c.totalOrders " +
            "FROM Customer c WHERE c.active = true ORDER BY c.createdAt DESC")
    List<Object[]> findCustomersForExport();

    // ============================================================================
    // ADMIN & MAINTENANCE (Ground control)
    // ============================================================================

    /**
     * Count customers by status
     */
    @Query("SELECT " +
            "COUNT(c) as total, " +
            "SUM(CASE WHEN c.active = true THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN c.emailVerified = true THEN 1 ELSE 0 END) as verified, " +
            "SUM(CASE WHEN c.accountLocked = true THEN 1 ELSE 0 END) as locked " +
            "FROM Customer c")
    Object[] getCustomerStatusCounts();

    /**
     * Find duplicate emails (data integrity check)
     */
    @Query("SELECT c.email FROM Customer c GROUP BY c.email HAVING COUNT(c) > 1")
    List<String> findDuplicateEmails();

    /**
     * Clean up unverified old accounts
     */
    @Modifying
    @Query("DELETE FROM Customer c WHERE c.emailVerified = false AND c.createdAt < :cutoffDate")
    int deleteUnverifiedOldAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find customers needing data cleanup
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "(c.firstName IS NULL OR c.firstName = '') OR " +
            "(c.lastName IS NULL OR c.lastName = '') OR " +
            "(c.phoneNumber IS NULL OR c.phoneNumber = '') " +
            "AND c.active = true")
    List<Customer> findCustomersNeedingDataCleanup();
}