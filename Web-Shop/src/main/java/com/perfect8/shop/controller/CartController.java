package com.perfect8.shop.controller;

import com.perfect8.shop.service.CartService;
import com.perfect8.shop.service.ShippingService;
import com.perfect8.shop.dto.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Shopping Cart management
 *
 * Cart Endpoints:
 * GET    /api/shop/cart                     - Get current cart
 * POST   /api/shop/cart/items               - Add item to cart
 * PUT    /api/shop/cart/items/{itemId}      - Update cart item quantity
 * DELETE /api/shop/cart/items/{itemId}      - Remove item from cart
 * DELETE /api/shop/cart                     - Clear entire cart
 *
 * Calculation Endpoints:
 * POST   /api/shop/cart/calculate           - Calculate cart totals
 * POST   /api/shop/cart/shipping-options    - Get shipping options
 * POST   /api/shop/cart/apply-coupon        - Apply discount coupon
 * DELETE /api/shop/cart/coupon              - Remove applied coupon
 *
 * Checkout Endpoints:
 * POST   /api/shop/cart/checkout/validate   - Validate cart for checkout
 * POST   /api/shop/cart/checkout/prepare    - Prepare checkout (reserve stock)
 * POST   /api/shop/cart/save-for-later      - Save cart for later
 * POST   /api/shop/cart/restore             - Restore saved cart
 */
