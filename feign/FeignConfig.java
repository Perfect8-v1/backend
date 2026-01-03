package com.perfect8.feign;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Kopiera säkerhets-headers från inkommande till utgående anrop
                String userId = request.getHeader("X-User-Id");
                String userEmail = request.getHeader("X-Auth-User");
                String userRoles = request.getHeader("X-User-Roles");

                if (userId != null) requestTemplate.header("X-User-Id", userId);
                if (userEmail != null) requestTemplate.header("X-Auth-User", userEmail);
                if (userRoles != null) requestTemplate.header("X-User-Roles", userRoles);
            }
        };
    }
}