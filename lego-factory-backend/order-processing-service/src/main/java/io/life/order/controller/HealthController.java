package io.life.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-processing-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> apiHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-processing-service");
        return ResponseEntity.ok(response);
    }
}
