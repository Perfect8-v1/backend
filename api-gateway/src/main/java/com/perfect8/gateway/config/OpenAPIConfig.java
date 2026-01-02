package com.perfect8.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI Configuration for API Gateway
 * 
 * Aggregates OpenAPI documentation from all microservices into a single Swagger UI.
 * 
 * Access Swagger UI at:
 * - Local: http://localhost:8080/swagger-ui.html
 * - Production: https://p8.rantila.com/swagger-ui.html
 * 
 * Individual service documentation:
 * - Admin: http://localhost:8080/api/v1/admin/v3/api-docs
 * - Blog: http://localhost:8080/api/v1/posts/v3/api-docs
 * - Email: http://localhost:8080/api/v1/email/v3/api-docs
 * - Image: http://localhost:8080/api/v1/images/v3/api-docs
 * - Shop: http://localhost:8080/api/v1/products/v3/api-docs
 * 
 * @author Perfect8 Team
 * @version 1.0.0
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Admin Service OpenAPI Group
     */
    @Bean
    public GroupedOpenApi adminServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("1. Admin Service")
                .pathsToMatch("/api/auth/**", "/api/admin/**")
                .build();
    }

    /**
     * Blog Service OpenAPI Group
     */
    @Bean
    public GroupedOpenApi blogServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("2. Blog Service")
                .pathsToMatch("/api/posts/**")
                .build();
    }

    /**
     * Email Service OpenAPI Group
     */
    @Bean
    public GroupedOpenApi emailServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("3. Email Service")
                .pathsToMatch("/api/email/**")
                .build();
    }

    /**
     * Image Service OpenAPI Group
     */
    @Bean
    public GroupedOpenApi imageServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("4. Image Service")
                .pathsToMatch("/api/images/**")
                .build();
    }

    /**
     * Shop Service OpenAPI Group
     */
    @Bean
    public GroupedOpenApi shopServiceOpenAPI() {
        return GroupedOpenApi.builder()
                .group("5. Shop Service")
                .pathsToMatch(
                        "/api/products/**",
                        "/api/categories/**",
                        "/api/cart/**",
                        "/api/orders/**",
                        "/api/customers/**",
                        "/api/payments/**"
                )
                .build();
    }

    /**
     * All APIs - Complete overview
     */
    @Bean
    public GroupedOpenApi allAPIs() {
        return GroupedOpenApi.builder()
                .group("0. All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}
