package com.perfect8.shop.service;

import com.perfect8.shop.model.Product;
import com.perfect8.shop.model.Category;
import com.perfect8.shop.repository.ProductRepository;
import com.perfect8.shop.repository.CategoryRepository;
import com.perfect8.shop.dto.*;
import com.perfect8.shop.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Product business logic
 *
 * Som stridsledning - koordinerar alla product operations med precision!
 * Swedish air traffic control för e-commerce product management.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ImageService imageService,
            InventoryService inventoryService,
            NotificationService notificationService) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageService = imageService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    // ============================================================================
    // BASIC OPERATIONS (Basic flight operations)
    // ============================================================================

    /**
     * Find product by ID - Mission identification
     */
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        return mapToProductResponse(product);
    }

    /**
     * Find product by SKU - Callsign lookup
     */
    public ProductResponse findBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        return mapToProductResponse(product);
    }

    /**
     * Get all products with advanced filtering - Air traffic overview
     */
    public Page<ProductResponse> findProducts(
            Pageable pageable,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Boolean featured) {

        Page<Product> products = productRepository.findWithFilters(
                categoryId, minPrice, maxPrice, active, featured, null, pageable);

        return products.map(this::mapToProductResponse);
    }

    /**
     * Search products - Radar sweep search
     */
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return findProducts(pageable, null, null, null, true, null);
        }

        Page<Product> products = productRepository.searchByNameOrDescription(query.trim(), pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Find products by category - Squadron grouping
     */
    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        Page<Product> products = productRepository.findByCategoryAndActiveTrue(category, pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Get featured products - VIP aircraft
     */
    public List<ProductResponse> findFeaturedProducts(int limit) {
        List<Product> products = productRepository.findByFeaturedTrueAndActiveTrue();

        return products.stream()
                .limit(limit)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    // ============================================================================
    // INVENTORY MANAGEMENT (Aircraft maintenance)
    // ============================================================================

    /**
     * Get low stock products - Aircraft needing maintenance
     */
    public List<ProductResponse> findLowStockProducts(int threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check stock availability - Flight readiness check
     */
    public AvailabilityResponse checkAvailability(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        boolean available = product.isInStock(quantity);
        String message = available
                ? "Product is available"
                : String.format("Only %d units available", product.getStockQuantity());

        return new AvailabilityResponse(
                productId,
                available,
                quantity,
                product.getStockQuantity(),
                message
        );
    }

    /**
     * Update stock quantity - Aircraft count update
     */
    @Transactional
    public ProductResponse updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Validering
        if (quantity < 0) {
            throw new InvalidStockQuantityException("Stock quantity cannot be negative");
        }

        Integer oldStock = product.getStockQuantity();
        product.setStockQuantity(quantity);
        Product savedProduct = productRepository.save(product);

        // Notification för low stock
        if (quantity <= 5 && oldStock > 5) {
            notificationService.sendLowStockAlert(product);
        }

        // Inventory tracking
        inventoryService.recordStockUpdate(productId, oldStock, quantity, "Manual update");

        return mapToProductResponse(savedProduct);
    }

    /**
     * Get stock status - Aircraft status report
     */
    public StockResponse getStockStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        String stockLevel;
        if (product.getStockQuantity() == 0) {
            stockLevel = "OUT_OF_STOCK";
        } else if (product.getStockQuantity() <= 5) {
            stockLevel = "LOW";
        } else if (product.getStockQuantity() <= 20) {
            stockLevel = "MEDIUM";
        } else {
            stockLevel = "HIGH";
        }

        return new StockResponse(
                productId,
                product.getName(),
                product.getStockQuantity(),
                product.isInStock(),
                stockLevel
        );
    }

    // ============================================================================
    // PRODUCT MANAGEMENT (Fleet management)
    // ============================================================================

    /**
     * Create new product - New aircraft delivery
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // Validering
        validateProductRequest(request);

        // Check duplicate SKU
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + request.getSku() + " already exists");
        }

        // Fetch category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        // Create product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        product.setActive(true);
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        product.setWeight(request.getWeight());
        product.setBrand(request.getBrand());

        Product savedProduct = productRepository.save(product);

        // Inventory tracking
        inventoryService.recordStockUpdate(
                savedProduct.getId(), 0, request.getStockQuantity(), "Initial stock");

        return mapToProductResponse(savedProduct);
    }

    /**
     * Update existing product - Aircraft modification
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Validering
        validateProductUpdateRequest(request);

        // Check SKU change
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new DuplicateSkuException("Product with SKU " + request.getSku() + " already exists");
            }
        }

        // Update fields
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getBrand() != null) product.setBrand(request.getBrand());

        // Category change
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            product.setCategory(newCategory);
        }

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    /**
     * Toggle active status - Aircraft operational status
     */
    @Transactional
    public ProductResponse toggleActiveStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setActive(!product.getActive());
        Product savedProduct = productRepository.save(product);

        return mapToProductResponse(savedProduct);
    }

    /**
     * Delete product - Aircraft decommission
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Check if product has orders
        if (hasActiveOrders(productId)) {
            throw new ProductDeletionException("Cannot delete product with active orders");
        }

        // Soft delete by setting inactive
        product.setActive(false);
        productRepository.save(product);

        // Or hard delete if really needed
        // productRepository.delete(product);
    }

    // ============================================================================
    // BUSINESS INTELLIGENCE (Flight data analysis)
    // ============================================================================

    /**
     * Find related products - Similar aircraft recommendations
     */
    public List<ProductResponse> findRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> relatedProducts = productRepository.findRelatedProducts(
                product.getCategory(), productId, pageable);

        return relatedProducts.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product analytics - Aircraft performance data
     */
    public ProductAnalyticsResponse getProductAnalytics(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Calculate metrics (would typically come from order/view analytics)
        return new ProductAnalyticsResponse(
                productId,
                product.getName(),
                0L, // viewCount - from analytics service
                0L, // orderCount - from order service
                BigDecimal.ZERO, // totalRevenue - from order service
                BigDecimal.ZERO, // averageRating - from review service
                0L  // reviewCount - from review service
        );
    }

    // ============================================================================
    // HELPER METHODS (Ground support)
    // ============================================================================

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .featured(product.getFeatured())
                .weight(product.getWeight())
                .brand(product.getBrand())
                .inStock(product.isInStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Validate product creation request
     */
    private void validateProductRequest(ProductCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("Product name is required");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Product price must be greater than zero");
        }

        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new InvalidProductDataException("Stock quantity cannot be negative");
        }

        if (request.getCategoryId() == null) {
            throw new InvalidProductDataException("Category is required");
        }
    }

    /**
     * Validate product update request
     */
    private void validateProductUpdateRequest(ProductUpdateRequest request) {
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Product price must be greater than zero");
        }

        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be empty");
        }
    }

    /**
     * Check if product has active orders
     */
    private boolean hasActiveOrders(Long productId) {
        // This would typically query the order service or repository
        // For now, returning false as a placeholder
        return false;
    }

    // ============================================================================
    // DTO RESPONSE CLASSES (Communication protocols)
    // ============================================================================

    public static class StockResponse {
        private Long productId;
        private String productName;
        private Integer currentStock;
        private Boolean inStock;
        private String stockLevel;

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

    public static class ProductAnalyticsResponse {
        private Long productId;
        private String productName;
        private Long viewCount;
        private Long orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageRating;
        private Long reviewCount;

        public ProductAnalyticsResponse(Long productId, String productName, Long viewCount, Long orderCount, BigDecimal totalRevenue, BigDecimal averageRating, Long reviewCount) {
            this.productId = productId;
            this.productName = productName;
            this.viewCount = viewCount;
            this.orderCount = orderCount;
            this.totalRevenue = totalRevenue;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Long getViewCount() { return viewCount; }
        public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getAverageRating() { return averageRating; }
        public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

        public Long getReviewCount() { return reviewCount; }
        public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
    }
}