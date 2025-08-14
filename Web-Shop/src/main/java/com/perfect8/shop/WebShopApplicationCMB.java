package com.perfect8.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class WebShopApplicationCMB {

    public static void main(String[] args) {
        SpringApplication.run(WebShopApplicationCMB.class, args);
        System.out.println("🛒 Perfect8 Web Shop Service Started on Port 8082");
    }
}