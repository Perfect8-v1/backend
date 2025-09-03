package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Product;
import com.perfect8.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Product> products = productService.findProducts(
                    pageable, categoryId, minPrice, maxPrice, featured, inStock);

            // Convert to ProductResponse page
            Page<ProductResponse> productResponses = products.map(this::convertToProductResponse);

            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", productResponses));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve products", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            ProductResponse response = convertToProductResponse(product);
            return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.searchProducts(query, pageable);
            Page<ProductResponse> productResponses = products.map(this::convertToProductResponse);

            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", productResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.findByCategory(categoryId, pageable);
            Page<ProductResponse> productResponses = products.map(this::convertToProductResponse);

            return ResponseEntity.ok(ApiResponse.success("Products by category retrieved successfully", productResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve products by category", e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Product> products = productService.findFeaturedProducts(limit);
            List<ProductResponse> productResponses = products.stream()
                    .map(this::convertToProductResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("Featured products retrieved successfully", productResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve featured products", e.getMessage()));
        }
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        try {
            List<Product> products = productService.findLowStockProducts(threshold);
            List<ProductResponse> productResponses = products.stream()
                    .map(this::convertToProductResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", productResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve low stock products", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        try {
            ProductDTO productDTO = convertToProductDTO(request);
            Product createdProduct = productService.createProduct(productDTO);
            ProductResponse response = convertToProductResponse(createdProduct);

            return ResponseEntity.ok(ApiResponse.success("Product created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to create product", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        try {
            ProductDTO productDTO = convertToProductDTO(request, id);
            Product updatedProduct = productService.updateProduct(productDTO);
            ProductResponse response = convertToProductResponse(updatedProduct);

            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to update product", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to delete product", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(@PathVariable Long id) {
        try {
            Product product = productService.toggleActiveStatus(id);
            ProductResponse response = convertToProductResponse(product);

            return ResponseEntity.ok(ApiResponse.success("Product status toggled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to toggle product status", e.getMessage()));
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Integer>> getStockLevel(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("Stock level retrieved successfully", product.getStockQuantity()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve stock level", e.getMessage()));
        }
    }

    @PostMapping("/{id}/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            Product product = productService.findById(id);
            Boolean available = product.getStockQuantity() >= quantity;
            String message = available ? "Product is available" : "Product is not available in requested quantity";

            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to check availability", e.getMessage()));
        }
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Product> products = productService.findRelatedProducts(id, limit);
            List<ProductResponse> productResponses = products.stream()
                    .map(this::convertToProductResponse)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success("Related products retrieved successfully", productResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to retrieve related products", e.getMessage()));
        }
    }

    // Helper conversion methods
    private ProductResponse convertToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .sku(product.getSku())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .featured(product.isFeatured())
                .active(product.isActive())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductDTO convertToProductDTO(ProductCreateRequest request) {
        return ProductDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .sku(request.getSku())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .categoryId(request.getCategoryId())
                .featured(request.isFeatured())
                .active(true)
                .weight(request.getWeight() != null ? new BigDecimal(request.getWeight()) : null)  // FIXED: Convert Double to BigDecimal
                .dimensions(request.getDimensions())  // This is already List<String> in request
                .tags(request.getTags())
                .build();
    }

    private ProductDTO convertToProductDTO(ProductUpdateRequest request, Long id) {
        return ProductDTO.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .categoryId(request.getCategoryId())
                .featured(request.isFeatured())
                .active(request.isActive())
                .weight(request.getWeight() != null ? new BigDecimal(request.getWeight()) : null)  // FIXED: Convert Double to BigDecimal
                .dimensions(request.getDimensions())  // This is already List<String> in request
                .tags(request.getTags())
                .build();
    }
}