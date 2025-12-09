package com.perfect8.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Email Service Application
 * 
 * Plain branch - scanBasePackages includes common for AuthProvider
 * 
 * @version 1.0-plain
 */
@SpringBootApplication(scanBasePackages = {
        "com.perfect8.email",
        "com.perfect8.common"
})
@EnableAsync
public class EmailServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }
}