@RestController
@RequestMapping("/api/shop/cart")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class CartController {

    private final CartService cartService;
    private final ShippingService shippingService;

    @Autowired
    public CartController(CartService cartService, ShippingService shippingService) {
        this.cartService = cartService;
        this.shippingService = shippingService;
    }

    // ============================================================================
    // CART MANAGEMENT ENDPOINTS
    // ============================================================================

    /**
     * Get current cart for authenticated customer
     *
     * @param authentication Customer authentication
     * @return Current cart with all items and totals
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartResponse cart = cartService.getCart(customerEmail);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add item to cart
     *
     * @param request Add to cart request
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.addItemToCart(customerEmail, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Update cart item quantity
     *
     * @param itemId Cart item ID
     * @param request Quantity update request
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.updateCartItem(customerEmail, itemId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove item from cart
     *
     * @param itemId Cart item ID
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long itemId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.removeItemFromCart(customerEmail, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clear entire cart
     *
     * @param authentication Customer authentication
     * @return Empty cart response
     */
    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartResponse cart = cartService.clearCart(customerEmail);
        return ResponseEntity.ok(cart);
    }

    /**
     * Move item to wishlist and remove from cart
     *
     * @param itemId Cart item ID
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @PostMapping("/items/{itemId}/move-to-wishlist")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> moveItemToWishlist(
            @PathVariable Long itemId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.moveItemToWishlist(customerEmail, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Save item for later (remove from active cart but keep saved)
     *
     * @param itemId Cart item ID
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @PostMapping("/items/{itemId}/save-for-later")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> saveItemForLater(
            @PathVariable Long itemId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.saveItemForLater(customerEmail, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Move saved item back to active cart
     *
     * @param itemId Saved item ID
     * @param authentication Customer authentication
     * @return Updated cart
     */
    @PostMapping("/saved-items/{itemId}/move-to-cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> moveSavedItemToCart(
            @PathVariable Long itemId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.moveSavedItemToCart(customerEmail, itemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Get saved items (saved for later)
     *
     * @param authentication Customer authentication
     * @return List of saved items
     */
    @GetMapping("/saved-items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CartItemResponse>> getSavedItems(Authentication authentication) {
        String customerEmail = authentication.getName();
        List<CartItemResponse> savedItems = cartService.getSavedItems(customerEmail);
        return ResponseEntity.ok(savedItems);
    }

    // ============================================================================
    // CALCULATION ENDPOINTS
    // ============================================================================

    /**
     * Calculate cart totals with current items
     *
     * @param authentication Customer authentication
     * @return Cart calculation details
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartCalculationResponse> calculateCart(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartCalculationResponse calculation = cartService.calculateCart(customerEmail);
        return ResponseEntity.ok(calculation);
    }

    /**
     * Get available shipping options for cart
     *
     * @param request Shipping calculation request with address
     * @param authentication Customer authentication
     * @return Available shipping options with costs
     */
    @PostMapping("/shipping-options")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ShippingOptionResponse>> getShippingOptions(
            @Valid @RequestBody ShippingCalculationRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        List<ShippingOptionResponse> options = shippingService.getShippingOptions(customerEmail, request);
        return ResponseEntity.ok(options);
    }

    /**
     * Apply discount coupon to cart
     *
     * @param request Coupon application request
     * @param authentication Customer authentication
     * @return Updated cart with applied discount
     */
    @PostMapping("/apply-coupon")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.applyCoupon(customerEmail, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove applied coupon from cart
     *
     * @param authentication Customer authentication
     * @return Updated cart without discount
     */
    @DeleteMapping("/coupon")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> removeCoupon(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartResponse cart = cartService.removeCoupon(customerEmail);
        return ResponseEntity.ok(cart);
    }

    /**
     * Validate coupon code (without applying)
     *
     * @param couponCode Coupon code to validate
     * @param authentication Customer authentication
     * @return Coupon validation result
     */
    @GetMapping("/validate-coupon/{couponCode}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @PathVariable String couponCode,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CouponValidationResponse validation = cartService.validateCoupon(customerEmail, couponCode);
        return ResponseEntity.ok(validation);
    }

    // ============================================================================
    // CHECKOUT PREPARATION ENDPOINTS
    // ============================================================================

    /**
     * Validate cart for checkout (stock availability, pricing, etc.)
     *
     * @param authentication Customer authentication
     * @return Checkout validation result
     */
    @PostMapping("/checkout/validate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CheckoutValidationResponse> validateCheckout(Authentication authentication) {
        String customerEmail = authentication.getName();
        CheckoutValidationResponse validation = cartService.validateCheckout(customerEmail);
        return ResponseEntity.ok(validation);
    }

    /**
     * Prepare cart for checkout (reserve stock, lock prices)
     *
     * @param request Checkout preparation request
     * @param authentication Customer authentication
     * @return Checkout preparation result with reservation token
     */
    @PostMapping("/checkout/prepare")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CheckoutPreparationResponse> prepareCheckout(
            @Valid @RequestBody CheckoutPreparationRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CheckoutPreparationResponse preparation = cartService.prepareCheckout(customerEmail, request);
        return ResponseEntity.ok(preparation);
    }

    /**
     * Estimate taxes for cart based on shipping address
     *
     * @param request Tax calculation request
     * @param authentication Customer authentication
     * @return Tax estimation
     */
    @PostMapping("/estimate-tax")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaxEstimationResponse> estimateTax(
            @Valid @RequestBody TaxCalculationRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        TaxEstimationResponse estimation = cartService.estimateTax(customerEmail, request);
        return ResponseEntity.ok(estimation);
    }

    // ============================================================================
    // CART PERSISTENCE ENDPOINTS
    // ============================================================================

    /**
     * Save current cart state for later
     *
     * @param request Save cart request with optional name
     * @param authentication Customer authentication
     * @return Success response
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> saveCart(
            @Valid @RequestBody SaveCartRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        cartService.saveCartForLater(customerEmail, request);
        return ResponseEntity.ok(Map.of("message", "Cart saved successfully"));
    }

    /**
     * Get list of saved carts
     *
     * @param authentication Customer authentication
     * @return List of saved carts
     */
    @GetMapping("/saved")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<SavedCartResponse>> getSavedCarts(Authentication authentication) {
        String customerEmail = authentication.getName();
        List<SavedCartResponse> savedCarts = cartService.getSavedCarts(customerEmail);
        return ResponseEntity.ok(savedCarts);
    }

    /**
     * Restore a saved cart
     *
     * @param savedCartId Saved cart ID
     * @param authentication Customer authentication
     * @return Restored cart
     */
    @PostMapping("/restore/{savedCartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> restoreCart(
            @PathVariable Long savedCartId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        CartResponse cart = cartService.restoreCart(customerEmail, savedCartId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Delete a saved cart
     *
     * @param savedCartId Saved cart ID
     * @param authentication Customer authentication
     * @return Success response
     */
    @DeleteMapping("/saved/{savedCartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> deleteSavedCart(
            @PathVariable Long savedCartId,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        cartService.deleteSavedCart(customerEmail, savedCartId);
        return ResponseEntity.ok(Map.of("message", "Saved cart deleted successfully"));
    }

    // ============================================================================
    // CART ANALYTICS ENDPOINTS
    // ============================================================================

    /**
     * Get cart abandonment data for customer
     *
     * @param authentication Customer authentication
     * @return Cart history and abandonment data
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartHistoryResponse> getCartHistory(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartHistoryResponse history = cartService.getCartHistory(customerEmail);
        return ResponseEntity.ok(history);
    }

    /**
     * Get recommended products based on cart contents
     *
     * @param authentication Customer authentication
     * @param limit Maximum number of recommendations
     * @return Product recommendations
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ProductRecommendationResponse>> getCartRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "6") Integer limit) {

        String customerEmail = authentication.getName();
        List<ProductRecommendationResponse> recommendations = cartService.getCartRecommendations(
                customerEmail, limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get items frequently bought together with cart items
     *
     * @param authentication Customer authentication
     * @param limit Maximum number of suggestions
     * @return Frequently bought together suggestions
     */
    @GetMapping("/frequently-bought-together")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ProductRecommendationResponse>> getFrequentlyBoughtTogether(
            Authentication authentication,
            @RequestParam(defaultValue = "4") Integer limit) {

        String customerEmail = authentication.getName();
        List<ProductRecommendationResponse> suggestions = cartService.getFrequentlyBoughtTogether(
                customerEmail, limit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get cart summary for quick view
     *
     * @param authentication Customer authentication
     * @return Cart summary with item count and total
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartSummaryResponse> getCartSummary(Authentication authentication) {
        String customerEmail = authentication.getName();
        CartSummaryResponse summary = cartService.getCartSummary(customerEmail);
        return ResponseEntity.ok(summary);
    }

    // DTO Classes for responses
    public static class ProductRecommendationResponse {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private String imageUrl;
        private String recommendationReason;
        private Double confidence;

        // Constructors, getters, setters
        public ProductRecommendationResponse() {}

        public ProductRecommendationResponse(Long productId, String productName, BigDecimal price,
                                             String imageUrl, String recommendationReason, Double confidence) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.imageUrl = imageUrl;
            this.recommendationReason = recommendationReason;
            this.confidence = confidence;
        }

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getRecommendationReason() { return recommendationReason; }
        public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }

    public static class CartSummaryResponse {
        private Integer itemCount;
        private BigDecimal subtotal;
        private BigDecimal estimatedTotal;
        private Boolean hasDiscounts;
        private String currencyCode;

        // Constructors, getters, setters
        public CartSummaryResponse() {}

        public CartSummaryResponse(Integer itemCount, BigDecimal subtotal, BigDecimal estimatedTotal,
                                   Boolean hasDiscounts, String currencyCode) {
            this.itemCount = itemCount;
            this.subtotal = subtotal;
            this.estimatedTotal = estimatedTotal;
            this.hasDiscounts = hasDiscounts;
            this.currencyCode = currencyCode;
        }

        // Getters and setters
        public Integer getItemCount() { return itemCount; }
        public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getEstimatedTotal() { return estimatedTotal; }
        public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

        public Boolean getHasDiscounts() { return hasDiscounts; }
        public void setHasDiscounts(Boolean hasDiscounts) { this.hasDiscounts = hasDiscounts; }

        public String getCurrencyCode() { return currencyCode; }
        public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    }
}