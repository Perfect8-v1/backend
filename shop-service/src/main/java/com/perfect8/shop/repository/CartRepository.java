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

    Optional<Cart> findFirstByCustomerOrderByCreatedDateDesc(Customer customer);

    default Optional<Cart> findByCustomer(Customer customer) {
        return findFirstByCustomerOrderByCreatedDateDesc(customer);
    }

    Optional<Cart> findFirstByCustomerCustomerIdOrderByCreatedDateDesc(Long customerId);

    default Optional<Cart> findByCustomerId(Long customerId) {
        return findFirstByCustomerCustomerIdOrderByCreatedDateDesc(customerId);
    }

    @Query("SELECT c FROM Cart c WHERE c.customer.customerId = :customerId AND c.createdDate BETWEEN :start AND :end")
    List<Cart> findByCustomerIdAndCreatedDateBetween(@Param("customerId") Long customerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c FROM Cart c WHERE c.customer.customerId = :customerId AND c.updatedDate > :since")
    List<Cart> findRecentCartsByCustomer(@Param("customerId") Long customerId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.customer.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    @Query("DELETE FROM Cart c WHERE c.customer = :customer")
    void deleteByCustomer(@Param("customer") Customer customer);

    @Query("DELETE FROM Cart c WHERE c.customer.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT c FROM Cart c WHERE c.totalAmount > :minAmount")
    List<Cart> findCartsWithMinimumAmount(@Param("minAmount") java.math.BigDecimal minAmount);
}