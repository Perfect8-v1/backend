package com.perfect8.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Object, Long> {
    // Tillfällig interface - vi lägger till Order entity senare
    // Använder Object som placeholder för att undvika compilation errors
}