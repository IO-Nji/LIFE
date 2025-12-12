package io.life.order.controller;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:80"})
public class HealthController {

    private static final String STATUS_KEY = "status";

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHealthStats() {
        Map<String, Object> healthStats = new HashMap<>();
        
        // Basic service information
        healthStats.put("service", "Order Processing Service");
        healthStats.put(STATUS_KEY, "UP");
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
            database.put(STATUS_KEY, "UP");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("driver", connection.getMetaData().getDriverName());
        } catch (Exception e) {
            database.put(STATUS_KEY, "DOWN");
            database.put("error", e.getMessage());
        }
        healthStats.put("database", database);
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        system.put("processors", Runtime.getRuntime().availableProcessors());
        healthStats.put("system", system);
        
        // Order processing specific information
        Map<String, Object> orderProcessing = new HashMap<>();
        orderProcessing.put("functions", "Order Management, Assembly Tracking, Manufacturing Control");
        orderProcessing.put("description", "Handles order lifecycle and manufacturing operations");
        orderProcessing.put("endpoints", "/api/orders, /api/assembly");
        healthStats.put("order_processing", orderProcessing);
        
        return ResponseEntity.ok(healthStats);
    }
}