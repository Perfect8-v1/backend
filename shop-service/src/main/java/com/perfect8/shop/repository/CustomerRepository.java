package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Customer;
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
 * Customer Repository - Version 1.0
 * Core database operations for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ========== Authentication & Security ==========

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists excluding specific customer ID
     */
    boolean existsByEmailAndCustomerIdNot(String email, Long customerId);

    /**
     * Find customer by password reset token
     */
    Optional<Customer> findByPasswordResetToken(String passwordResetToken);

    /**
     * Find customer by email verification token
     */
    Optional<Customer> findByEmailVerificationToken(String emailVerificationToken);

    /**
     * Find customer by phone number
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // ========== Customer Status Queries ==========

    /**
     * Find all active customers with pagination
     */
    Page<Customer> findByIsActiveTrue(Pageable pageable);

    /**
     * Find all inactive customers with pagination
     */
    Page<Customer> findByIsActiveFalse(Pageable pageable);

    /**
     * Count active customers
     */
    long countByIsActiveTrue();

    /**
     * Count inactive customers
     */
    long countByIsActiveFalse();

    /**
     * Count verified email customers
     */
    long countByIsEmailVerifiedTrue();

    /**
     * Count unverified email customers
     */
    long countByIsEmailVerifiedFalse();

    /**
     * Find verified customers
     */
    List<Customer> findByIsEmailVerifiedTrue();

    /**
     * Find unverified customers
     */
    List<Customer> findByIsEmailVerifiedFalse();

    /**
     * Find customers by active status
     */
    List<Customer> findByIsActive(Boolean isActive);

    // ========== Registration & Time-based Queries ==========

    /**
     * Find customers registered after specific date
     */
    List<Customer> findByRegistrationDateAfter(LocalDateTime date);

    /**
     * Find customers registered after date with pagination
     */
    Page<Customer> findByRegistrationDateAfter(LocalDateTime date, Pageable pageable);

    /**
     * Find customers registered between dates
     */
    List<Customer> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count customers registered after date
     */
    long countByRegistrationDateAfter(LocalDateTime date);

    /**
     * Count customers registered between dates
     */
    long countByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find customers who haven't logged in since specified date
     */
    @Query("SELECT c FROM Customer c WHERE c.lastLoginDate < :date OR c.lastLoginDate IS NULL")
    List<Customer> findInactiveCustomersSince(@Param("date") LocalDateTime date);

    // ========== Search Queries ==========

    /**
     * Search customers by email containing string
     */
    Page<Customer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Search customers by first name containing string
     */
    Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    /**
     * Search customers by last name containing string
     */
    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    /**
     * Search customers by name (first or last)
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * General customer search
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "c.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========== Statistics Queries for v1.0 ==========

    /**
     * Get new customers from last N days
     */
    @Query("SELECT c FROM Customer c WHERE c.registrationDate >= :date ORDER BY c.registrationDate DESC")
    List<Customer> findNewCustomers(@Param("date") LocalDateTime date);

    /**
     * Count customers by country
     */
    @Query("SELECT c.preferredCurrency, COUNT(c) FROM Customer c GROUP BY c.preferredCurrency")
    List<Object[]> countCustomersByCurrency();

    /**
     * Find customers with orders
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o")
    List<Customer> findCustomersWithOrders();

    /**
     * Find customers without orders
     */
    @Query("SELECT c FROM Customer c WHERE c.orders IS EMPTY")
    List<Customer> findCustomersWithoutOrders();

    /**
     * Find customers needing email verification
     */
    @Query("SELECT c FROM Customer c WHERE c.isEmailVerified = false AND c.isActive = true")
    List<Customer> findCustomersNeedingEmailVerification();

    /**
     * Find customers with expired password reset tokens
     */
    @Query("SELECT c FROM Customer c WHERE c.passwordResetToken IS NOT NULL " +
            "AND c.passwordResetTokenExpiry < :now")
    List<Customer> findCustomersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    // ========== Clean-up Queries ==========

    /**
     * Find soft-deleted customers older than specified date
     */
    @Query("SELECT c FROM Customer c WHERE c.isDeleted = true AND c.deletedDate < :date")
    List<Customer> findDeletedCustomersOlderThan(@Param("date") LocalDateTime date);

    /**
     * Clear expired password reset tokens
     */
    @Query("UPDATE Customer c SET c.passwordResetToken = null, c.passwordResetTokenExpiry = null " +
            "WHERE c.passwordResetTokenExpiry < :now")
    int clearExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    /* VERSION 2.0 QUERIES (kommenterat bort för v1.0):
     *
     * Monthly/yearly customer growth analysis:
     * @Query("SELECT YEAR(c.registrationDate), MONTH(c.registrationDate), COUNT(c) " +
     *        "FROM Customer c WHERE c.registrationDate BETWEEN :startDate AND :endDate " +
     *        "GROUP BY YEAR(c.registrationDate), MONTH(c.registrationDate) " +
     *        "ORDER BY YEAR(c.registrationDate), MONTH(c.registrationDate)")
     * List<Object[]> findMonthlyCustomerGrowth(@Param("startDate") LocalDateTime startDate,
     *                                          @Param("endDate") LocalDateTime endDate);
     *
     * Cohort analysis:
     * @Query("SELECT c, COUNT(o) FROM Customer c LEFT JOIN c.orders o " +
     *        "WHERE c.registrationDate >= :startDate " +
     *        "GROUP BY c ORDER BY c.registrationDate")
     * List<Object[]> findCohortAnalysisData(@Param("startDate") LocalDateTime startDate);
     *
     * Customer lifetime value:
     * @Query("SELECT c, SUM(o.totalAmount) FROM Customer c JOIN c.orders o " +
     *        "WHERE o.orderStatus = 'DELIVERED' GROUP BY c ORDER BY SUM(o.totalAmount) DESC")
     * Page<Object[]> findCustomersByLifetimeValue(Pageable pageable);
     *
     * Churn risk analysis:
     * @Query("SELECT c FROM Customer c WHERE c.lastLoginDate < :cutoffDate " +
     *        "OR (c.lastLoginDate IS NULL AND c.registrationDate < :cutoffDate)")
     * List<Customer> findCustomersAtChurnRisk(@Param("cutoffDate") LocalDateTime cutoffDate);
     *
     * Geographic distribution:
     * List<Customer> findByShippingCountry(String country);
     * List<Customer> findByShippingState(String state);
     * List<Customer> findByShippingCity(String city);
     *
     * Referral tracking:
     * List<Customer> findByReferralCode(String referralCode);
     *
     * Customer segments:
     * List<Customer> findByCustomerSegment(String segment);
     *
     * Marketing preferences:
     * List<Customer> findByMarketingOptInTrue();
     * List<Customer> findByNewsletterSubscribedTrue();
     */
}