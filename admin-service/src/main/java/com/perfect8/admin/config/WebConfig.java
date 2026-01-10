package com.perfect8.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web-konfiguration för admin-service.
 * Anpassad för Spring Cloud Gateway och SOP-branschen.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Tillåt trafik från API Gateway (port 8080)
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://p8.rantila.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}