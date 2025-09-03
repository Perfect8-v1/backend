package com.perfect8.email.client;

import com.perfect8.email.dto.OrderDto;
import com.perfect8.email.dto.CustomerEmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for ShopServiceClient
 * Provides graceful degradation when shop-service is unavailable
 * Version 1.0
 */
@Slf4j
@Component
public class ShopServiceClientFallback implements ShopServiceClient {

    private static final String SERVICE_UNAVAILABLE = "Shop service is currently unavailable";

    @Override
    public OrderDto getOrderById(Long orderId, String token) {
        log.error("Fallback: Unable to fetch order with ID: {}. {}", orderId, SERVICE_UNAVAILABLE);
        return createEmptyOrder(orderId);
    }

    @Override
    public OrderDto getOrderByReference(String orderReference, String token) {
        log.error("Fallback: Unable to fetch order with reference: {}. {}", orderReference, SERVICE_UNAVAILABLE);
        return createEmptyOrderWithReference(orderReference);
    }

    @Override
    public List<OrderDto> getOrdersByCustomerId(Long customerId, String token) {
        log.error("Fallback: Unable to fetch orders for customer ID: {}. {}", customerId, SERVICE_UNAVAILABLE);
        return Collections.emptyList();
    }

    @Override
    public CustomerEmailDTO getCustomerById(Long customerId, String token) {
        log.error("Fallback: Unable to fetch customer with ID: {}. {}", customerId, SERVICE_UNAVAILABLE);
        return createEmptyCustomer(customerId);
    }

    @Override
    public CustomerEmailDTO getCustomerByEmail(String email, String token) {
        log.error("Fallback: Unable to fetch customer with email: {}. {}", email, SERVICE_UNAVAILABLE);
        return createEmptyCustomerWithEmail(email);
    }

    @Override
    public List<CustomerEmailDTO> getNewsletterSubscribers(String token) {
        log.error("Fallback: Unable to fetch newsletter subscribers. {}", SERVICE_UNAVAILABLE);
        return Collections.emptyList();
    }

    @Override
    public List<CustomerEmailDTO> getActiveCustomers(String token) {
        log.error("Fallback: Unable to fetch active customers. {}", SERVICE_UNAVAILABLE);
        return Collections.emptyList();
    }

    @Override
    public OrderDto.ShippingInfo getShippingInfo(Long orderId, String token) {
        log.error("Fallback: Unable to fetch shipping info for order ID: {}. {}", orderId, SERVICE_UNAVAILABLE);
        return createEmptyShippingInfo(orderId);
    }

    @Override
    public OrderDto.PaymentStatus getPaymentStatus(Long orderId, String token) {
        log.error("Fallback: Unable to fetch payment status for order ID: {}. {}", orderId, SERVICE_UNAVAILABLE);
        return createEmptyPaymentStatus(orderId);
    }

    // Helper methods to create empty/default objects

    private OrderDto createEmptyOrder(Long orderId) {
        return OrderDto.builder()
                .id(orderId)
                .status("UNKNOWN")
                .errorMessage(SERVICE_UNAVAILABLE)
                .build();
    }

    private OrderDto createEmptyOrderWithReference(String orderReference) {
        return OrderDto.builder()
                .orderReference(orderReference)
                .status("UNKNOWN")
                .errorMessage(SERVICE_UNAVAILABLE)
                .build();
    }

    private CustomerEmailDTO createEmptyCustomer(Long customerId) {
        return CustomerEmailDTO.builder()
                .id(customerId)
                .active(false)
                .errorMessage(SERVICE_UNAVAILABLE)
                .build();
    }

    private CustomerEmailDTO createEmptyCustomerWithEmail(String email) {
        return CustomerEmailDTO.builder()
                .email(email)
                .active(false)
                .errorMessage(SERVICE_UNAVAILABLE)
                .build();
    }

    private OrderDto.ShippingInfo createEmptyShippingInfo(Long orderId) {
        return OrderDto.ShippingInfo.builder()
                .orderId(orderId)
                .shippingStatus("UNKNOWN")
                .build();
    }

    private OrderDto.PaymentStatus createEmptyPaymentStatus(Long orderId) {
        return OrderDto.PaymentStatus.builder()
                .orderId(orderId)
                .status("UNKNOWN")
                .build();
    }
}