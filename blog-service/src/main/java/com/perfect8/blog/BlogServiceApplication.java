package com.perfect8.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Blog Service Application
 * Version 1.1 - With security auto-configuration excluded
 * 
 * FIXED: Excludes UserDetailsServiceAutoConfiguration to prevent
 * Spring Boot from generating a default password and overriding
 * our custom SecurityConfig with HeaderAuthenticationFilter.
 * 
 * See: Missforstand_Analys.md punkt 15
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.perfect8.blog",
                "com.perfect8.common"
        },
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@EnableFeignClients
public class BlogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogServiceApplication.class, args);
    }
}
