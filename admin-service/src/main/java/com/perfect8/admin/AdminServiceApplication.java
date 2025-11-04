package com.perfect8.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients
@EnableMethodSecurity(prePostEnabled = true)
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
        System.out.println("🚀 Perfect8 Admin Service started successfully on port 8083!");
        System.out.println("📊 Admin Dashboard: http://localhost:8083/api/admin/dashboard/stats");
        System.out.println("🔐 Login endpoint: http://localhost:8083/api/auth/login");
        System.out.println("📋 Health check: http://localhost:8083/actuator/health");
    }
}