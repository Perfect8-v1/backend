package com.perfect8.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Blog Service Application
 * 
 * Plain branch - scanBasePackages includes common for AuthProvider
 * 
 * @version 1.0-plain
 */
@SpringBootApplication(scanBasePackages = {
        "com.perfect8.blog",
        "com.perfect8.common"
})
@EnableFeignClients
public class BlogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogServiceApplication.class, args);
    }
}
