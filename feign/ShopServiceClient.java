@FeignClient(name = "shop-service", url = "http://shop-service:8085", configuration = FeignConfig.class)
public interface ShopServiceClient {

    @GetMapping("/api/products")
    ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    // Magnum Opus: Explicit namngivning av resursens ID
    @GetMapping("/api/products/{productId}")
    ResponseEntity<ProductDto> getProduct(@PathVariable("productId") Long productId);

    @GetMapping("/api/categories")
    ResponseEntity<List<CategoryDto>> getAllCategories();

    @GetMapping("/api/orders/stats")
    ResponseEntity<Map<String, Object>> getOrderStats();
}