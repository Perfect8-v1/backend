package com.perfect8.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopServiceApplication.class, args);
        System.out.println("🛒 Perfect8 Shop Service started successfully on port 8082!");
        System.out.println("📦 Products API: http://localhost:8082/api/products");
        System.out.println("🏷️ Categories API: http://localhost:8082/api/categories");
        System.out.println("🛍️ Orders API: http://localhost:8082/api/orders");
        System.out.println("📋 Health check: http://localhost:8082/actuator/health");
        System.out.println("🗄️ H2 Console: http://localhost:8082/h2-console");
    }
}