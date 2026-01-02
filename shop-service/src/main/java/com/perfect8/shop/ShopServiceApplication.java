package com.perfect8.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Shop Service Application
 * 
 * Plain branch - scanBasePackages includes common for AuthProvider
 * 
 * @version 1.0-plain
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.perfect8.shop",
                "com.perfect8.common"
        },
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
public class ShopServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopServiceApplication.class, args);
        System.out.println("🛒 Perfect8 Shop Service started successfully!");
        System.out.println("📦 Products API: /api/products");
        System.out.println("🏷️ Categories API: /api/categories");
        System.out.println("📋 Health check: /actuator/health");
    }
}
