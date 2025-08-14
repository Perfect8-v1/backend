package com.perfect8.shop.controller;

import com.perfect8.shop.model.Product;
import com.perfect8.shop.service.ProductService;
import com.perfect8.shop.dto.ProductCreateRequest;
import com.perfect8.shop.dto.ProductUpdateRequest;
import com.perfect8.shop.dto.ProductResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Product management
 *
 * Endpoints:
 * GET    /api/shop/products          - Get all products (paginated)
 * GET    /api/shop/products/{id}     - Get product by ID
 * GET    /api/shop/products/search   - Search products
 * GET    /api/shop/products/category/{categoryId} - Get products by category
 * POST   /api/shop/products          - Create new product (ADMIN)
 * PUT    /api/shop/products/{id}     - Update product (ADMIN)
 * DELETE /api/shop/products/{id}     - Delete product (ADMIN)
 */
@RestController
@RequestMapping("/api/shop/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products with pagination and filtering
     *
     * @param pageable Pagination parameters
     * @param categoryId Optional category filter
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param active Optional active status filter
     * @param featured Optional featured status filter
     * @return Paginated list of products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean featured) {

        Page<ProductResponse> products = productService.findProducts(
                pageable, categoryId, minPrice, maxPrice, active, featured);

        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     *
     * @param id Product ID
     * @return Product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Search products by name or description
     *
     * @param query Search query
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProductResponse> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     *
     * @param categoryId Category ID
     * @param pageable Pagination parameters
     * @return Products in the specified category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProductResponse> products = productService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get featured products
     *
     * @param limit Maximum number of featured products to return
     * @return List of featured products
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {

        List<ProductResponse> products = productService.findFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products with low stock
     *
     * @param threshold Stock threshold (default: 10)
     * @return Products with stock below threshold
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        List<ProductResponse> products = productService.findLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * Create new product (Admin only)
     *
     * @param request Product creation request
     * @return Created product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update existing product (Admin only)
     *
     * @param id Product ID
     * @param request Product update request
     * @return Updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Update product stock
     *
     * @param id Product ID
     * @param quantity New stock quantity
     * @return Updated product
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        ProductResponse product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }

    /**
     * Toggle product active status
     *
     * @param id Product ID
     * @return Updated product
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> toggleActiveStatus(@PathVariable Long id) {
        ProductResponse product = productService.toggleActiveStatus(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Delete product (Admin only)
     *
     * @param id Product ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get product stock status
     *
     * @param id Product ID
     * @return Stock information
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable Long id) {
        StockResponse stock = productService.getStockStatus(id);
        return ResponseEntity.ok(stock);
    }

    /**
     * Check if product is available for purchase
     *
     * @param id Product ID
     * @param quantity Requested quantity
     * @return Availability status
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity) {

        AvailabilityResponse availability = productService.checkAvailability(id, quantity);
        return ResponseEntity.ok(availability);
    }

    /**
     * Get related products (same category, exclude current)
     *
     * @param id Product ID
     * @param limit Maximum number of related products
     * @return List of related products
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int limit) {

        List<ProductResponse> products = productService.findRelatedProducts(id, limit);
        return ResponseEntity.ok(products);
    }

    // DTO Classes for responses
    public static class StockResponse {
        private Long productId;
        private String productName;
        private Integer currentStock;
        private Boolean inStock;
        private String stockLevel; // LOW, MEDIUM, HIGH

        // Constructors, getters, setters
        public StockResponse() {}

        public StockResponse(Long productId, String productName, Integer currentStock, Boolean inStock, String stockLevel) {
            this.productId = productId;
            this.productName = productName;
            this.currentStock = currentStock;
            this.inStock = inStock;
            this.stockLevel = stockLevel;
        }

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public Boolean getInStock() { return inStock; }
        public void setInStock(Boolean inStock) { this.inStock = inStock; }

        public String getStockLevel() { return stockLevel; }
        public void setStockLevel(String stockLevel) { this.stockLevel = stockLevel; }
    }

    public static class AvailabilityResponse {
        private Long productId;
        private Boolean available;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private String message;

        // Constructors, getters, setters
        public AvailabilityResponse() {}

        public AvailabilityResponse(Long productId, Boolean available, Integer requestedQuantity, Integer availableQuantity, String message) {
            this.productId = productId;
            this.available = available;
            this.requestedQuantity = requestedQuantity;
            this.availableQuantity = availableQuantity;
            this.message = message;
        }

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }

        public Integer getRequestedQuantity() { return requestedQuantity; }
        public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

        public Integer getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}