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
 * CustomerRepository - Version 1.0
 * 
 * Core customer data access with:
 * - Authentication & Security queries
 * - Customer status queries
 * - Search functionality
 * - Basic statistics
 * 
 * Version 2.0 queries are commented out at the bottom
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ==============================================
    // AUTHENTICATION & SECURITY (v1.0)
    // ==============================================

    /**
     * Find customer by email (used for login)
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if email already exists (registration validation)
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists for different customer (update validation)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
           "WHERE c.email = :email AND c.customerId != :customerId")
    boolean existsByEmailAndCustomerIdNot(@Param("email") String email, @Param("customerId") Long customerId);

    /**
     * Find customer by password reset token
     */
    Optional<Customer> findByResetPasswordToken(String resetPasswordToken);

    /**
     * Find customer by email verification token
     */
    Optional<Customer> findByEmailVerificationToken(String emailVerificationToken);

    /**
     * Find customer by phone number
     */
    Optional<Customer> findByPhone(String phone);

    // ==============================================
    // CUSTOMER STATUS QUERIES (v1.0)
    // ==============================================

    /**
     * Find active customers (paginated)
     */
    Page<Customer> findByActiveTrue(Pageable pageable);

    /**
     * Find inactive customers (paginated)
     */
    Page<Customer> findByActiveFalse(Pageable pageable);

    /**
     * Count active customers (for statistics)
     */
    long countByActiveTrue();

    /**
     * Count inactive customers (for statistics)
     */
    long countByActiveFalse();

    /**
     * Count email verified customers (for statistics)
     */
    long countByEmailVerifiedTrue();

    /**
     * Count email unverified customers (for statistics)
     */
    long countByEmailVerifiedFalse();

    /**
     * Find all verified customers
     */
    List<Customer> findByEmailVerifiedTrue();

    /**
     * Find all unverified customers
     */
    List<Customer> findByEmailVerifiedFalse();

    /**
     * Find customers by active status
     */
    List<Customer> findByActive(Boolean active);

    // ==============================================
    // SEARCH QUERIES (v1.0)
    // ==============================================

    /**
     * Search by email (case insensitive)
     */
    Page<Customer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Search by first name (case insensitive)
     */
    Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    /**
     * Search by last name (case insensitive)
     */
    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    /**
     * Search by name (first or last name)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Search customers by email, name, or phone
     * Main search method used by CustomerService
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%')")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find customers ordered by creation date (newest first)
     * Used for "recent customers" feature
     */
    Page<Customer> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // ==============================================
    // STATISTICS (v1.0)
    // ==============================================

    /**
     * Count customers by preferred currency
     * Returns: List of [currency, count] pairs
     */
    @Query("SELECT c.preferredCurrency, COUNT(c) FROM Customer c GROUP BY c.preferredCurrency")
    List<Object[]> countCustomersByCurrency();

    /**
     * Find customers who have placed at least one order
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o")
    List<Customer> findCustomersWithOrders();

    /**
     * Find customers who have never placed an order
     */
    @Query("SELECT c FROM Customer c WHERE c.orders IS EMPTY")
    List<Customer> findCustomersWithoutOrders();

    /**
     * Find active customers who haven't verified their email
     */
    @Query("SELECT c FROM Customer c WHERE c.emailVerified = false AND c.active = true")
    List<Customer> findCustomersNeedingEmailVerification();

    // ==============================================
    // VERSION 2.0 - COMMENTED OUT
    // ==============================================
    /*
     * These queries use fields or features not in Version 1.0:
     * - registrationDate (v1.0 uses createdDate instead)
     * - Advanced analytics
     * - Soft delete functionality
     * - Expired token cleanup
     *
     * Uncomment when implementing Version 2.0
     */

    // Find customers registered after a date
    // List<Customer> findByRegistrationDateAfter(LocalDateTime date);
    // Page<Customer> findByRegistrationDateAfter(LocalDateTime date, Pageable pageable);
    
    // Find customers registered within date range
    // List<Customer> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count customers registered after date
    // long countByRegistrationDateAfter(LocalDateTime date);
    // long countByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find inactive customers (not logged in since date)
    // @Query("SELECT c FROM Customer c WHERE c.lastLoginDate < :date OR c.lastLoginDate IS NULL")
    // List<Customer> findInactiveCustomersSince(@Param("date") LocalDateTime date);

    // Find new customers registered since date
    // @Query("SELECT c FROM Customer c WHERE c.registrationDate >= :date ORDER BY c.registrationDate DESC")
    // List<Customer> findNewCustomers(@Param("date") LocalDateTime date);

    // Find customers with expired password reset tokens
    // @Query("SELECT c FROM Customer c WHERE c.resetPasswordToken IS NOT NULL " +
    //        "AND c.resetPasswordTokenExpiry < :now")
    // List<Customer> findCustomersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    // Find deleted customers older than date (soft delete)
    // @Query("SELECT c FROM Customer c WHERE c.isDeleted = true AND c.deletedDate < :date")
    // List<Customer> findDeletedCustomersOlderThan(@Param("date") LocalDateTime date);

    // Clear expired password reset tokens (bulk update)
    // @Query("UPDATE Customer c SET c.resetPasswordToken = null, c.resetPasswordTokenExpiry = null " +
    //        "WHERE c.resetPasswordTokenExpiry < :now")
    // int clearExpiredPasswordResetTokens(@Param("now") LocalDateTime now);
}
