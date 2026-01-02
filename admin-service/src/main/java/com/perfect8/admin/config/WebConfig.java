package com.perfect8.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for Spring MVC.
 *
 * Enables trailing slash matching for all endpoints.
 * Required for nginx compatibility where locations use trailing slashes.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Match both /api/auth/salt and /api/auth/salt/
        configurer.setUseTrailingSlashMatch(true);
    }
}
