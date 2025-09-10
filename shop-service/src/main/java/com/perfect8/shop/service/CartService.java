package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.*;
import com.perfect8.shop.exception.*;
import com.perfect8.shop.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ShippingService shippingService;

    /**
     * Validate checkout - Core functionality for v1.0
     */
    public CheckoutValidationResponse validateCheckout(String customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        // Build response with correct structure
        CheckoutValidationResponse response = CheckoutValidationResponse.builder()
                .isValid(true)
                .issues(new ArrayList<>())
                .cartId(cart.getId())
                .customerId(customerIdLong)
                .itemCount(cart.getItems().size())
                .subtotal(cart.getTotalAmount())
                .total(cart.getTotalAmount())
                .currency("USD")
                .build();

        // Check if cart is empty
        if (cart.getItems().isEmpty()) {
            response.addIssue("Cart is empty");
            return response;
        }

        // Check inventory for each item
        CheckoutValidationResponse.InventoryValidation inventoryValidation =
                CheckoutValidationResponse.InventoryValidation.builder()
                        .isValid(true)
                        .outOfStockItems(new ArrayList<>())
                        .lowStockItems(new ArrayList<>())
                        .warnings(new ArrayList<>())
                        .build();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.getStockQuantity() == 0) {
                CheckoutValidationResponse.OutOfStockItem outOfStock =
                        CheckoutValidationResponse.OutOfStockItem.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .sku(product.getSku())
                                .requestedQuantity(item.getQuantity())
                                .availableQuantity(0)
                                .build();
                inventoryValidation.getOutOfStockItems().add(outOfStock);
                inventoryValidation.setValid(false);
                response.addIssue("Out of stock: " + product.getName());

            } else if (product.getStockQuantity() < item.getQuantity()) {
                CheckoutValidationResponse.LowStockItem lowStock =
                        CheckoutValidationResponse.LowStockItem.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .sku(product.getSku())
                                .requestedQuantity(item.getQuantity())
                                .availableQuantity(product.getStockQuantity())
                                .warning("Only " + product.getStockQuantity() + " available")
                                .build();
                inventoryValidation.getLowStockItems().add(lowStock);
                inventoryValidation.setValid(false);
                response.addIssue("Insufficient stock for " + product.getName() +
                        " (requested: " + item.getQuantity() +
                        ", available: " + product.getStockQuantity() + ")");
            }
        }

        response.setInventoryValidation(inventoryValidation);

        // Set final amounts
        response.setSubtotal(cart.getTotalAmount());
        response.setTotal(cart.getTotalAmount());
        response.setValidatedAt(LocalDateTime.now());

        return response;
    }

    /**
     * Get cart for customer - Core functionality
     */
    public CartResponse getCart(String customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElse(createNewCart(customer));

        return convertToCartResponse(cart);
    }

    /**
     * Add item to cart - Core functionality
     */
    public CartResponse addToCart(String customerId, AddToCartRequest request) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        Cart cart = cartRepository.findByCustomer(customer)
                .orElse(createNewCart(customer));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException("Total quantity exceeds available stock");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Update cart item quantity - Core functionality
     */
    public CartResponse updateCartItem(String customerId, UpdateCartItemRequest request) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (request.getQuantity() > item.getProduct().getStockQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product");
            }
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Remove item from cart - Core functionality
     */
    public CartResponse removeFromCart(String customerId, Long productId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Clear cart - Core functionality
     */
    public void clearCart(String customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setItemCount(0);
        cartRepository.save(cart);
    }

    /**
     * Get cart item count - Core functionality
     */
    public Integer getCartItemCount(String customerId) {
        if (customerId == null) {
            return 0;
        }

        try {
            Long customerIdLong = Long.parseLong(customerId);
            Customer customer = customerRepository.findById(customerIdLong)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

            Cart cart = cartRepository.findByCustomer(customer)
                    .orElse(null);

            return cart != null ? cart.getItemCount() : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Prepare checkout - Core functionality for v1.0
     */
    public CheckoutPreparationResponse prepareCheckout(String customerId, CheckoutPreparationRequest request) {
        log.info("Preparing checkout for customer: {}", customerId);

        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Long customerIdLong = Long.parseLong(customerId);
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        // Validate cart has items
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        // Convert cart items to response items
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        // Calculate amounts
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal shippingCost = BigDecimal.valueOf(9.99); // Default shipping
        BigDecimal taxRate = BigDecimal.valueOf(0.08); // 8% tax rate
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal totalAmount = subtotal.add(shippingCost).add(taxAmount);

        // Build checkout preparation response
        CheckoutPreparationResponse response = CheckoutPreparationResponse.builder()
                .cartId(cart.getId())
                .customerId(customerIdLong)
                .itemCount(cart.getItems().size())
                .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .items(items)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .taxRate(taxRate)
                .totalAmount(totalAmount)
                .currency("USD")
                .isValid(true)
                .preparedAt(LocalDateTime.now())
                .build();

        // Convert shipping address from request fields to AddressDTO
        if (request != null && request.getShippingStreetAddress() != null) {
            AddressDTO shippingAddress = AddressDTO.builder()
                    .streetAddress(request.getShippingStreetAddress())
                    .addressLine2(request.getShippingStreetAddress2())
                    .city(request.getShippingCity())
                    .state(request.getShippingState())
                    .postalCode(request.getShippingPostalCode())
                    .country(request.getShippingCountry())
                    .addressType("SHIPPING")
                    .build();
            response.setShippingAddress(shippingAddress);
        }

        // Add payment method if provided
        if (request != null && request.getPaymentMethod() != null) {
            response.setPaymentMethod(request.getPaymentMethod());
        }

        log.info("Checkout prepared successfully for customer: {}", customerId);
        return response;
    }

    /* ============================================
     * VERSION 2.0 FEATURES - Commented out for v1.0
     * ============================================
     * These features will be implemented in version 2.0:
     * - Coupon functionality
     * - Saved cart functionality
     */

    /*
    // Version 2.0: Apply coupon to cart
    public CartResponse applyCoupon(String customerId, ApplyCouponRequest request) {
        log.warn("applyCoupon - Feature planned for version 2.0");
        return getCart(customerId);
    }

    // Version 2.0: Remove coupon from cart
    public CartResponse removeCoupon(String customerId) {
        log.warn("removeCoupon - Feature planned for version 2.0");
        return getCart(customerId);
    }

    // Version 2.0: Save cart for later
    public SavedCartResponse saveCart(String customerId, SaveCartRequest request) {
        log.warn("saveCart - Feature planned for version 2.0");
        return SavedCartResponse.builder()
                .cartId(1L)
                .savedAt(LocalDateTime.now())
                .name(request != null ? request.getName() : "Saved Cart")
                .build();
    }

    // Version 2.0: Get saved carts
    public SavedCartResponse getSavedCarts(String customerId) {
        log.warn("getSavedCarts - Feature planned for version 2.0");
        return null;
    }
    */

    // ========== Helper methods ==========

    private Cart createNewCart(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setItemCount(0);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    private void updateCartTotals(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(total);
        cart.setItemCount(cart.getItems().size());
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomer().getCustomerId())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .itemCount(cart.getItems().size())
                .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem item) {
        Product product = item.getProduct();
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .imageUrl(product.getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .stockAvailable(product.getStockQuantity())
                .inStock(product.getStockQuantity() > 0)
                .build();
    }
}