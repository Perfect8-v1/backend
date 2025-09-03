package com.perfect8.email.client;

import com.perfect8.email.dto.OrderDto;
import com.perfect8.email.dto.CustomerEmailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

/**
 * Feign client for communication with Shop Service
 * Version 1.0 - Core order and customer operations only
 */
@FeignClient(
        name = "shop-service",
        url = "${shop.service.url:http://localhost:8082}",
        fallback = ShopServiceClientFallback.class
)
public interface ShopServiceClient {

    // Order endpoints
    @GetMapping("/api/orders/{orderId}")
    OrderDto getOrderById(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/orders/reference/{orderReference}")
    OrderDto getOrderByReference(
            @PathVariable("orderReference") String orderReference,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/orders/customer/{customerId}")
    List<OrderDto> getOrdersByCustomerId(
            @PathVariable("customerId") Long customerId,
            @RequestHeader("Authorization") String token
    );

    // Customer endpoints
    @GetMapping("/api/customers/{customerId}")
    CustomerEmailDTO getCustomerById(
            @PathVariable("customerId") Long customerId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/customers/email/{email}")
    CustomerEmailDTO getCustomerByEmail(
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String token
    );

    // Newsletter subscribers
    @GetMapping("/api/customers/newsletter/subscribers")
    List<CustomerEmailDTO> getNewsletterSubscribers(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/customers/active")
    List<CustomerEmailDTO> getActiveCustomers(
            @RequestHeader("Authorization") String token
    );

    // Order status checks (for email triggers)
    @GetMapping("/api/orders/{orderId}/shipping-info")
    OrderDto.ShippingInfo getShippingInfo(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/orders/{orderId}/payment-status")
    OrderDto.PaymentStatus getPaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String token
    );

    // Version 2.0 - Analytics endpoints (commented out for now)
    /*
    @GetMapping("/api/orders/analytics/abandoned-carts")
    List<OrderDto> getAbandonedCarts(
        @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/customers/analytics/engagement")
    List<CustomerEngagementDto> getCustomerEngagement(
        @RequestHeader("Authorization") String token
    );
    */
}