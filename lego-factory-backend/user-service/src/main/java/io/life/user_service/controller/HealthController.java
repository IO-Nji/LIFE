package io.life.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:80"})
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHealthStats() {
        Map<String, Object> healthStats = new HashMap<>();
        
        // Basic service information
        healthStats.put("service", "User Service");
        healthStats.put("status", "UP");
        healthStats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthStats.put("version", "1.0.0");
        
        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        memory.put("max", runtime.maxMemory() / 1024 / 1024 + " MB");
        healthStats.put("memory", memory);
        
        // Database connectivity
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put("status", "UP");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("driver", connection.getMetaData().getDriverName());
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        healthStats.put("database", database);
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        system.put("processors", Runtime.getRuntime().availableProcessors());
        healthStats.put("system", system);
        
        return ResponseEntity.ok(healthStats);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSimpleHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "User Service");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(health);
    }
}