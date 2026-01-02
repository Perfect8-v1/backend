package com.perfect8.email.client;

import com.perfect8.email.dto.OrderDto;
import com.perfect8.email.dto.CustomerEmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;

/**
 * Client for communication with Shop Service using RestTemplate
 * Version 1.0 - Core order and customer operations only
 */
@Slf4j
@Service
public class ShopServiceClient {

    private final RestTemplate restTemplate;

    @Value("${shop.service.url:http://localhost:8082}")
    private String shopServiceUrl;

    public ShopServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Order endpoints
    public OrderDto getOrderById(Long orderId, String token) {
        try {
            String url = shopServiceUrl + "/api/orders/" + orderId;
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<OrderDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch order with ID: {}", orderId, e);
            return null;
        }
    }

    public OrderDto getOrderByReference(String orderReference, String token) {
        try {
            String url = shopServiceUrl + "/api/orders/reference/" + orderReference;
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<OrderDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch order with reference: {}", orderReference, e);
            return null;
        }
    }

    public List<OrderDto> getOrdersByCustomerId(Long customerId, String token) {
        try {
            String url = shopServiceUrl + "/api/orders/customer/" + customerId;
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<List<OrderDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<OrderDto>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch orders for customer ID: {}", customerId, e);
            return Collections.emptyList();
        }
    }

    // Customer endpoints
    public CustomerEmailDTO getCustomerById(Long customerId, String token) {
        try {
            String url = shopServiceUrl + "/api/customers/" + customerId;
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerEmailDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CustomerEmailDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch customer with ID: {}", customerId, e);
            return null;
        }
    }

    public CustomerEmailDTO getCustomerByEmail(String email, String token) {
        try {
            String url = shopServiceUrl + "/api/customers/email/" + email;
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerEmailDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CustomerEmailDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch customer with email: {}", email, e);
            return null;
        }
    }

    // Newsletter subscribers
    public List<CustomerEmailDTO> getNewsletterSubscribers(String token) {
        try {
            String url = shopServiceUrl + "/api/customers/newsletter/subscribers";
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<List<CustomerEmailDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<CustomerEmailDTO>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch newsletter subscribers", e);
            return Collections.emptyList();
        }
    }

    public List<CustomerEmailDTO> getActiveCustomers(String token) {
        try {
            String url = shopServiceUrl + "/api/customers/active";
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<List<CustomerEmailDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<CustomerEmailDTO>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch active customers", e);
            return Collections.emptyList();
        }
    }

    // Order status checks (for email triggers)
    public OrderDto.ShippingInfo getShippingInfo(Long orderId, String token) {
        try {
            String url = shopServiceUrl + "/api/orders/" + orderId + "/shipping-info";
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<OrderDto.ShippingInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto.ShippingInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch shipping info for order ID: {}", orderId, e);
            return null;
        }
    }

    public OrderDto.PaymentStatus getPaymentStatus(Long orderId, String token) {
        try {
            String url = shopServiceUrl + "/api/orders/" + orderId + "/payment-status";
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<OrderDto.PaymentStatus> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OrderDto.PaymentStatus.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch payment status for order ID: {}", orderId, e);
            return null;
        }
    }

    // Helper method to create headers with Authorization token
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", token);
        }
        return headers;
    }

    // Version 2.0 - Analytics endpoints (commented out for now)
    /*
    public List<OrderDto> getAbandonedCarts(String token) {
        // Implementation for v2.0
    }

    public List<CustomerEngagementDto> getCustomerEngagement(String token) {
        // Implementation for v2.0
    }
    */
}