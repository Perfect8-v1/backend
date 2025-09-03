package com.perfect8.shop.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * REST Template configuration for HTTP client operations.
 * Version 1.0 - Simple and reliable configuration
 *
 * This uses Spring Boot's built-in RestTemplateBuilder
 * No external dependencies needed beyond Spring Boot Web
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Default RestTemplate for general use
     * - Connect timeout: 10 seconds
     * - Read timeout: 30 seconds
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}