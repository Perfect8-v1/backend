package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Cart;
import com.perfect8.shop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer(Customer customer);

    Optional<Cart> findByCustomerId(Long customerId);

    List<Cart> findByCustomerIdAndCreatedAtBetween(Long customerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM Cart c WHERE c.customer.id = :customerId AND c.updatedAt > :since")
    List<Cart> findRecentCartsByCustomer(@Param("customerId") Long customerId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.customer.id = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    void deleteByCustomer(Customer customer);

    void deleteByCustomerId(Long customerId);

    @Query("SELECT c FROM Cart c WHERE c.totalAmount > :minAmount")
    List<Cart> findCartsWithMinimumAmount(@Param("minAmount") java.math.BigDecimal minAmount);
}