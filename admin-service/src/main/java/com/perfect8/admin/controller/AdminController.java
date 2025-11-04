package com.perfect8.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Mock data - senare kan du implementera Feign calls till andra services
            stats.put("totalProducts", 125);
            stats.put("lowStockProducts", 8);
            stats.put("activeProducts", 117);
            stats.put("totalCategories", 15);
            stats.put("totalPosts", 23);
            stats.put("publishedPosts", 18);
            stats.put("totalCustomers", 312);
            stats.put("totalOrders", 84);
            stats.put("totalRevenue", 25847.50);
            stats.put("pendingOrders", 12);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Dashboard statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "DASHBOARD_ERROR",
                    "message", "Failed to retrieve dashboard statistics: " + e.getMessage()
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        try {
//TODO Mock till feign
            // Mock data - senare kan du implementera Feign call till shop-service
            Map<String, Object> mockData = new HashMap<>();
            mockData.put("content", List.of(
                    Map.of("id", 1, "name", "iPhone 15 Pro Max", "price", 1199.99, "stock", 25, "active", true),
                    Map.of("id", 2, "name", "Samsung Galaxy S24 Ultra", "price", 1299.99, "stock", 30, "active", true),
                    Map.of("id", 3, "name", "MacBook Pro 16\"", "price", 3999.99, "stock", 3, "active", true)
            ));

            mockData.put("page", Map.of(
                    "number", page,
                    "size", size,
                    "totalElements", 125,
                    "totalPages", 7
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", mockData);
            response.put("message", "Products retrieved successfully (mock data)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "PRODUCTS_ERROR",
                    "message", "Failed to retrieve products: " + e.getMessage()
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getCategories() {
        try {
            // Mock data - senare implementera Feign call till shop-service
            Map<String, Object> mockData = Map.of(
                    "categories", List.of(
                            Map.of("id", 1, "name", "Electronics", "active", true, "productCount", 45),
                            Map.of("id", 2, "name", "Fashion", "active", true, "productCount", 32),
                            Map.of("id", 3, "name", "Books", "active", true, "productCount", 28)
                    )
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", mockData);
            response.put("message", "Categories retrieved successfully (mock data)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "CATEGORIES_ERROR",
                    "message", "Failed to retrieve categories: " + e.getMessage()
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/posts")
    @PreAuthorize("hasRole('CONTENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
//TODO Mock till feign
            // Mock data - senare implementera Feign call till blog-service
            Map<String, Object> mockData = Map.of(
                    "posts", List.of(
                            Map.of("id", 1, "title", "Top 10 Smartphones of 2024", "status", "PUBLISHED", "author", "Tech Team"),
                            Map.of("id", 2, "title", "Sustainable Fashion Guide", "status", "PUBLISHED", "author", "Style Team"),
                            Map.of("id", 3, "title", "Home Office Setup", "status", "DRAFT", "author", "Productivity Team")
                    )
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", mockData);
            response.put("message", "Posts retrieved successfully (mock data)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "POSTS_ERROR",
                    "message", "Failed to retrieve posts: " + e.getMessage()
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/system/health")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSystemHealth() {
        Map<String, Object> systemHealth = new HashMap<>();
//TODO Mock till feign
        // Mock health status - senare implementera Feign health checks
        systemHealth.put("adminService", "UP");
        systemHealth.put("shopService", "UP (not tested)");
        systemHealth.put("blogService", "UP (not tested)");
        systemHealth.put("imageService", "UP (not tested)");
        systemHealth.put("database", "UP");
        systemHealth.put("overallStatus", "HEALTHY");
        systemHealth.put("lastCheck", java.time.LocalDateTime.now().toString());
        systemHealth.put("note", "Feign client health checks not implemented yet");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", systemHealth);
        response.put("message", "System health check completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> toggleProductStatus(@PathVariable Long id) {
        try {
//TODO Mock till feign
            // Mock response - senare implementera Feign call till shop-service
            Map<String, Object> result = new HashMap<>();
            result.put("productId", id);
            result.put("message", "Product status toggle request sent to shop-service");
            result.put("note", "Actual implementation requires shop-service endpoint");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "TOGGLE_ERROR",
                    "message", "Failed to toggle product status: " + e.getMessage()
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}