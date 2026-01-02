package com.perfect8.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Image Service Application
 * 
 * Plain branch - scanBasePackages includes common for AuthProvider
 * 
 * @version 1.0-plain
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.perfect8.image",
                "com.perfect8.common"
        },
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
public class ImageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageServiceApplication.class, args);
    }
}
