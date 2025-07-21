package main.java.com.perfect8.blog.repository;

import main.java.com.perfect8.blog.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByCustomerCustomerId(Long customerId);
}
