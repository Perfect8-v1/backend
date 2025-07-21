package main.java.com.perfect8.blog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    // Relationship: Many items belong to one Order.
    // This is the other side of the relationship in the Order class.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Relationship: This item refers to one Product.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    // We store the price here to "freeze" it. Even if the Product's price
    // changes later, this order item will always show the price at the
    // time of purchase. This is very important.
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // --- Getters and Setters ---
    // Remember to generate these using Alt+Insert or right-click -> Generate
}
