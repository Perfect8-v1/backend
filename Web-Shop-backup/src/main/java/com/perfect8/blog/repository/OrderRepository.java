package com.perfect8.blog.repository;

import main.java.com.perfect8.blog.model.Order_1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order_1, Long>{
    List<Order_1> findByCustomerCustomerId(Long customerId);
}
