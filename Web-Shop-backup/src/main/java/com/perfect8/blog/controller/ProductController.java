package com.perfect8.blog.controller;



import main.java.com.perfect8.blog.model.Product;
import main.java.com.perfect8.blog.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * This is the Controller for handling all product-related API requests.
 * The @RestController annotation combines @Controller and @ResponseBody,
 * which means methods in this class will return data directly as JSON.
 *
 * @RequestMapping("/api/v1/products") sets the base URL for all endpoints in this class.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Handles GET requests to /api/v1/products
     * Fetches all products.
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }

    /**
     * Handles GET requests to /api/v1/products/{id}
     * Fetches a single product by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles POST requests to /api/v1/products
     * Creates a new product.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    /**
     * Handles PUT requests to /api/v1/products/{id}
     * Updates an existing product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productService.findProductById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setStockQuantity(productDetails.getStockQuantity());
                    existingProduct.setFeaturedImageId(productDetails.getFeaturedImageId());
                    //existingProduct.setImageUrl(productDetails.getImageUrl());
                    // Note: You might want more complex logic for updating the category
                    existingProduct.setCategory(productDetails.getCategory());

                    Product updatedProduct = productService.saveProduct(existingProduct);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles DELETE requests to /api/v1/products/{id}
     * Deletes a product.
     * --- THIS METHOD IS NOW CORRECTED ---
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // First, check if the product exists.
        boolean productExists = productService.findProductById(id).isPresent();

        if (productExists) {
            // If it exists, delete it and return 204 No Content.
            productService.deleteProductById(id);
            return ResponseEntity.noContent().build();
        } else {
            // If it does not exist, return 404 Not Found.
            return ResponseEntity.notFound().build();
        }
    }
}