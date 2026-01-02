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
 * MAGNUM OPUS (2025-11-23):
 * - Matches Customer.active field (NOT isActive)
 * - Matches Customer.emailVerified field (NOT isEmailVerified)
 * - Auth fields removed (handled by admin-service)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ==============================================
    // AUTHENTICATION & SECURITY (v1.0)
    // ==============================================

    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUserId(Long userId);
    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.email = :email AND c.customerId != :customerId")
    boolean existsByEmailAndCustomerIdNot(@Param("email") String email, @Param("customerId") Long customerId);

    Optional<Customer> findByPhone(String phone);

    // ==============================================
    // CUSTOMER STATUS QUERIES (v1.0)
    // ==============================================

    Page<Customer> findByActiveTrue(Pageable pageable);
    Page<Customer> findByActiveFalse(Pageable pageable);
    long countByActiveTrue();
    long countByActiveFalse();
    List<Customer> findByActive(Boolean active);

    long countByEmailVerifiedTrue();
    long countByEmailVerifiedFalse();
    List<Customer> findByEmailVerifiedTrue();
    List<Customer> findByEmailVerifiedFalse();

    // ==============================================
    // SEARCH QUERIES (v1.0)
    // ==============================================

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
            "c.phone LIKE CONCAT('%', :searchTerm, '%')")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Customer> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // ==============================================
    // STATISTICS (v1.0)
    // ==============================================

    @Query("SELECT c.preferredCurrency, COUNT(c) FROM Customer c GROUP BY c.preferredCurrency")
    List<Object[]> countCustomersByCurrency();

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders o")
    List<Customer> findCustomersWithOrders();

    @Query("SELECT c FROM Customer c WHERE c.orders IS EMPTY")
    List<Customer> findCustomersWithoutOrders();

    @Query("SELECT c FROM Customer c WHERE c.emailVerified = false AND c.active = true")
    List<Customer> findCustomersNeedingEmailVerification();
}
