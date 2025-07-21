package main.java.com.perfect8.blog.repository;

import org.springframework.data.repository.ListCrudRepository;
import main.java.com.perfect8.blog.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface is the Repository (the "database clerk") for our Product entity.
 * By extending JpaRepository, we get many powerful database methods for free.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Basic CRUD operations are inherited from JpaRepository.
    // We can add custom query methods here later.
}