package com.perfect8.blog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.perfect8.adminservice.dto.ProductDto;
import se.perfect8.adminservice.dto.CategoryDto;

@FeignClient(name = "shop-service", url = "${services.shop.url:http://shop-service:8082}")
public interface ShopServiceClient {

    // Products
    @GetMapping("/api/products")
    ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/products/{id}")
    ResponseEntity<?> getProductById(@PathVariable Long id);

    @PostMapping("/api/products")
    ResponseEntity<?> createProduct(@RequestBody ProductDto productDto);

    @PutMapping("/api/products/{id}")
    ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto);

    @DeleteMapping("/api/products/{id}")
    ResponseEntity<?> deleteProduct(@PathVariable Long id);

    @PatchMapping("/api/products/{id}/stock")
    ResponseEntity<?> updateProductStock(@PathVariable Long id, @RequestParam int stock);

    @GetMapping("/api/products/count")
    ResponseEntity<Integer> getProductCount();

    // Categories
    @GetMapping("/api/categories")
    ResponseEntity<?> getAllCategories();

    @PostMapping("/api/categories")
    ResponseEntity<?> createCategory(@RequestBody CategoryDto categoryDto);

    @PutMapping("/api/categories/{id}")
    ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryDto categoryDto);

    @DeleteMapping("/api/categories/{id}")
    ResponseEntity<?> deleteCategory(@PathVariable Long id);

    // Orders
    @GetMapping("/api/orders")
    ResponseEntity<?> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String status);

    @GetMapping("/api/orders/{id}")
    ResponseEntity<?> getOrderById(@PathVariable Long id);

    @PatchMapping("/api/orders/{id}/status")
    ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status);

    @GetMapping("/api/orders/count")
    ResponseEntity<Integer> getOrderCount();

    // Reports
    @GetMapping("/api/reports/sales")
    ResponseEntity<?> getSalesReport(@RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate);

    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
}
