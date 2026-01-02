package com.perfect8.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Blog Service
 * 
 * Provides Swagger UI documentation at:
 * - Local: http://localhost:8082/swagger-ui.html
 * - Via Gateway: http://localhost:8080/blog/swagger-ui.html
 * 
 * @author Perfect8 Team
 * @version 1.0.0
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI blogServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Perfect8 Blog Service API")
                        .description("Blog post management, comments, and content publishing for Perfect8 platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Perfect8 Team")
                                .email("admin@perfect8.se")
                                .url("https://p8.rantila.com"))
                        .license(new License()
                                .name("Private")
                                .url("https://p8.rantila.com/license")))
                .servers(List.of(
                        new Server()
                                .url("https://p8.rantila.com")
                                .description("Production via API Gateway"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via API Gateway (local)")
                ));
    }
}
