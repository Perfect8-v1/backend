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

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Find by email
    Optional<Customer> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if email exists excluding specific ID
    boolean existsByEmailAndIdNot(String email, Long id);

    // Find active customers
    Page<Customer> findByIsActiveTrue(Pageable pageable);

    // Count active customers
    long countByIsActiveTrue();

    // Count verified email customers - ADDED
    long countByIsEmailVerifiedTrue();

    // Find customers registered after date
    List<Customer> findByRegistrationDateAfter(LocalDateTime date);

    // Find customers registered after date with pagination - ADDED
    List<Customer> findByRegistrationDateAfter(LocalDateTime date, Pageable pageable);

    // Count customers registered after date
    long countByRegistrationDateAfter(LocalDateTime date);

    // Count customers registered between dates
    long countByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find customers by active status
    List<Customer> findByIsActive(Boolean isActive);

    // Find verified customers
    List<Customer> findByIsEmailVerifiedTrue();

    // Find unverified customers
    List<Customer> findByIsEmailVerifiedFalse();

    // Find customers by phone
    Optional<Customer> findByPhone(String phone);

    // Find by email containing - ADDED
    Page<Customer> findByEmailContaining(String email, Pageable pageable);

    // Custom query for new customers with limit and offset
    @Query("SELECT c FROM Customer c WHERE c.registrationDate > :date ORDER BY c.registrationDate DESC")
    List<Customer> findNewCustomersWithLimitAndOffset(
            @Param("date") LocalDateTime date,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

    // Find customers by name (first or last)
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByNameContaining(@Param("name") String name);

    // Monthly customer growth data
    @Query("SELECT YEAR(c.registrationDate), MONTH(c.registrationDate), COUNT(c) FROM Customer c WHERE c.registrationDate BETWEEN :startDate AND :endDate GROUP BY YEAR(c.registrationDate), MONTH(c.registrationDate) ORDER BY YEAR(c.registrationDate), MONTH(c.registrationDate)")
    List<Object[]> findMonthlyCustomerGrowth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Cohort analysis data
    @Query("SELECT c FROM Customer c WHERE c.registrationDate >= :startDate ORDER BY c.registrationDate")
    List<Object[]> findCohortAnalysisData(@Param("startDate") LocalDateTime startDate);

    // Customers at churn risk (no activity in last X days)
    @Query("SELECT c FROM Customer c WHERE c.lastLogin < :cutoffDate OR c.lastLogin IS NULL")
    List<Object[]> findCustomersAtChurnRisk(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find customers by country
    List<Customer> findByCountry(String country);

    // Find customers by city
    List<Customer> findByCity(String city);

    // Find customers by referral code
    List<Customer> findByReferralCode(String referralCode);

    // Count customers by email verification status
    long countByIsEmailVerified(Boolean isEmailVerified);

    // Find top customers by login count
    @Query("SELECT c FROM Customer c WHERE c.loginCount IS NOT NULL ORDER BY c.loginCount DESC")
    List<Customer> findTopCustomersByLoginCount(Pageable pageable);

    // Search customers
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);
}