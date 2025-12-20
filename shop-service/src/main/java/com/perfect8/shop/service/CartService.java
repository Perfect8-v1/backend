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

/**
 * Cart Service - Version 1.0
 * Core shopping cart functionality
 *
 * Magnum Opus Principles:
 * - Readable variable names (customerId not customerEmailDTOId, not customerIdLong)
 * - NO backward compatibility - built right from start
 * - NO alias methods - one method, one name
 * - Clean variable names without redundant suffixes
 */
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
    public CheckoutValidationResponse validateCheckout(Long customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        // Build response with correct structure
        CheckoutValidationResponse response = CheckoutValidationResponse.builder()
                .isValid(true)
                .issues(new ArrayList<>())
                .cartId(cart.getCartId())
                .customerId(customerId)
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

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() == 0) {
                CheckoutValidationResponse.OutOfStockItem outOfStockItem =
                        CheckoutValidationResponse.OutOfStockItem.builder()
                                .productId(product.getProductId())
                                .productName(product.getName())
                                .sku(product.getSku())
                                .requestedQuantity(cartItem.getQuantity())
                                .availableQuantity(0)
                                .build();
                inventoryValidation.getOutOfStockItems().add(outOfStockItem);
                inventoryValidation.setValid(false);
                response.addIssue("Out of stock: " + product.getName());

            } else if (product.getStockQuantity() < cartItem.getQuantity()) {
                CheckoutValidationResponse.LowStockItem lowStockItem =
                        CheckoutValidationResponse.LowStockItem.builder()
                                .productId(product.getProductId())
                                .productName(product.getName())
                                .sku(product.getSku())
                                .requestedQuantity(cartItem.getQuantity())
                                .availableQuantity(product.getStockQuantity())
                                .warning("Only " + product.getStockQuantity() + " available")
                                .build();
                inventoryValidation.getLowStockItems().add(lowStockItem);
                inventoryValidation.setValid(false);
                response.addIssue("Insufficient stock for " + product.getName() +
                        " (requested: " + cartItem.getQuantity() +
                        ", available: " + product.getStockQuantity() + ")");
            }
        }

        response.setInventoryValidation(inventoryValidation);

        // Set final amounts
        response.setSubtotal(cart.getTotalAmount());
        response.setTotal(cart.getTotalAmount());
        response.setValidatedDate(LocalDateTime.now());

        return response;
    }

    /**
     * Get cart for customer - Core functionality
     */
    public CartResponse getCart(Long customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> createNewCart(customer));

        return convertToCartResponse(cart);
    }

    /**
     * Add item to cart - Core functionality
     */
    public CartResponse addToCart(Long customerId, AddToCartRequest request) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with customerEmailDTOId: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> createNewCart(customer));

        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            Integer newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException("Total quantity exceeds available stock");
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(request.getQuantity());
            newCartItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(newCartItem);
            cart.getItems().add(newCartItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Update cart item quantity - Core functionality
     */
    public CartResponse updateCartItem(Long customerId, UpdateCartItemRequest request) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        CartItem cartItem = cart.getItems().stream()
                .filter(currentCartItem -> currentCartItem.getProduct().getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            if (request.getQuantity() > cartItem.getProduct().getStockQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product");
            }
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Remove item from cart - Core functionality
     */
    public CartResponse removeFromCart(Long customerId, Long productId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        CartItem cartItemToRemove = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cart.getItems().remove(cartItemToRemove);
        cartItemRepository.delete(cartItemToRemove);

        updateCartTotals(cart);
        cartRepository.save(cart);

        return convertToCartResponse(cart);
    }

    /**
     * Clear cart - Core functionality
     */
    public void clearCart(Long customerId) {
        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

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
    public Integer getCartItemCount(Long customerId) {
        if (customerId == null) {
            return 0;
        }

        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

            Cart cart = cartRepository.findByCustomer(customer)
                    .orElse(null);

            return cart != null ? cart.getItemCount() : 0;
        } catch (Exception e) {
            log.error("Error getting cart item count for customer {}: {}", customerId, e.getMessage());
            return 0;
        }
    }

    /**
     * Prepare checkout - Core functionality for v1.0
     */
    public CheckoutPreparationResponse prepareCheckout(Long customerId, CheckoutPreparationRequest request) {
        log.info("Preparing checkout for customer: {}", customerId);

        if (customerId == null) {
            throw new CustomerNotFoundException("Customer ID cannot be null");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with customerEmailDTOId: " + customerId));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for customer"));

        // Validate cart has items
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        // Convert cart items to response items
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        // Calculate amounts
        BigDecimal subtotalAmount = cart.getTotalAmount();
        BigDecimal shippingCostAmount = BigDecimal.valueOf(9.99); // Default shipping
        BigDecimal taxRate = BigDecimal.valueOf(0.08); // 8% tax rate
        BigDecimal taxAmount = subtotalAmount.multiply(taxRate);
        BigDecimal totalAmount = subtotalAmount.add(shippingCostAmount).add(taxAmount);

        // Build checkout preparation response
        CheckoutPreparationResponse response = CheckoutPreparationResponse.builder()
                .cartId(cart.getCartId())
                .customerId(customerId)
                .itemCount(cart.getItems().size())
                .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .items(cartItemResponses)
                .subtotal(subtotalAmount)
                .shippingCost(shippingCostAmount)
                .taxAmount(taxAmount)
                .taxRate(taxRate)
                .totalAmount(totalAmount)
                .currency("USD")
                .isValid(true)
                .preparedDate(LocalDateTime.now())
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

    // ========== Helper methods ==========

    private Cart createNewCart(Customer customer) {
        Cart newCart = new Cart();
        newCart.setCustomer(customer);
        newCart.setTotalAmount(BigDecimal.ZERO);
        newCart.setItemCount(0);
        newCart.setCreatedDate(LocalDateTime.now());
        newCart.setUpdatedDate(LocalDateTime.now());
        return cartRepository.save(newCart);
    }

    private void updateCartTotals(Cart cart) {
        BigDecimal totalAmount = cart.getItems().stream()
                .map(cartItem -> cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        cart.setItemCount(cart.getItems().size());
        cart.setUpdatedDate(LocalDateTime.now());
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomer().getCustomerId())
                .items(cartItemResponses)
                .totalAmount(cart.getTotalAmount())
                .itemCount(cart.getItems().size())
                .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .createdDate(cart.getCreatedDate())
                .updatedDate(cart.getUpdatedDate())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .productSku(product.getSku())
                .imageUrl(product.getImageUrl())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .stockAvailable(product.getStockQuantity())
                .inStock(product.getStockQuantity() > 0)
                .build();
    }
}