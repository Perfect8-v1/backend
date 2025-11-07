package com.perfect8.feign;

import com.perfect8.dto.ProductDto;
import com.perfect8.dto.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "shop-service", url = "http://localhost:8082")
public interface ShopServiceClient {

    @GetMapping("/api/products")
    ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @GetMapping("/api/products/{customerEmailDTOId}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Long id);

    @GetMapping("/api/categories")
    ResponseEntity<List<CategoryDto>> getAllCategories();

    @GetMapping("/api/orders/stats")
    ResponseEntity<Map<String, Object>> getOrderStats();
}
