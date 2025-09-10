package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.service.CartService;
import com.perfect8.shop.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final ShippingService shippingService;

    /**
     * Add item to cart - Core functionality
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.addToCart(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Item added to cart successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Update cart item quantity - Core functionality
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.updateCartItem(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart item updated successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Remove item from cart - Core functionality
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long productId,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.removeFromCart(customerId, productId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Item removed from cart successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Get cart - Core functionality
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.getCart(customerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart retrieved successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Clear cart - Core functionality
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            cartService.clearCart(customerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart cleared successfully",
                    (Void) null,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Get cart item count - Core functionality
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            Integer count = cartService.getCartItemCount(customerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart item count retrieved successfully",
                    count,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Get shipping options - Core functionality for checkout
     */
    @GetMapping("/shipping-options")
    public ResponseEntity<ApiResponse<ShippingOptionResponse>> getShippingOptions(
            @RequestParam String address,
            @Valid @RequestBody ShippingCalculationRequest request,
            Principal principal) {
        try {
            ShippingOptionResponse response = shippingService.getShippingOptions(address, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Shipping options retrieved successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Calculate tax - Core functionality for checkout
     */
    @PostMapping("/calculate-tax")
    public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculateTax(
            @Valid @RequestBody TaxCalculationRequest request,
            Principal principal) {
        try {
            TaxCalculationResponse response = shippingService.calculateTax(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Tax calculated successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Validate checkout - Core functionality
     */
    @PostMapping("/validate-checkout")
    public ResponseEntity<ApiResponse<CheckoutValidationResponse>> validateCheckout(
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CheckoutValidationResponse response = cartService.validateCheckout(customerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Checkout validation completed",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Prepare checkout - Core functionality
     */
    @PostMapping("/prepare-checkout")
    public ResponseEntity<ApiResponse<CheckoutPreparationResponse>> prepareCheckout(
            @Valid @RequestBody CheckoutPreparationRequest request,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CheckoutPreparationResponse response = cartService.prepareCheckout(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Checkout preparation completed",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /* ============================================
     * VERSION 2.0 ENDPOINTS - Commented out for v1.0
     * ============================================
     * These endpoints will be implemented in version 2.0:
     * - Coupon functionality
     * - Saved cart functionality
     */

    /*
    // Version 2.0: Apply coupon to cart
    @PostMapping("/apply-coupon")
    public ResponseEntity<ApiResponse<CartResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.applyCoupon(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Coupon applied successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    // Version 2.0: Remove coupon from cart
    @DeleteMapping("/remove-coupon")
    public ResponseEntity<ApiResponse<CartResponse>> removeCoupon(Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            CartResponse response = cartService.removeCoupon(customerId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Coupon removed successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    // Version 2.0: Save cart for later
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<SavedCartResponse>> saveCart(
            @Valid @RequestBody SaveCartRequest request,
            Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            SavedCartResponse response = cartService.saveCart(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart saved successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    // Version 2.0: Get saved carts
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<SavedCartResponse>> getSavedCarts(Principal principal) {
        try {
            String customerId = principal != null ? principal.getName() : null;
            SavedCartResponse response = cartService.getSavedCarts(customerId);
            if (response != null) {
                return ResponseEntity.ok(new ApiResponse<>(
                        "Saved carts retrieved successfully",
                        response,
                        true
                ));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(
                        "No saved carts found",
                        (SavedCartResponse) null,
                        true
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }
    */
}