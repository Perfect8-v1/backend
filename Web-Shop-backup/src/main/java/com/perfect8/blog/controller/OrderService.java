package com.perfect8.blog.controller;


import com.perfect8.blog.dto.OrderRequestDto;
import com.perfect8.blog.model.*;
import com.perfect8.blog.repository.CustomerRepository;
import com.perfect8.blog.repository.OrderRepository;
import com.perfect8.blog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important import

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository; // We need this to find the customer

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * The core logic for placing an order.
     * @Transactional is CRITICAL. It tells Spring that all the database operations
     * inside this method must either ALL succeed, or if any one of them fails,
     * they should ALL be rolled back. This prevents partial orders.
     */
    @Transactional
    public Order_1 placeOrder(OrderRequestDto orderRequest, String customerEmail) {
        // 1. Find the customer who is placing the order
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + customerEmail));

        // 2. Create the main Order object
        Order_1 order = new Order_1();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED); // We'll set it to CONFIRMED for this simple version
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setBillingAddress(orderRequest.getBillingAddress());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Process each item in the request
        for (var itemDto : orderRequest.getItems()) {
            // Find the product in the database
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.getProductId()));

            // Check for sufficient stock
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Create a new OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(product.getPrice()); // Freeze the price at time of purchase
            orderItem.setOrder(order); // Link the item back to the main order
            orderItems.add(orderItem);

            // Update the total amount
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            // Decrease the product's stock
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product); // Save the updated stock
        }

        // 4. Set the final details on the order
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 5. Save the complete order to the database.
        // Because of CascadeType.ALL, saving the order will also save all its OrderItems.
        return orderRepository.save(order);
    }
}
