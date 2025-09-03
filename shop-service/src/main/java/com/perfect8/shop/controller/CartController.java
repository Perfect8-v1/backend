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
}