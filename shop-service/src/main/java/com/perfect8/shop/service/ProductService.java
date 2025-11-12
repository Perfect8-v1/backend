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
     * FIXED: Changed parameter from 'customerEmailDTOId' to 'productId' (Magnum Opus principle)
     */
    @Transactional(readOnly = true)
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
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
            return productRepository.findByActiveTrue(pageable);
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
            return productRepository.findByActiveTrue(pageable);
        }

        return productRepository.searchByNameOrDescription(query, pageable);
    }

    /**
     * Find products by category
     */
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    /**
     * Find featured products
     */
    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return productRepository.findByFeaturedTrueAndActiveTrue(pageRequest).getContent();
    }

    /**
     * Find low stock products
     */
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanAndActiveTrue(threshold);
    }

    /**
     * Find related products
     * FIXED: Changed .getActivityFeedResponseId() to .getProductId() and .getCategoryId() (Magnum Opus principle)
     */
    @Transactional(readOnly = true)
    public List<Product> findRelatedProducts(Long productId, int limit) {
        Product product = findById(productId);

        if (product.getCategory() == null) {
            // If no category, return random active products
            PageRequest pageRequest = PageRequest.of(0, limit);
            return productRepository.findByActiveTrue(pageRequest)
                    .stream()
                    .filter(p -> !p.getProductId().equals(productId))
                    .collect(Collectors.toList());
        }

        // Find products in same category
        PageRequest pageRequest = PageRequest.of(0, limit + 1); // +1 to exclude current
        List<Product> related = productRepository
                .findByCategoryIdAndActiveTrue(product.getCategory().getCategoryId(), pageRequest)
                .stream()
                .filter(p -> !p.getProductId().equals(productId))
                .limit(limit)
                .collect(Collectors.toList());

        return related;
    }

    /**
     * Create new product
     * FIXED: Changed createdDate -> createdDate (Magnum Opus principle)
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
                .featured(productDTO.isFeatured())
                .active(productDTO.isActive())
                .weight(productDTO.getWeight())
                .dimensions(convertDimensionsToString(productDTO.getDimensions()))  // FIXED: Convert List<String> to String
                .tags(productDTO.getTags())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getProductId());

        return savedProduct;
    }

    /**
     * Update existing product
     * FIXED: Changed setUpdatedDate -> setUpdatedDate (Magnum Opus principle)
     */
    public Product updateProduct(ProductDTO productDTO) {
        log.info("Updating product with ID: {}", productDTO.getProductId());

        Product product = findById(productDTO.getProductId());

        // Check for duplicate SKU if SKU is being changed
        if (!product.getSku().equals(productDTO.getSku()) &&
                productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateSkuException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        // Update category if changed
        if (productDTO.getCategoryId() != null &&
                (product.getCategory() == null || !product.getCategory().getCategoryId().equals(productDTO.getCategoryId()))) {
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

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");

        return updatedProduct;
    }

    /**
     * Delete product (soft delete)
     * FIXED: Changed setUpdatedDate -> setUpdatedDate (Magnum Opus principle)
     */
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);

        Product product = findById(productId);
        product.setActive(false);
        productRepository.save(product);

        log.info("Product deleted (soft delete) successfully");
    }

    /**
     * Toggle product active status
     * FIXED: Changed setUpdatedDate -> setUpdatedDate (Magnum Opus principle)
     */
    public Product toggleActiveStatus(Long productId) {
        log.info("Toggling active status for product ID: {}", productId);

        Product product = findById(productId);
        product.setActive(!product.isActive());

        Product updatedProduct = productRepository.save(product);
        log.info("Product active status toggled to: {}", updatedProduct.isActive());

        return updatedProduct;
    }

    /**
     * Update stock quantity
     * FIXED: Changed setUpdatedDate -> setUpdatedDate (Magnum Opus principle)
     */
    public Product updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product ID: {} to quantity: {}", productId, quantity);

        Product product = findById(productId);
        product.setStockQuantity(quantity);

        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated successfully");

        return updatedProduct;
    }

    /**
     * Adjust stock quantity (increase or decrease)
     * FIXED: Changed setUpdatedDate -> setUpdatedDate (Magnum Opus principle)
     */
    public Product adjustStock(Long productId, Integer adjustment) {
        log.info("Adjusting stock for product ID: {} by: {}", productId, adjustment);

        Product product = findById(productId);
        int newQuantity = product.getStockQuantity() + adjustment;

        if (newQuantity < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newQuantity);

        Product updatedProduct = productRepository.save(product);
        log.info("Stock adjusted successfully. New quantity: {}", newQuantity);

        return updatedProduct;
    }

    /**
     * Check if product is in stock
     * FIXED: Changed parameter from 'customerEmailDTOId' to 'productId' (Magnum Opus principle)
     */
    @Transactional(readOnly = true)
    public boolean isInStock(Long productId) {
        Product product = findById(productId);
        return product.getStockQuantity() > 0;
    }

    /**
     * Check if sufficient stock is available
     * FIXED: Changed parameter from 'customerEmailDTOId' to 'productId' (Magnum Opus principle)
     */
    @Transactional(readOnly = true)
    public boolean hasStock(Long productId, Integer requiredQuantity) {
        Product product = findById(productId);
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
