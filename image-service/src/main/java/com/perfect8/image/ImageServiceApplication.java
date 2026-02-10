package com.perfect8.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Image Service Application
 * Version 1.1 - With security auto-configuration excluded
 * 
 * FIXED: Excludes UserDetailsServiceAutoConfiguration to prevent
 * Spring Boot from generating a default password and overriding
 * our custom SecurityConfig with HeaderAuthenticationFilter.
 * 
 * See: Missforstand_Analys.md punkt 15
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class ImageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageServiceApplication.class, args);
    }
}
