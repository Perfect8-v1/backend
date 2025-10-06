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

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCustomerIdNot(String email, Long customerId);

    Optional<Customer> findByPasswordResetToken(String passwordResetToken);

    Optional<Customer> findByEmailVerificationToken(String emailVerificationToken);

    Optional<Customer> findByPhoneNumber(String phone);

    // ========== Customer Status Queries ==========

    Page<Customer> findByActiveTrue(Pageable pageable);

    Page<Customer> findByActiveFalse(Pageable pageable);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByEmailVerifiedTrue();

    long countByEmailVerifiedFalse();

    List<Customer> findByEmailVerifiedTrue();

    List<Customer> findByEmailVerifiedFalse();

    List<Customer> findByActive(Boolean active);

    // ========== Search Queries ==========

    Page<Customer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "c.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========== Basic Statistics for v1.0 ==========

    @Query("SELECT c.preferredCurrency, COUNT(c) FROM Customer c GROUP BY c.preferredCurrency")
    List<Object[]> countCustomersByCurrency();

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o")
    List<Customer> findCustomersWithOrders();

    @Query("SELECT c FROM Customer c WHERE c.orders IS EMPTY")
    List<Customer> findCustomersWithoutOrders();

    // FIXED: Changed from c.isEmailVerified to c.emailVerified (Lombok boolean naming)
    @Query("SELECT c FROM Customer c WHERE c.emailVerified = false AND c.active = true")
    List<Customer> findCustomersNeedingEmailVerification();

    /* ========== VERSION 2.0 - COMMENTED OUT ==========
     *
     * These queries use fields not in Version 1.0:
     * - registrationDate
     * - lastLoginDate
     * - passwordResetTokenExpiry
     * - isDeleted
     * - deletedDate
     *
     * Uncomment when these fields are added to Customer entity in v2.0
     */

    // List<Customer> findByRegistrationDateAfter(LocalDateTime date);
    // Page<Customer> findByRegistrationDateAfter(LocalDateTime date, Pageable pageable);
    // List<Customer> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    // long countByRegistrationDateAfter(LocalDateTime date);
    // long countByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // @Query("SELECT c FROM Customer c WHERE c.lastLoginDate < :date OR c.lastLoginDate IS NULL")
    // List<Customer> findInactiveCustomersSince(@Param("date") LocalDateTime date);

    // @Query("SELECT c FROM Customer c WHERE c.registrationDate >= :date ORDER BY c.registrationDate DESC")
    // List<Customer> findNewCustomers(@Param("date") LocalDateTime date);

    // @Query("SELECT c FROM Customer c WHERE c.passwordResetToken IS NOT NULL " +
    //         "AND c.passwordResetTokenExpiry < :now")
    // List<Customer> findCustomersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    // @Query("SELECT c FROM Customer c WHERE c.isDeleted = true AND c.deletedDate < :date")
    // List<Customer> findDeletedCustomersOlderThan(@Param("date") LocalDateTime date);

    // @Query("UPDATE Customer c SET c.passwordResetToken = null, c.passwordResetTokenExpiry = null " +
    //         "WHERE c.passwordResetTokenExpiry < :now")
    // int clearExpiredPasswordResetTokens(@Param("now") LocalDateTime now);
}