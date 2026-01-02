package com.perfect8.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Perfect8 API Gateway Application
 *
 * Spring Cloud Gateway for routing requests to microservices:
 * - admin-service (port 8081)
 * - blog-service (port 8082)
 * - email-service (port 8083)
 * - image-service (port 8084)
 * - shop-service (port 8085)
 *
 * Gateway provides:
 * - Centralized JWT authentication
 * - Request routing
 * - CORS handling
 * - Rate limiting (future)
 * - Circuit breaker (future)
 *
 * @author Perfect8 Team
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}