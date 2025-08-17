package com.perfect8.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Object, Long> {
    // Tillfällig interface för att lösa compilation errors
    // Kommer att uppdateras när Product entity skapas
}