package com.perfect8.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "image-service", url = "http://localhost:8081")
public interface ImageServiceClient {

    @GetMapping("/api/images/stats")
    ResponseEntity<Map<String, Object>> getImageStats();
}