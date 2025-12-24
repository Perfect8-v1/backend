package com.perfect8.image.config;

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
        // Match both /images/original/file.jpg and /images/original/file.jpg/
        configurer.setUseTrailingSlashMatch(true);
    }
}
