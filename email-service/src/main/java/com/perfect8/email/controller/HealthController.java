package com.perfect8.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * KOPIERA DENNA FIL TILL VARJE SERVICE OCH ÄNDRA:
 * 1. package namn (SERVICE_NAME → blog, shop, email, image)
 * 2. @Value("${server.port}") kommer automatiskt få rätt port
 * 3. service-name i responses
 */
@RestController
@RequestMapping("/health")
public class HealthController implements HealthIndicator {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * Enkel health check för Uptime Kuma
     * URL: http://localhost:PORT/health/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Liveness probe - Kubernetes/Docker check
     * URL: http://localhost:PORT/health/live
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", serviceName);
        status.put("port", serverPort);
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }

    /**
     * Readiness probe - Är servicen redo?
     * URL: http://localhost:PORT/health/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> status = new HashMap<>();
        boolean isReady = true;

        // Testa databas om JdbcTemplate finns
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                status.put("database", "connected");
            } catch (Exception e) {
                status.put("database", "disconnected");
                status.put("error", e.getMessage());
                isReady = false;
            }
        }

        status.put("status", isReady ? "UP" : "DOWN");
        status.put("service", serviceName);
        status.put("port", serverPort);
        status.put("timestamp", LocalDateTime.now());
        status.put("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds() + " seconds");

        return isReady ? ResponseEntity.ok(status) : ResponseEntity.status(503).body(status);
    }

    /**
     * Detaljerad status
     * URL: http://localhost:PORT/health/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", serviceName);
        health.put("port", serverPort);
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds() + " seconds");

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        health.put("memory", memory);

        // Database status om tillgänglig
        if (jdbcTemplate != null) {
            Map<String, Object> database = new HashMap<>();
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                database.put("status", "UP");
            } catch (Exception e) {
                database.put("status", "DOWN");
                database.put("error", e.getMessage());
            }
            health.put("database", database);
        }

        health.put("status", "UP");
        return ResponseEntity.ok(health);
    }

    /**
     * Spring Boot Actuator health indicator
     */
    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up()
                .withDetail("service", serviceName)
                .withDetail("port", serverPort)
                .withDetail("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds() + " seconds");

        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                healthBuilder.withDetail("database", "connected");
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", serviceName)
                        .withDetail("database", "disconnected")
                        .withException(e)
                        .build();
            }
        }

        return healthBuilder.build();
    }
}