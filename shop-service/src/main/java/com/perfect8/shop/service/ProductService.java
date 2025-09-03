package com.perfect8.shop.service;

import com.perfect8.shop.entity.Product;
import com.perfect8.shop.entity.Category;
import com.perfect8.shop.dto.ProductDTO;
import com.perfect8.shop.repository.ProductRepository;
import com.perfect8.shop.repository.CategoryRepository;
import com.perfect8.shop.exception.ProductNotFoundException;
import com.perfect8.shop.exception.DuplicateSkuException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Find product by ID
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Find products with filters
     */
    @Transactional(readOnly = true)
    public Page<Product> findProducts(Pageable pageable, Long categoryId,
                                      BigDecimal minPrice, BigDecimal maxPrice,
                                      Boolean featured, Boolean inStock) {

        // If no filters, return all active products
        if (categoryId == null && minPrice == null && maxPrice == null &&
                featured == null && inStock == null) {
            return productRepository.findByIsActiveTrue(pageable);
        }

        // Build query based on filters
        return productRepository.findProductsWithFilters(
                categoryId, minPrice, maxPrice, featured, inStock, pageable
        );
    }

    /**
     * Search products by name or description
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findByIsActiveTrue(pageable);
        }

        return productRepository.searchByNameOrDescription(query, pageable);
    }

    /**
     * Find products by category
     */
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    /**
     * Find featured products
     */
    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageRequest).getContent();
    }

    /**
     * Find low stock products
     */
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanAndIsActiveTrue(threshold);
    }

    /**
     * Find related products
     */
    @Transactional(readOnly = true)
    public List<Product> findRelatedProducts(Long productId, int limit) {
        Product product = findById(productId);

        if (product.getCategory() == null) {
            // If no category, return random active products
            PageRequest pageRequest = PageRequest.of(0, limit);
            return productRepository.findByIsActiveTrue(pageRequest)
                    .stream()
                    .filter(p -> !p.getId().equals(productId))
                    .collect(Collectors.toList());
        }

        // Find products in same category
        PageRequest pageRequest = PageRequest.of(0, limit + 1); // +1 to exclude current
        List<Product> related = productRepository
                .findByCategoryIdAndIsActiveTrue(product.getCategory().getId(), pageRequest)
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .collect(Collectors.toList());

        return related;
    }

    /**
     * Create new product
     */
    public Product createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check for duplicate SKU
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        // Get category
        Category category = null;
        if (productDTO.getCategoryId() != null) {
            category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + productDTO.getCategoryId()));
        }

        // Create product entity
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .discountPrice(productDTO.getDiscountPrice())
                .sku(productDTO.getSku())
                .stockQuantity(productDTO.getStockQuantity())
                .imageUrl(productDTO.getImageUrl())
                .category(category)
                .isFeatured(productDTO.isFeatured())
                .isActive(productDTO.isActive())
                .weight(productDTO.getWeight())
                .dimensions(convertDimensionsToString(productDTO.getDimensions()))  // FIXED: Convert List<String> to String
                .tags(productDTO.getTags())
                .createdAt(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        return savedProduct;
    }

    /**
     * Update existing product
     */
    public Product updateProduct(ProductDTO productDTO) {
        log.info("Updating product with ID: {}", productDTO.getId());

        Product product = findById(productDTO.getId());

        // Check for duplicate SKU if SKU is being changed
        if (!product.getSku().equals(productDTO.getSku()) &&
                productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        // Update category if changed
        if (productDTO.getCategoryId() != null &&
                (product.getCategory() == null || !product.getCategory().getId().equals(productDTO.getCategoryId()))) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        // Update fields
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setDiscountPrice(productDTO.getDiscountPrice());
        product.setSku(productDTO.getSku());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setFeatured(productDTO.isFeatured());
        product.setActive(productDTO.isActive());
        product.setWeight(productDTO.getWeight());
        product.setDimensions(convertDimensionsToString(productDTO.getDimensions()));  // FIXED: Convert List<String> to String
        product.setTags(productDTO.getTags());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");

        return updatedProduct;
    }

    /**
     * Delete product (soft delete)
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = findById(id);
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Product deleted (soft delete) successfully");
    }

    /**
     * Toggle product active status
     */
    public Product toggleActiveStatus(Long id) {
        log.info("Toggling active status for product ID: {}", id);

        Product product = findById(id);
        product.setActive(!product.isActive());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Product active status toggled to: {}", updatedProduct.isActive());

        return updatedProduct;
    }

    /**
     * Update stock quantity
     */
    public Product updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product ID: {} to quantity: {}", id, quantity);

        Product product = findById(id);
        product.setStockQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated successfully");

        return updatedProduct;
    }

    /**
     * Adjust stock quantity (increase or decrease)
     */
    public Product adjustStock(Long id, Integer adjustment) {
        log.info("Adjusting stock for product ID: {} by: {}", id, adjustment);

        Product product = findById(id);
        int newQuantity = product.getStockQuantity() + adjustment;

        if (newQuantity < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newQuantity);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Stock adjusted successfully. New quantity: {}", newQuantity);

        return updatedProduct;
    }

    /**
     * Check if product is in stock
     */
    @Transactional(readOnly = true)
    public boolean isInStock(Long id) {
        Product product = findById(id);
        return product.getStockQuantity() > 0;
    }

    /**
     * Check if sufficient stock is available
     */
    @Transactional(readOnly = true)
    public boolean hasStock(Long id, Integer requiredQuantity) {
        Product product = findById(id);
        return product.getStockQuantity() >= requiredQuantity;
    }

    /**
     * Convert dimensions list to string for storage
     * FIXED: Helper method to convert List<String> to String
     */
    private String convertDimensionsToString(List<String> dimensions) {
        if (dimensions == null || dimensions.isEmpty()) {
            return null;
        }
        // Join dimensions with 'x' separator (e.g., "10cm x 20cm x 30cm")
        return String.join(" x ", dimensions);
    }

    /**
     * Convert dimensions string to list
     * Helper method for converting back from String to List<String>
     */
    private List<String> convertDimensionsToList(String dimensions) {
        if (dimensions == null || dimensions.trim().isEmpty()) {
            return null;
        }
        // Split by 'x' separator
        return List.of(dimensions.split(" x "));
    }
}