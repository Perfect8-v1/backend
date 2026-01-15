package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.service.ShippingService;
import com.perfect8.shop.service.CartService;
import com.perfect8.shop.service.CustomerService;
import com.perfect8.shop.entity.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;

/**
 * Cart Controller - Version 1.0
 * FIXED: Using Long customerId instead of String!
 * CORS hanteras globalt av WebConfig
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final ShippingService shippingService;
    private final CustomerService customerService;  // ADDED: To convert email to customerId

    /**
     * Add item to cart - Core functionality
     * FIXED: Converting email to customerId
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.addToCart(customerIdLong, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Item added to cart successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Update cart item quantity - Core functionality
     * FIXED: Converting email to customerId
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.updateCartItem(customerIdLong, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart item updated successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Remove item from cart - Core functionality
     * FIXED: Converting email to customerId
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long productId,
            Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.removeFromCart(customerIdLong, productId);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Item removed from cart successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error removing from cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Get cart - Core functionality
     * FIXED: Converting email to customerId
     */
    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.getCart(customerIdLong);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart retrieved successfully",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error retrieving cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Clear cart - Core functionality
     * FIXED: Converting email to customerId
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            cartService.clearCart(customerIdLong);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart cleared successfully",
                    null,
                    true
            ));
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Get cart item count - Core functionality
     * FIXED: Converting email to customerId
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            Integer count = cartService.getCartItemCount(customerIdLong);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Cart item count retrieved successfully",
                    count,
                    true
            ));
        } catch (Exception e) {
            log.error("Error getting cart count: {}", e.getMessage());
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
            log.error("Error getting shipping options: {}", e.getMessage());
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
            log.error("Error calculating tax: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Validate checkout - Core functionality
     * FIXED: Converting email to customerId
     */
    @PostMapping("/validate-checkout")
    public ResponseEntity<ApiResponse<CheckoutValidationResponse>> validateCheckout(
            Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CheckoutValidationResponse response = cartService.validateCheckout(customerIdLong);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Checkout validation completed",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error validating checkout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Prepare checkout - Core functionality
     * FIXED: Converting email to customerId
     */
    @PostMapping("/prepare-checkout")
    public ResponseEntity<ApiResponse<CheckoutPreparationResponse>> prepareCheckout(
            @Valid @RequestBody CheckoutPreparationRequest request,
            Principal principal) {
        try {
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CheckoutPreparationResponse response = cartService.prepareCheckout(customerIdLong, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    "Checkout preparation completed",
                    response,
                    true
            ));
        } catch (Exception e) {
            log.error("Error preparing checkout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    Collections.singletonList(e.getMessage()),
                    false
            ));
        }
    }

    /**
     * Helper method to get customerId from Principal
     * FIXED: Converting email to customerId properly
     */
    private Long getCustomerIdFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        // Principal.getName() returns the email
        String customerEmail = principal.getName();
        if (customerEmail == null || customerEmail.isEmpty()) {
            return null;
        }

        // Get customer by email and return the customerId
        Customer customer = customerService.getCustomerByEmail(customerEmail);
        if (customer == null) {
            log.warn("No customer found for email: {}", customerEmail);
            return null;
        }

        return customer.getCustomerId();
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
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.applyCoupon(customerIdLong, request);
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
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            CartResponse response = cartService.removeCoupon(customerIdLong);
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
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            SavedCartResponse response = cartService.saveCart(customerIdLong, request);
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
            Long customerIdLong = getCustomerIdFromPrincipal(principal);
            SavedCartResponse response = cartService.getSavedCarts(customerIdLong);
            if (response != null) {
                return ResponseEntity.ok(new ApiResponse<>(
                        "Saved carts retrieved successfully",
                        response,
                        true
                ));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(
                        "No saved carts found",
                        null,
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
